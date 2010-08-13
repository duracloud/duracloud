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
import org.duracloud.services.fixity.results.ServiceResultProcessor;
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
public class FixityService extends BaseService implements ComputeService, ManagedService {

    private final Logger log = LoggerFactory.getLogger(FixityService.class);

    private static final String DEFAULT_DURASTORE_HOST = "localhost";
    private static final String DEFAULT_DURASTORE_PORT = "8080";
    private static final String DEFAULT_DURASTORE_CONTEXT = "durastore";

    private ServiceWorkManager workManager;
    private ContentStore contentStore;

    private String duraStoreHost;
    private String duraStorePort;
    private String duraStoreContext;
    private String username;
    private String password;

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

    private int threads;


    private String text;


    @Override
    public void start() throws Exception {
        this.setServiceStatus(ServiceStatus.STARTING);

        StringBuilder sb = new StringBuilder("Starting Fixity Service as '");
        sb.append(getUsername());
        sb.append(": ");
        sb.append(threads);
        sb.append(" worker threads");
        log.info(sb.toString());

        try {
            doStart();
            this.setServiceStatus(ServiceStatus.STARTED);

        } catch (Exception e) {
            StringBuilder err = new StringBuilder("Error starting service: ");
            err.append(e.getMessage());
            log.error(err.toString());

            this.setServiceStatus(ServiceStatus.INSTALLED);
        }
    }

    public void doStart() throws Exception {
        FixityServiceOptions serviceOptions = getServiceOptions();
        ContentStore contentStore = getContentStore();

        File workDir = new File(getServiceWorkDir());
        CountDownLatch doneHashing = new CountDownLatch(1);
        if (serviceOptions.needsToHash()) {
            startHashing(contentStore, serviceOptions, workDir, doneHashing);
        } else {
            doneHashing.countDown();
        }

        if (serviceOptions.needsToCompare()) {
            startComparing(contentStore, serviceOptions, workDir, doneHashing);
        }
    }

    private void startHashing(ContentStore contentStore,
                              FixityServiceOptions serviceOptions,
                              File workDir,
                              CountDownLatch doneHashing) {
        ServiceResultProcessor resultListener = new ServiceResultProcessor(
            contentStore,
            outputSpaceId,
            outputContentId,
            "FindHashes",
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
                                CountDownLatch doneHashing)
        throws ServiceException {
        try {
            doneHashing.await();

        } catch (InterruptedException e) {
            StringBuilder sb = new StringBuilder("Error: ");
            sb.append("calling doneWorking.await(): ");
            sb.append(e.getMessage());
            log.error(sb.toString(), e);
            throw new ServiceException(sb.toString(), e);
        }

        String previousPhaseStatus = null;
        if (workManager != null) {
            previousPhaseStatus = workManager.getProcessingStatus();
        }

        ServiceResultListener resultListener = new ServiceResultProcessor(
            contentStore,
            outputSpaceId,
            reportContentId,
            "CompareHashes",
            previousPhaseStatus,
            workDir);

        ServiceWorkerFactory workerFactory = new HashVerifierWorkerFactory(
            contentStore,
            workDir,
            resultListener);

        ServiceWorkload workload = new HashVerifierWorkload(serviceOptions);
        workManager = new ServiceWorkManager(workload,
                                             workerFactory,
                                             resultListener,
                                             threads,
                                             doneHashing);
        workManager.start();
    }


    @Override
    public void stop() throws Exception {
        log.info("FixityService is Stopping");
        this.setServiceStatus(ServiceStatus.STOPPING);
        if (workManager != null) {
            workManager.stopProcessing();
        }
        this.setServiceStatus(ServiceStatus.STOPPED);
    }

    @Override
    public Map<String, String> getServiceProps() {
        Map<String, String> props = super.getServiceProps();
        if (workManager != null) {
            props.put(ServiceResultProcessor.STATUS_KEY,
                      workManager.getProcessingStatus());
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
                                                             reportContentId);

        opts.verify();
        return opts;
    }

    public void setContentStore(ContentStore contentStore) {
        this.contentStore = contentStore;
    }

    public void setDuraStoreHost(String duraStoreHost) {
        this.contentStore = null;
        this.duraStoreHost = duraStoreHost;
    }

    public void setDuraStorePort(String duraStorePort) {
        this.contentStore = null;
        this.duraStorePort = duraStorePort;
    }

    public void setDuraStoreContext(String duraStoreContext) {
        this.contentStore = null;
        this.duraStoreContext = duraStoreContext;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setHashApproach(String hashApproach) {
        this.hashApproach = hashApproach;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setFailFast(String failFast) {
        isFailFast = failFast;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public void setProvidedListingSpaceIdA(String providedListingSpaceIdA) {
        this.providedListingSpaceIdA = providedListingSpaceIdA;
    }

    public void setProvidedListingSpaceIdB(String providedListingSpaceIdB) {
        this.providedListingSpaceIdB = providedListingSpaceIdB;
    }

    public void setProvidedListingContentIdA(String providedListingContentIdA) {
        this.providedListingContentIdA = providedListingContentIdA;
    }

    public void setProvidedListingContentIdB(String providedListingContentIdB) {
        this.providedListingContentIdB = providedListingContentIdB;
    }

    public void setTargetSpaceId(String targetSpaceId) {
        this.targetSpaceId = targetSpaceId;
    }

    public void setOutputSpaceId(String outputSpaceId) {
        this.outputSpaceId = outputSpaceId;
    }

    public void setOutputContentId(String outputContentId) {
        this.outputContentId = outputContentId;
    }

    public void setReportContentId(String reportContentId) {
        this.reportContentId = reportContentId;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        log.info("FixityService: setText (" + text + ")");
        this.text = text;
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

}
