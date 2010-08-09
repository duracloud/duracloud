/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import java.io.File;
import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.HashFinderResult;
import org.duracloud.services.fixity.results.ServiceResultProcessor;
import org.duracloud.services.fixity.worker.PatientServiceWorkManager;
import org.duracloud.services.fixity.worker.ServiceWorkManager;
import org.duracloud.services.fixity.worker.ServiceWorkerFactory;
import org.duracloud.services.fixity.worker.ServiceWorkload;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private String duraStoreHost;
    private String duraStorePort;
    private String duraStoreContext;
    private String username;
    private String password;

    private String mode;
    private String hashApproach;
    private String salt;
    private Boolean isFailFast;
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
        log.info("Starting Fixity Service as '" + username + "': " + threads +
            " worker threads");

        ContentStore contentStore = getContentStore();
        FixityServiceOptions serviceOptions = getServiceOptions();

        CountDownLatch doneHashing = new CountDownLatch(1);
        if (serviceOptions.needsToHash()) {
            startHashing(contentStore,
                         serviceOptions,
                         doneHashing);
        } else {
            doneHashing.countDown();
        }

        if (serviceOptions.needsToCompare()) {
            startComparing(contentStore,
                           serviceOptions,
                           doneHashing);
        }
    }

    private void startHashing(ContentStore contentStore,
                              FixityServiceOptions serviceOptions,
                              CountDownLatch doneHashing) {
        File workDir = new File(getServiceWorkDir());

        String resultHeader = HashFinderResult.HEADER;
        ServiceResultProcessor resultListener = new ServiceResultProcessor(
            contentStore,
            outputSpaceId,
            outputContentId,
            resultHeader,
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

        this.setServiceStatus(ServiceStatus.STARTED);

    }

    private void startComparing(ContentStore contentStore,
                                FixityServiceOptions serviceOptions,
                                CountDownLatch doneHashing) {
        ServiceWorkload workload = new HashVerifierWorkload(serviceOptions,
                                                          contentStore);

        ServiceWorkerFactory workerFactory = new HashVerifierWorkerFactory(
            serviceOptions,
            contentStore);

        workManager = new PatientServiceWorkManager(workload,
                                                    workerFactory,
                                                    doneHashing);

        workManager.start();

        this.setServiceStatus(ServiceStatus.STARTED);
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
        ContentStoreManager storeManager = new ContentStoreManagerImpl(
            duraStoreHost,
            duraStorePort,
            duraStoreContext);
        storeManager.login(new Credential(username, password));
        return storeManager.getContentStore(storeId);
    }

    private FixityServiceOptions getServiceOptions() {
        return new FixityServiceOptions(mode,
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
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        log.info("FixityService: setText (" + text + ")");
        this.text = text;
    }

}
