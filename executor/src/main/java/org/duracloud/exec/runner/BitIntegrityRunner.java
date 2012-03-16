/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.runner;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.error.ContentStoreException;
import org.duracloud.exec.handler.BitIntegrityHandler;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.duracloud.services.ComputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: 3/8/12
 */
public class BitIntegrityRunner implements Runnable {

    public static final String MODE_NAME = "generate-for-space";
    public static final String HASH_APPROACH_FILES = "generated";
    public static final String HASH_APPROACH_PROVIDER = "stored";

    protected static final String HANDLER_STATE_FILE =
        "bit-integrity-handler-state.xml";

    private final Logger log =
        LoggerFactory.getLogger(BitIntegrityRunner.class);

    private ContentStoreManager storeMgr;
    private ServicesManager servicesMgr;
    private ServiceInfo service;
    private BitIntegrityHandler handler;

    private String status;
    private boolean running;

    public BitIntegrityRunner(ContentStoreManager storeMgr,
                              ServicesManager servicesMgr,
                              ServiceInfo service,
                              BitIntegrityHandler handler) {
        this.storeMgr = storeMgr;
        this.servicesMgr = servicesMgr;
        this.service = service;
        this.handler = handler;
        this.status = "Bit Integrity: Idle";
    }

    public boolean isRunning() {
        return running;
    }

    public String getStatus() {
        return status;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        running = true;
        try {
            Map<String, ContentStore> contentStores =
                storeMgr.getContentStores();
            ContentStore primary = storeMgr.getPrimaryContentStore();
            for(ContentStore store : contentStores.values()) {
                if(running) {
                    if(store.getStoreId().equals(primary.getStoreId())) {
                        runPrimaryBitIntegrityCheck(store);
                    } else {
                        runSecondaryBitIntegrityCheck(store);
                    }
                }
            }
        } catch(ContentStoreException e) {
            setError("Unable to complete bit integrity check due to: " +
                      e.getMessage(), e);
        }
        running = false;
    }

    /*
     * Verifies the bit integrity of all files in each space using both bit
     * integrity check methods.
     */
    private void runPrimaryBitIntegrityCheck(ContentStore store) {
        runBitIntegrityCheck(store, true);
        runBitIntegrityCheck(store, false);
    }

    /*
     * Verifies the bit integrity of all files in each space using only the
     * checksum available from the storage provider.
     */
    private void runSecondaryBitIntegrityCheck(ContentStore store) {
        runBitIntegrityCheck(store, false);
    }

    /*
     * Verifies the bit integrity of all files in a space in one of two ways:
      * 1. When download == true, retrieve each file and calculate its checksum,
      *    then compare to checksums in the space manifest.
      * 2. When download == false, ask the storage provider for the checksum of
      *    each file, then compare to checksums in the space manifest
      *
      * @param store storage provider in which to check content
      * @param download true if content should be downloaded to recompute
      *                 checksums, false if provider checksums should be used
     */
    private void runBitIntegrityCheck(ContentStore store, boolean download) {
        List<String> spaceIds = getSpaces(store);
        Collections.sort(spaceIds);
        int totalSpaces = spaceIds.size();

        List<String> failureList =
            runBitIntegrityOnSpaces(store, download, spaceIds, totalSpaces, 0);

        // Retry any service runs which failed to complete
        for(int i=0; i<3; i++) {
            if(!failureList.isEmpty()) {
                int checkedSpaces = totalSpaces - failureList.size();
                failureList =
                    runBitIntegrityOnSpaces(store, download, failureList,
                                            totalSpaces, checkedSpaces);
            }
        }

        if(!failureList.isEmpty()) {
            StringBuilder spacesBuilder = new StringBuilder();
            for(String spaceId : failureList) {
                spacesBuilder.append(spaceId);
                spacesBuilder.append(" ");
            }
            String spaces = spacesBuilder.toString().trim();
            log.error("Bit integrity failed to complete for these spaces: " +
                      spaces);

            //TODO: Notify
        }
    }

    private List<String> runBitIntegrityOnSpaces(ContentStore store,
                                                 boolean download,
                                                 List<String> spaceIds,
                                                 int totalSpaces,
                                                 int checkedSpaces) {
        int spacesChecked = checkedSpaces;
        String method = download?"downloaded files":"provider";

        List<String> failureList = new ArrayList<String>();
        for(String spaceId : spaceIds) {
            if(running) {
                status = "Bit Integrity: " + spacesChecked + " spaces out of " +
                         totalSpaces + " have been checked for store " +
                         store.getStorageProviderType() + " using checksums " +
                         "from the " + method;

                try {
                    runBitIntegrityOnSpace(store, download, spaceId);
                     ++spacesChecked;
                } catch(NotFoundException e) {
                    setError("Unable to complete bit integrity check on " +
                             "space " + spaceId + "due to a " +
                             "NotFoundException: " + e.getMessage(), e);
                    failureList.add(spaceId);
                } catch(ServicesException e) {
                    setError("Unable to complete bit integrity check on " +
                             "space " + spaceId + "due to a " +
                             "ServicesException: " + e.getMessage(), e);
                    failureList.add(spaceId);
                }
            }
        }
        return failureList;
    }

