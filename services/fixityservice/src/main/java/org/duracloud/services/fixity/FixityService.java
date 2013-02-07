/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.duracloud.services.common.error.ServiceException;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.results.ServiceResultListener.StatusMsg;
import org.duracloud.services.fixity.results.ServiceResultProcessor;
import org.duracloud.services.fixity.status.StatusListener;
import org.duracloud.services.fixity.worker.ServiceWorkManager;
import org.duracloud.services.fixity.worker.ServiceWorkerFactory;
import org.duracloud.services.fixity.worker.ServiceWorkload;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * This class is the entry point for performing on-demand bit-integrity
 * verification.
 *
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class FixityService extends BaseService implements ComputeService, ManagedService, StatusListener {

    private final Logger log = LoggerFactory.getLogger(FixityService.class);

    public static final String PHASE_FIND = "FindHashes";
    public static final String PHASE_COMPARE = "CompareHashes";

    private static final String DEFAULT_DURASTORE_HOST = "localhost";
    private static final String DEFAULT_DURASTORE_PORT = "8080";
    private static final String DEFAULT_DURASTORE_CONTEXT = "durastore";

    private ServiceWorkManager workManager;
    private ContentStore contentStore;
    private String autoHashContentId;

    private String duraStoreHost;
    private String duraStorePort;
    private String duraStoreContext;
    private String username;
    private String password;

    private String defaultOutputSpaceId;

    private String mode;
    private String hashApproach;
    private String salt;
    private String isFailFast;
    private String storeId;
    private String providedListingSpaceIdA;
    private String providedListingSpaceIdB;
    private String providedListingContentIdA;
    private String providedListingContentIdB;
    private String targetSpaceId;
    private String outputSpaceId;
    private String outputContentId;
    private String reportContentId;
    private String errorReportContentId;

    private StatusMsg processingStatusMsg;
    private boolean keepWorking;

    private int threads = 3;


    @Override
    public void start() throws Exception {
        super.start();
        workManager = null;
        processingStatusMsg = null;
        keepWorking = true;
        autoHashContentId = null;

        StringBuilder sb = new StringBuilder("Starting Fixity Service as '");
        sb.append(getUsername());
        sb.append(": ");
        sb.append(threads);
        sb.append(" worker threads");
        log.info(sb.toString());

        setUp();
        new Thread(new FixityServiceThread()).start();
        new Thread(new ProcessingStatusMonitorThread()).start();
    }

    private void setUp() {
        String outSpaceId = getServiceOptions().getOutputSpaceId();
        if (null == outSpaceId) {
            return;
        }

        try {
            getContentStore().getSpaceProperties(outSpaceId);
            return;

        } catch (ContentStoreException e) {
            log.info("Output space does not already exist: {}", outSpaceId);
        }

        try {
            log.debug("Creating output space: {}", outSpaceId);
            getContentStore().createSpace(outSpaceId);
            
        } catch (ContentStoreException e) {
            log.warn("Error in creating output space: {}", outSpaceId);
        }
    }

    private class FixityServiceThread implements Runnable {
        @Override
        public void run() {
            try {
                setServiceStatus(ServiceStatus.PROCESSING);
                doStart();

            } catch (Exception e) {
                StringBuilder err = new StringBuilder("Error starting service: ");
                err.append(e.getMessage());
                log.error(err.toString());

                setError(err.toString());
                setServiceStatus(ServiceStatus.INSTALLED);
                keepWorking = false;
            }
        }
    }

    private class ProcessingStatusMonitorThread implements Runnable {
        @Override
        public void run() {
            while (keepWorking) {
                log.debug("PStatusMonitorThread.run() " + processingStatusMsg);
                updateProcessingStatus();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }
    }

    private void updateProcessingStatus() {
        log.debug("updateProcessingStatus() " + processingStatusMsg);
        if (workManager != null) {
            processingStatusMsg = workManager.getProcessingStatus();
        }
    }

    private void doStart() throws Exception {
        FixityServiceOptions serviceOptions = getServiceOptions();
        ContentStore contentStore = getContentStore();

        File workDir = new File(getServiceWorkDir());
        CountDownLatch doneHashing = new CountDownLatch(1);
        if (serviceOptions.needsToHash()) {
            startHashing(contentStore, serviceOptions, workDir, doneHashing);
        } else {
            doneHashing.countDown();
        }

        CountDownLatch doneComparing = new CountDownLatch(1);
        if (serviceOptions.needsToCompare()) {
            startComparing(contentStore, serviceOptions, workDir, doneHashing, doneComparing);
        } else {
            doneComparing.countDown();
        }

        cleanup(serviceOptions, doneHashing, doneComparing);
        setReportIds(serviceOptions);

        doneWorking();
    }

    private void startHashing(ContentStore contentStore,
                              FixityServiceOptions serviceOptions,
                              File workDir,
                              CountDownLatch doneHashing)
        throws ServiceException {
        
        CountDownLatch doneAuto = new CountDownLatch(1);
        if (serviceOptions.needsToAutoGenerateHashListing()) {
            FixityServiceOptions autoOptions = getAutoHashingOptions();
            doStartHashing(contentStore, autoOptions, workDir, doneAuto);

        } else {
            doneAuto.countDown();
        }

        waitForLatch(doneAuto);
        doStartHashing(contentStore, serviceOptions, workDir, doneHashing);
    }

    private void doStartHashing(ContentStore contentStore,
                                FixityServiceOptions serviceOptions,
                                File workDir,
                                CountDownLatch doneHashing) {
        ServiceResultProcessor resultListener = new ServiceResultProcessor(
            contentStore,
            getHashingHeader(),
            this,
            serviceOptions.getOutputSpaceId(),
            serviceOptions.getOutputContentId(),
            null,
            PHASE_FIND,
            workDir);

        ServiceWorkload workload = new HashFinderWorkload(serviceOptions,
                                                          contentStore);

        ServiceWorkerFactory workerFactory = new HashFinderWorkerFactory(
            serviceOptions,
            contentStore,
            resultListener);

        workManager = new ServiceWorkManager(workload,
                                             workerFactory,
                                             resultListener,
                                             threads,
                                             doneHashing);
        workManager.start();
    }

    private void startComparing(ContentStore contentStore,
                                FixityServiceOptions serviceOptions,
                                File workDir,
                                CountDownLatch doneHashing,
                                CountDownLatch doneComparing)
        throws ServiceException {
        waitForLatch(doneHashing);

        StatusMsg previousPhaseStatus = null;
        if (workManager != null) {
            previousPhaseStatus = workManager.getProcessingStatus();
        }

        // Build result file header
        String spaceA = serviceOptions.getProvidedListingSpaceIdA();
        String contentA = serviceOptions.getProvidedListingContentIdA();
        String spaceB = serviceOptions.getProvidedListingSpaceIdB();
        if(null == spaceB) {
            spaceB = serviceOptions.getOutputSpaceId();
        }
        String contentB = serviceOptions.getProvidedListingContentIdB();
        if(null == contentB) {
            contentB = serviceOptions.getOutputContentId();
        }

        String comparingHeader =
            getComparingHeader(spaceA, contentA, spaceB, contentB);

        // Set up result file writer
        ServiceResultListener resultListener = new ServiceResultProcessor(
            contentStore,
            comparingHeader,
            this,
            serviceOptions.getOutputSpaceId(),
            serviceOptions.getReportContentId(),
            serviceOptions.getErrorContentId(),
            PHASE_COMPARE,
            (previousPhaseStatus == null) ? null : previousPhaseStatus.toString(),
            workDir);

        if(previousPhaseStatus != null){
            resultListener.setTotalWorkItems(previousPhaseStatus.getTotal());
        }

        // Set up and run work manager
        ServiceWorkerFactory workerFactory = new HashVerifierWorkerFactory(
            contentStore,
            workDir,
            resultListener);

        ServiceWorkload workload = new HashVerifierWorkload(serviceOptions);
        workManager = new ServiceWorkManager(workload,
                                             workerFactory,
                                             resultListener,
                                             threads,
                                             doneComparing);
        workManager.start();
    }

    private void cleanup(FixityServiceOptions serviceOptions,
                         CountDownLatch doneHashing,
                         CountDownLatch doneComparing) throws ServiceException {
        waitForLatch(doneHashing);
        waitForLatch(doneComparing);

        // skip case where goal of run is to generate a hash listing
        if (serviceOptions.needsToHash() && serviceOptions.needsToCompare()) {
            deleteContent(serviceOptions.getOutputSpaceId(),
                          serviceOptions.getOutputContentId());
        }
        if (serviceOptions.needsToAutoGenerateHashListing()) {
            deleteContent(serviceOptions.getOutputSpaceId(),
                          autoHashContentId);
        }
    }

    private void deleteContent(String spaceId, String contentId) {
        try {
            getContentStore().deleteContent(spaceId, contentId);
        } catch (ContentStoreException e) {
            log.warn("Unable to delete content: " + e.getMessage());
        }
    }

    private void waitForLatch(CountDownLatch doneHashing)
        throws ServiceException {
        try {
            doneHashing.await();

        } catch (InterruptedException e) {
            StringBuilder sb = new StringBuilder("Error: ");
            sb.append("calling doneWorking.await(): ");
            sb.append(e.getMessage());
            log.error(sb.toString(), e);
            setError(sb.toString());
            throw new ServiceException(sb.toString(), e);
        }
    }

     private void setReportIds(FixityServiceOptions serviceOptions) {
        String spaceId = serviceOptions.getOutputSpaceId();
        String reportId = serviceOptions.getReportContentId();
        String storeId = serviceOptions.getStoreId();

        // are we only creating hashes? with no actual report?
        if (serviceOptions.needsToHash() && !serviceOptions.needsToCompare()) {
            reportId = serviceOptions.getOutputContentId();
        }
        super.setReportId(spaceId, reportId, serviceOptions.getStoreId());
        super.setErrorReportId(spaceId, serviceOptions.getErrorContentId(),storeId);
    }

    @Override
    public void stop() throws Exception {
        log.info("FixityService is Stopping");
        this.setServiceStatus(ServiceStatus.STOPPING);
        if (workManager != null) {
            workManager.stopProcessing();
        }

        doneWorking();
        this.setServiceStatus(ServiceStatus.STOPPED);
    }

    @Override
    public void doneWorking() {
        super.doneWorking();
        updateProcessingStatus();
        keepWorking = false;
    }

    @Override
    public Map<String, String> getServiceProps() {
        Map<String, String> props = super.getServiceProps();
        if (processingStatusMsg != null) {
            log.debug("getServiceProps() " + processingStatusMsg);
            updateProcessingStatus();
            props.put(ServiceResultProcessor.STATUS_KEY, processingStatusMsg.toString());

            if(getServiceStatus() != ServiceStatus.FAILED){
                long failed = processingStatusMsg.getFailed();
                long passed = processingStatusMsg.getPassed();
                long total  = processingStatusMsg.getTotal();
                if(total < 0){
                    total = passed+failed;
                }

                props.put(ComputeService.ITEMS_PROCESS_COUNT, String.valueOf(total));
                props.put(ComputeService.FAILURE_COUNT_KEY, String.valueOf(failed));
                props.put(ComputeService.PASS_COUNT_KEY, String.valueOf(passed));
                if(processingStatusMsg.getFailed() > 0){
                    addErrorReport(props);
                }
            }
        }
        return props;
    }

    @Override
    public void updated(Dictionary config) throws ConfigurationException {
        log.warn("Attempt made to update Fixity Service configuration " +
            "via updated method. Updates should occur via class setters.");
    }

    private ContentStore getContentStore() throws ContentStoreException {
        ContentStore store = contentStore;
        if (null == contentStore) {
            ContentStoreManager storeManager = new ContentStoreManagerImpl(
                getDuraStoreHost(),
                getDuraStorePort(),
                getDuraStoreContext());
            storeManager.login(new Credential(getUsername(), getPassword()));
            store = storeManager.getContentStore(storeId);
        }
        return store;
    }

    private FixityServiceOptions getServiceOptions() {
        if (null == outputSpaceId || outputSpaceId.equals("")) {
            this.outputSpaceId = this.defaultOutputSpaceId;
        }

        FixityServiceOptions opts = new FixityServiceOptions(mode,
                                                             hashApproach,
                                                             salt,
                                                             isFailFast,
                                                             storeId,
                                                             providedListingSpaceIdA,
                                                             providedListingSpaceIdB,
                                                             providedListingContentIdA,
                                                             providedListingContentIdB,
                                                             targetSpaceId,
                                                             outputSpaceId,
                                                             outputContentId,
                                                             reportContentId, 
                                                             errorReportContentId);

        opts.verify();
        log.debug(opts.toString());
        return opts;
    }

    private FixityServiceOptions getAutoHashingOptions() {
        if (null == outputSpaceId || outputSpaceId.equals("")) {
            this.outputSpaceId = this.defaultOutputSpaceId;
        }

        FixityServiceOptions.Mode m = FixityServiceOptions.Mode.GENERATE_SPACE;
        FixityServiceOptions.HashApproach ha = FixityServiceOptions.HashApproach.STORED;
        String outContentId = FixityServiceOptions.defaultGenContentId;

        FixityServiceOptions opts = new FixityServiceOptions(m.name(),
                                                             ha.name(),
                                                             salt,
                                                             isFailFast,
                                                             storeId,
                                                             providedListingSpaceIdA,
                                                             providedListingSpaceIdB,
                                                             providedListingContentIdA,
                                                             providedListingContentIdB,
                                                             targetSpaceId,
                                                             outputSpaceId,
                                                             outContentId,
                                                             reportContentId,
                                                             errorReportContentId);

        opts.verify();
        log.debug(opts.toString());

        // cache auto-generated hash content-id for clean-up later.
        autoHashContentId = opts.getOutputContentId();

        return opts;
    }

    public void setContentStore(ContentStore contentStore) {
        this.contentStore = contentStore;
    }

    public void setDuraStoreHost(String duraStoreHost) {
        log.info("setDuraStoreHost (" + duraStoreHost + ")");
        this.contentStore = null;
        this.duraStoreHost = duraStoreHost;
    }

    public void setDuraStorePort(String duraStorePort) {
        log.info("set duraStorePort(" + duraStorePort + ")");
        this.contentStore = null;
        this.duraStorePort = duraStorePort;
    }

    public void setDuraStoreContext(String duraStoreContext) {
        log.info("set duraStoreContext(" + duraStoreContext + ")");
        this.contentStore = null;
        this.duraStoreContext = duraStoreContext;
    }

    public void setUsername(String username) {
        log.info("set username(" + username + ")");
        this.username = username;
    }

    public void setPassword(String password) {
        log.info("set password(*)");
        this.password = password;
    }

    public void setDefaultOutputSpaceId(String defaultOutputSpaceId) {
        log.info("set defaultOutputSpaceId(" + defaultOutputSpaceId + ")");
        this.defaultOutputSpaceId = defaultOutputSpaceId;
    }

    public void setMode(String mode) {
        log.info("set mode(" + mode + ")");
        this.mode = mode;
    }

    public void setHashApproach(String hashApproach) {
        log.info("set hashApproach(" + hashApproach + ")");
        this.hashApproach = hashApproach;
    }

    public void setSalt(String salt) {
        log.info("set salt(" + salt + ")");
        this.salt = salt;
    }

    public void setFailFast(String failFast) {
        log.info("set failFast(" + failFast + ")");
        isFailFast = failFast;
    }

    public void setStoreId(String storeId) {
        log.info("set storeId(" + storeId + ")");
        this.storeId = storeId;
    }

    public void setProvidedListingSpaceIdA(String providedListingSpaceIdA) {
        log.info(
            "set providedListingSpaceIdA(" + providedListingSpaceIdA + ")");
        this.providedListingSpaceIdA = providedListingSpaceIdA;
    }

    public void setProvidedListingSpaceIdB(String providedListingSpaceIdB) {
        log.info(
            "set providedListingSpaceIdB(" + providedListingSpaceIdB + ")");
        this.providedListingSpaceIdB = providedListingSpaceIdB;
    }

    public void setProvidedListingContentIdA(String providedListingContentIdA) {
        log.info(
            "set providedListingContentIdA(" + providedListingContentIdA + ")");
        this.providedListingContentIdA = providedListingContentIdA;
    }

    public void setProvidedListingContentIdB(String providedListingContentIdB) {
        log.info(
            "set providedListingContentIdB(" + providedListingContentIdB + ")");
        this.providedListingContentIdB = providedListingContentIdB;
    }

    public void setTargetSpaceId(String targetSpaceId) {
        log.info("set targetSpaceId(" + targetSpaceId + ")");
        this.targetSpaceId = targetSpaceId;
    }

    public void setOutputSpaceId(String outputSpaceId) {
        log.info("set outputSpaceId(" + outputSpaceId + ")");
        this.outputSpaceId = outputSpaceId;
    }

    public void setOutputContentId(String outputContentId) {
        log.info("set outputContentId(" + outputContentId + ")");
        this.outputContentId = outputContentId;
    }

    public void setReportContentId(String reportContentId) {
        log.info("set reportContentId(" + reportContentId + ")");
        this.reportContentId = reportContentId;
    }

    public void setErrorReportContentId(String errorReportContentId) {
        log.info("set errorReportContentId(" + errorReportContentId + ")");
        this.errorReportContentId = errorReportContentId;
    }

    public void setThreads(int threads) {
        log.info("set threads(" + threads + ")");
        this.threads = threads;
    }

    public String getMode() {
        return this.mode;
    }

    public String getDuraStoreHost() {
        if (null == duraStoreHost) {
            duraStoreHost = DEFAULT_DURASTORE_HOST;
        }
        return duraStoreHost;
    }

    public String getDuraStorePort() {
        if (null == duraStorePort) {
            duraStorePort = DEFAULT_DURASTORE_PORT;
        }
        return duraStorePort;
    }

    public String getDuraStoreContext() {
        if (null == duraStoreContext) {
            duraStoreContext = DEFAULT_DURASTORE_CONTEXT;
        }
        return duraStoreContext;
    }

    public String getUsername() {
        if (null == username) {
            username = "username-null";
        }
        return username;
    }

    public String getPassword() {
        if (null == password) {
            password = "password-null";
        }
        return password;
    }

    protected String getHashingHeader() {
        return "space-id" + DELIM + "content-id" + DELIM + "MD5";
    }

    protected String getComparingHeader(String spaceIdA,
                                      String contentIdA,
                                      String spaceIdB,
                                      String contentIdB) {
        String locA = spaceIdA + "/" + contentIdA;
        String locB = spaceIdB + "/" + contentIdB;
        return "space-id" + DELIM + "content-id" + DELIM +
               "0:" + locA + DELIM + "1:" + locB + DELIM + "status";
    }

}
