/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.runner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.DateUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.exec.error.ManifestException;
import org.duracloud.exec.handler.BitIntegrityHandler;
import org.duracloud.execdata.bitintegrity.SpaceBitIntegrityResult;
import org.duracloud.manifest.ManifestGenerator;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestEmptyException;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.duracloud.services.ComputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
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
 * Does the actual work of running bit integrity checks over spaces.
 *
 * @author: Bill Branan
 * Date: 3/8/12
 */
public class BitIntegrityRunner implements Runnable {

    protected static final String GENERATE_MODE_NAME = "generate-for-space";
    protected static final String COMPARE_MODE_NAME = "compare";
    protected static final String HASH_APPROACH_FILES = "generated";
    protected static final String HASH_APPROACH_PROVIDER = "stored";

    protected static final String HANDLER_STATE_FILE =
        "bit-integrity-handler-state.xml";
    protected static final String STATE_STORE_ID = "store-id";
    protected static final String STATE_SPACE_ID = "space-id";
    protected static final String MANIFEST_CONTENT_ID = "manifest.tsv";
    protected static final String RESULT_SPACE_ID = "x-service-out";
    protected static final String RESULT_PREFIX = "bit-integrity";

    private final Logger log =
        LoggerFactory.getLogger(BitIntegrityRunner.class);

    private ContentStoreManager storeMgr;
    private ServicesManager servicesMgr;
    private ManifestGenerator manifestGenerator;
    private ServiceInfo service;
    private BitIntegrityHandler handler;

    private String status;
    private boolean running;

    public BitIntegrityRunner(ContentStoreManager storeMgr,
                              ServicesManager servicesMgr,
                              ManifestGenerator manifestGenerator,
                              ServiceInfo service,
                              BitIntegrityHandler handler) {
        this.storeMgr = storeMgr;
        this.servicesMgr = servicesMgr;
        this.manifestGenerator = manifestGenerator;
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
            String spaces =
                spacesBuilder.toString().trim().replaceAll(" ", ", ");
            String message = "The DuraCloud Executor failed to complete bit" +
                             " integrity runs for the following spaces: " +
                             spaces;
            log.error(message);
            handler.notify(message);
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
                } catch(Exception e) {
                    setError("Unable to complete bit integrity check on " +
                             "space " + spaceId + " due to a " +
                             e.getClass().getName() + ": " + e.getMessage(), e);
                    failureList.add(spaceId);
                }