    private void runBitIntegrityOnSpace(ContentStore store,
                                        boolean download,
                                        String spaceId)
        throws ServicesException, NotFoundException {
        // Deploy bit integrity check service
        int deploymentId =
            runIntegrityCheck(store.getStoreId(), spaceId, download);

        // Wait for bit integrity completion
        while(!serviceComplete(deploymentId)) {
            sleep(60000); // Wait a bit, then try again
        }

        // Get the bit integrity report
        String report = getReportId(deploymentId);

        // TODO: Compare result file to space manifest
        log.error("Should be comparing report file " + report +
                  " to space manifest.");
        // TODO: Notify on comparison mismatch
        // TODO: Write to results file (for display)

        // Undeploy bit integrity service
        servicesMgr.undeployService(service.getId(), deploymentId);
    }

    private List<String> getSpaces(ContentStore store) {
        try {
            return store.getSpaces();
        } catch(ContentStoreException e) {
            setError("Error: Unable to retrieve listing of spaces for " +
                     "store with ID " + store.getStoreId(), e);
            return new ArrayList<String>();
        }
    }

    private int runIntegrityCheck(String storeId,
                                  String spaceId,
                                  boolean generateChecksums)
        throws NotFoundException, ServicesException {
        // Get user config
        List<UserConfigModeSet> userConfigOrig =
            service.getUserConfigModeSets();
        // Clone top level config, to ensure a clean config on each run
        UserConfigModeSet userConfigMode;
        try {
            userConfigMode = userConfigOrig.get(0).clone();
        } catch(CloneNotSupportedException e) {
            throw new DuraCloudRuntimeException(e);
        }

        // Set user config selections
        for(UserConfigMode mode : userConfigMode.getModes()) {
            if(MODE_NAME.equals(mode.getName())) {
                mode.setSelected(true);

                // Set Hash approach
                SingleSelectUserConfig hashApproach =
                    (SingleSelectUserConfig)mode.getUserConfigs().get(0);
                for(Option option : hashApproach.getOptions()) {
                    if(generateChecksums) {
                        if(HASH_APPROACH_FILES.equals(option.getValue())) {
                            option.setSelected(true);
                        }
                    } else {
                        if(HASH_APPROACH_PROVIDER.equals(option.getValue())) {
                            option.setSelected(true);
                        }
                    }
                }

                // Set store and space
                for(UserConfigMode storeMode :
                    mode.getUserConfigModeSets().get(0).getModes()) {
                    if(storeId.equals(storeMode.getName())) {
                        storeMode.setSelected(true);
                        SingleSelectUserConfig spaceConfigs =
                            (SingleSelectUserConfig)storeMode
                                .getUserConfigs().get(0);
                        for(Option spaceOption : spaceConfigs.getOptions()) {
                            if(spaceId.equals(spaceOption.getValue())) {
                                spaceOption.setSelected(true);
                                break;
                            }
                        }
                    }
                }
            }
        }

        List<UserConfigModeSet> userConfig = new ArrayList<UserConfigModeSet>();
        userConfig.add(userConfigMode);

        // Run service
        return servicesMgr.deployService(service.getId(),
                                         handler.getDeploymentHost(service),
                                         service.getUserConfigVersion(),
                                         userConfig);
    }

    private boolean serviceComplete(int deploymentId)
        throws ServicesException, NotFoundException {
        Map<String, String> props =
            servicesMgr.getDeployedServiceProps(service.getId(), deploymentId);
        String statusVal = props.get(ComputeService.STATUS_KEY);
        if(null != statusVal) {
            try {
                ComputeService.ServiceStatus status =
                    ComputeService.ServiceStatus.valueOf(statusVal);
                if (status.isComplete()) {
                    return true;
                }
            } catch (Exception e) { // Property is something other than status
            }
        }
        return false;
    }

    private String getReportId(int deploymentId)
        throws ServicesException, NotFoundException {
        Map<String, String> props =
            servicesMgr.getDeployedServiceProps(service.getId(), deploymentId);
        String report = props.get(ComputeService.REPORT_KEY);
        if(null == report) {
            report = "";
        }
        return report;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    private void setError(String error, Exception e) {
        log.error(error, e);
        status = handler.ERROR_PREFIX + error;
    }

}
