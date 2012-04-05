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
import org.duracloud.common.constant.Constants;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.DateUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.exec.handler.BitIntegrityHandler;
import org.duracloud.execdata.bitintegrity.SpaceBitIntegrityResult;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    protected static final String STATE_STORE_ID = "store-id";
    protected static final String STATE_SPACE_ID = "space-id";

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

        Map<String, String> state = handler.getState(HANDLER_STATE_FILE);
        String stateStoreId = state.get(STATE_STORE_ID);
        String stateSpaceId = state.get(STATE_SPACE_ID);

        try {
            Map<String, ContentStore> contentStores =
                storeMgr.getContentStores();

            if(!contentStores.isEmpty()) {
                // Sort the stores (for consistency), and find the state store
                Set<String> keySet = contentStores.keySet();
                String[] keys = keySet.toArray(new String[keySet.size()]);
                Arrays.sort(keys);
                int startIndex = 0;
                if(null != stateStoreId && keySet.contains(stateStoreId)) {
                    startIndex = Arrays.binarySearch(keys, stateStoreId);
                }

                ContentStore primary = storeMgr.getPrimaryContentStore();
                for(int i=startIndex; i<keys.length; i++) {
                    if(running) {
                        ContentStore store = contentStores.get(keys[i]);
                        if(store.getStoreId().equals(primary.getStoreId())) {
                            runPrimaryBitIntegrityCheck(store, stateSpaceId);
                        } else {
                            runSecondaryBitIntegrityCheck(store, stateSpaceId);
                        }
                    }
                }
            }
        } catch(ContentStoreException e) {
            setError("Unable to complete bit integrity check due to: " +
                      e.getMessage(), e);
        }
        status = "Bit Integrity: Check Completed on: " + DateUtil.now();
        running = false;
    }

    /*
     * Verifies the bit integrity of all files in each space using both bit
     * integrity check methods.
     */
    private void runPrimaryBitIntegrityCheck(ContentStore store,
                                             String stateSpaceId) {
        runBitIntegrityCheck(store, stateSpaceId, true);
        runBitIntegrityCheck(store, stateSpaceId, false);
    }

    /*
     * Verifies the bit integrity of all files in each space using only the
     * checksum available from the storage provider.
     */
    private void runSecondaryBitIntegrityCheck(ContentStore store,
                                               String stateSpaceId) {
        runBitIntegrityCheck(store, stateSpaceId, false);
    }

    /*
     * Verifies the bit integrity of all files in a space in one of two ways:
      * 1. When download == true, retrieve each file and calculate its checksum,
      *    then compare to checksums in the space manifest.
      * 2. When download == false, ask the storage provider for the checksum of
      *    each file, then compare to checksums in the space manifest
      *
      * @param store storage provider in which to check content
      * @param stateSpaceId Id of the space on which to start the run
      * @param download true if content should be downloaded to recompute
      *                 checksums, false if provider checksums should be used
     */
    private void runBitIntegrityCheck(ContentStore store,
                                      String stateSpaceId,
                                      boolean download) {
        List<String> spaceIds = getSpaces(store);
        Collections.sort(spaceIds);
        int totalSpaces = spaceIds.size();

        List<String> failureList =
            runBitIntegrityOnSpaces(store, download, spaceIds, stateSpaceId,
                                    totalSpaces, 0, true);

        if(running) {
            // Clear state (unless run was interrupted)
            handler.clearState(HANDLER_STATE_FILE);

            // Retry any service runs which failed to complete
            for(int i=0; i<3; i++) {
                if(!failureList.isEmpty()) {
                    int checkedSpaces = totalSpaces - failureList.size();
                    failureList = runBitIntegrityOnSpaces(store,
                                                          download,
                                                          failureList,
                                                          null,
                                                          totalSpaces,
                                                          checkedSpaces,
                                                          false);
                }
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
                                                 String stateSpaceId,
                                                 int totalSpaces,
                                                 int checkedSpaces,
                                                 boolean storeState) {
        int spacesChecked = checkedSpaces;
        String method = download?"downloaded files":"provider";

        List<String> failureList = new ArrayList<String>();

        // Determine the correct position in the list to start
        Iterator<String> spaceIdsIt;
        if(null != stateSpaceId && spaceIds.contains(stateSpaceId)) {
            int spaceIndex = spaceIds.indexOf(stateSpaceId);
            spaceIdsIt = spaceIds.listIterator(spaceIndex);
            if(spacesChecked == 0) {
                spacesChecked = spaceIndex;
            }
        } else {
            spaceIdsIt = spaceIds.iterator();
        }

        String spType = store.getStorageProviderType();
        status = "Bit Integrity: Beginning check of " + totalSpaces +
                 " in store " + spType + " using checksums from the " + method;

        // Check bit integrity of each space
        while(spaceIdsIt.hasNext()) {
            String spaceId = spaceIdsIt.next();
            if(running && !Constants.SYSTEM_SPACES.contains(spaceId)) {
                if(storeState) {
                    Map<String, String> state = new HashMap<String, String>();
                    state.put(STATE_STORE_ID, store.getStoreId());
                    state.put(STATE_SPACE_ID, spaceId);
                    handler.storeState(HANDLER_STATE_FILE, state);
                }

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

                status = "Bit Integrity: " + spacesChecked + " spaces out of " +
                         totalSpaces + " have been checked for store " +
                         spType + " using checksums from the " + method;
            }
        }
        return failureList;
    }

    private void runBitIntegrityOnSpace(ContentStore store,
                                        boolean download,
                                        String spaceId)
        throws ServicesException, NotFoundException {
        // Deploy bit integrity check service
        String storeId = store.getStoreId();
        int deploymentId = runIntegrityCheck(storeId, spaceId, download);

        // Wait for bit integrity completion
        while(!serviceComplete(deploymentId)) {
            sleep(60000); // Wait a bit, then try again
        }

        // Get the bit integrity report
        Map<String, String> props = getServiceProps(deploymentId);
        String reportId = getReportId(props);

        // TODO: Compare result file to space manifest
        log.error("Should be comparing report file " + reportId +
                  " to space manifest.");
        // TODO: Notify on comparison mismatch

        // TODO: Use result of manifest comparison here
        boolean serviceSuccess = isServiceSuccessful(props);

        // Write to results file (for display)
        SpaceBitIntegrityResult result =
            new SpaceBitIntegrityResult(new Date(),
                                        serviceSuccess ? "success" : "failure",
                                        reportId,
                                        serviceSuccess ? true : false);
        handler.storeResults(storeId, spaceId, result);

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

                        boolean optionFound = false;
                        List<Option> spaceOptions = spaceConfigs.getOptions();
                        for(Option spaceOption : spaceOptions) {
                            if(spaceId.equals(spaceOption.getValue())) {
                                optionFound = true;
                                spaceOption.setSelected(true);
                                break;
                            }
                        }
                        if(!optionFound) { // Space not found as an option
                            // Space must have been created after the service
                            // was deployed. Add the space as a new option.
                            Option newOption =
                                new Option(spaceId, spaceId, true);
                            List<Option> updatedOptions =
                                new ArrayList<Option>();
                            updatedOptions.addAll(spaceOptions);
                            updatedOptions.add(newOption);
                            spaceConfigs.setOptions(updatedOptions);
                        }
                    } else {
                        storeMode.setSelected(false);
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

    private Map<String, String> getServiceProps(int deploymentId)
        throws ServicesException, NotFoundException {
        return servicesMgr.getDeployedServiceProps(service.getId(),
                                                   deploymentId);
    }

    private String getReportId(Map<String, String> props) {
        String report = props.get(ComputeService.REPORT_KEY);
        if(null == report) {
            report = "";
        }
        return report;
    }

    private boolean isServiceSuccessful(Map<String, String> props) {
        boolean success = false;
        String status = props.get(ComputeService.STATUS_KEY);
        if(ComputeService.ServiceStatus.SUCCESS.name().equals(status)) {
            String failedItems = props.get(ComputeService.FAILURE_COUNT_KEY);
            if("0".equals(failedItems)) {
                success = true;
            }
        }
        return success;
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