                status = "Bit Integrity: " + spacesChecked + " spaces out of " +
                         totalSpaces + " have been checked for store " +
                         spType + " using checksums from the " + method;
            }
        }
        return failureList;
    }

    protected void runBitIntegrityOnSpace(ContentStore store,
                                          boolean download,
                                          String spaceId)
        throws ServicesException, NotFoundException, ManifestException {
        // Deploy bit integrity check service
        String storeId = store.getStoreId();
        int deploymentId = runIntegrityCheck(storeId, spaceId, download);

        // Wait for bit integrity completion
        while(!serviceComplete(deploymentId)) {
            sleep(60000); // Wait a bit, then try again
        }

        // Get the bit integrity report
        Map<String, String> props = getServiceProps(deploymentId);
        String reportPath = getReportId(props);
        String reportContentId = getReportContentId(reportPath);

        // Undeploy bit integrity tools (after bit integrity run)
        servicesMgr.undeployService(service.getId(), deploymentId);

        boolean success = true;
        if(isServiceSuccessful(props)) { // Verify successful to continue
            // Store the space manifest in DuraCloud
            storeManifest(store, spaceId);

            // Compare result file to space manifest
            deploymentId = runManifestComparison(storeId,
                                                 RESULT_SPACE_ID,
                                                 reportContentId,
                                                 MANIFEST_CONTENT_ID);

            // Wait for comparison completion
            while(!serviceComplete(deploymentId)) {
                sleep(20000); // Wait a bit, then try again
            }

            // Get the manifest comparison report
            Map<String, String> compareProps = getServiceProps(deploymentId);
            reportPath = getReportId(compareProps);

            // Undeploy bit integrity tools (after comparison run)
            servicesMgr.undeployService(service.getId(), deploymentId);

            if(!isServiceSuccessful(compareProps)) {
                success = false;
                notifyOnFailure(download, true, spaceId,
                                store, props, compareProps);
            }
        } else {
            success = false;
            notifyOnFailure(download, false, spaceId, store, props, null);
        }

        // Write to results file (for display)
        SpaceBitIntegrityResult result =
            new SpaceBitIntegrityResult(new Date(),
                                        success ? "success" : "failure",
                                        reportPath,
                                        success ? true : false);
        handler.storeResults(storeId, spaceId, result);
    }

    private void notifyOnFailure(boolean download,
                                 boolean onCompare,
                                 String spaceId,
                                 ContentStore store,
                                 Map<String, String> props,
                                 Map<String, String> compareProps) {
        String type = download ? "generated from the stored files" :
                     "retrieved from the storage provider";
        String reason = onCompare ?
            "while comparing the manifest checksums and those " :
            "while collecting checksums ";
        String message =
            "The DuraCloud Executor reported a failed bit integrity run " +
            reason + type + ". The failure was in space with ID " + spaceId +
            " in store with ID " + store.getStoreId() + " and type " +
            store.getStorageProviderType() + "." +
            "\n\nThe properties of the bit integrity run where checksums from" +
            " the space were collected as they were " + type + ":" +
            buildPropsString(props);
        if(onCompare) {
            message += ("\n\nThe properties of the comparison bit integrity " +
            "run showing the failure were: " + buildPropsString(compareProps));
        }
        log.error(message);
        handler.notify(message);
    }

    private String buildPropsString(Map<String, String> props) {
        StringBuilder builder = new StringBuilder();
        for(String key : props.keySet()) {
            builder.append("\n  ").append(key).append(": ")
                   .append(props.get(key));
        }
        return builder.toString();
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
            if(GENERATE_MODE_NAME.equals(mode.getName())) {
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
                        updateSpaceConfig(spaceConfigs, spaceId);
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

    private int runManifestComparison(String storeId,
                                      String spaceId,
                                      String reportContentId,
                                      String manifestContentId)
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
            if(COMPARE_MODE_NAME.equals(mode.getName())) {
                mode.setSelected(true);

                // Set contentIds
                TextUserConfig contentIdA =
                    (TextUserConfig)mode.getUserConfigs().get(0);
                contentIdA.setValue(reportContentId);
                TextUserConfig contentIdB =
                    (TextUserConfig)mode.getUserConfigs().get(1);
                contentIdB.setValue(manifestContentId);

                // Set store and space
                for(UserConfigMode storeMode :
                    mode.getUserConfigModeSets().get(0).getModes()) {
                    if(storeId.equals(storeMode.getName())) {
                        storeMode.setSelected(true);

                        SingleSelectUserConfig spaceConfigsA =
                            (SingleSelectUserConfig)storeMode
                                .getUserConfigs().get(0);
                        updateSpaceConfig(spaceConfigsA, spaceId);

                        SingleSelectUserConfig spaceConfigsB =
                            (SingleSelectUserConfig)storeMode
                                .getUserConfigs().get(1);
                        updateSpaceConfig(spaceConfigsB, spaceId);
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

    private void updateSpaceConfig(SingleSelectUserConfig spaceConfigs,
                                   String spaceId) {
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

    private String getReportId(Map<String, String> props)
        throws ServicesException {
        String report = props.get(ComputeService.REPORT_KEY);
        if(null == report || report.equals("")) {
            String err = "No bit integrity report file available.";
            throw new ServicesException(err);
        }
        return report;
    }

    private String getReportContentId(String reportPath) {
        int endIndex = reportPath.indexOf("?");
        if(endIndex < 0) {
            endIndex = reportPath.length();
        }
        return reportPath.substring(reportPath.indexOf(RESULT_PREFIX),
                                    endIndex);
    }

    private void storeManifest(ContentStore store, String spaceId)
        throws ManifestException {
        try {
            boolean success = false;
            int attempts = 0;
            while(!success && attempts < 5) {
                InputStream stream = getManifest(store.getStoreId(), spaceId);
                File tempFile = IOUtil.writeStreamToFile(stream);
                InputStream fileStream = IOUtil.getFileStream(tempFile);
                try {
                    store.addContent(RESULT_SPACE_ID,
                                     MANIFEST_CONTENT_ID,
                                     fileStream,
                                     tempFile.length(),
                                     Constants.TEXT_TSV,
                                     null,
                                     null);
                    success = true;
                } catch(ContentStoreException e) {
                    log.warn("Failed to store manifest file due to: " +
                             e.getMessage());
                } finally {
                    IOUtils.closeQuietly(fileStream);
                    FileUtils.deleteQuietly(tempFile);
                }
                ++attempts;
            }
            if(!success) {
                String err = "Exceeded allowable attempts to store file";
                throw new DuraCloudRuntimeException(err);
            }
        } catch(Exception e) {
            String error = "Not able to store manifest file due to " +
                           e.getClass().getName() + ": " + e.getMessage();
            throw new ManifestException(error, e);
        }
    }

    private InputStream getManifest(String storeId, String spaceId)
        throws ManifestArgumentException, ManifestEmptyException {
        return manifestGenerator.getManifest(storeId,
                                             spaceId,
                                             ManifestGenerator.FORMAT.TSV,
                                             null);
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
