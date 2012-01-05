/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonfixity.postprocessing;

import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReducePostJobWorker;
import org.duracloud.services.fixity.FixityService;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.results.ServiceResultProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * @author Andrew Woods
 *         Date: Jan 12, 2011
 */
public class VerifyHashesPostJobWorker extends BaseAmazonMapReducePostJobWorker {

    private final Logger log = LoggerFactory.getLogger(VerifyHashesPostJobWorker.class);

    private FixityService fixityService;

    public VerifyHashesPostJobWorker(AmazonMapReduceJobWorker predecessor,
                                     ContentStore contentStore,
                                     FixityService fixityService,
                                     String workDir,
                                     String duraStoreHost,
                                     String duraStorePort,
                                     String duraStoreContext,
                                     String storeId,
                                     String username,
                                     String password,
                                     String providedListingContentIdA,
                                     String providedListingSpaceIdB,
                                     String providedListingContentIdB,
                                     String outputSpaceId,
                                     String reportContentId) {
        super(predecessor);
        log.debug("constructing");

        this.fixityService = fixityService;
        fixityService.setDuraStoreHost(duraStoreHost);
        fixityService.setDuraStorePort(duraStorePort);
        fixityService.setDuraStoreContext(duraStoreContext);
        fixityService.setStoreId(storeId);
        fixityService.setUsername(username);
        fixityService.setPassword(password);

        String hashApproach = null;
        String mode = FixityServiceOptions.Mode.COMPARE.name();
        String outputContentId = null;

        fixityService.setProvidedListingSpaceIdA(outputSpaceId);
        fixityService.setProvidedListingContentIdA(providedListingContentIdA);
        fixityService.setProvidedListingSpaceIdB(providedListingSpaceIdB);
        fixityService.setProvidedListingContentIdB(providedListingContentIdB);

        fixityService.setMode(mode);
        fixityService.setHashApproach(hashApproach);

        fixityService.setOutputSpaceId(outputSpaceId);
        fixityService.setOutputContentId(outputContentId);
        fixityService.setReportContentId(reportContentId);

        fixityService.setServiceWorkDir(workDir);

        StringBuilder sb = new StringBuilder();
        sb.append("constructed VerifyHashesPostJobWorker with: \n");
        sb.append("\n\tworkDir : ");
        sb.append(workDir);
        sb.append("\n\tduraStoreHost : ");
        sb.append(duraStoreHost);
        sb.append("\n\tduraStorePort : ");
        sb.append(duraStorePort);
        sb.append("\n\tduraStoreContext : ");
        sb.append(duraStoreContext);
        sb.append("\n\tstoreId : ");
        sb.append(storeId);
        sb.append("\n\tusername : ");
        sb.append(username);
        sb.append("\n\tmode : ");
        sb.append(mode);
        sb.append("\n\tprovidedListingSpaceIdA : ");
        sb.append(outputSpaceId);
        sb.append("\n\tprovidedListingContentIdA : ");
        sb.append(providedListingContentIdA);
        sb.append("\n\tprovidedListingSpaceIdB : ");
        sb.append(providedListingSpaceIdB);
        sb.append("\n\tprovidedListingContentIdB : ");
        sb.append(providedListingContentIdB);
        sb.append("\n\toutputSpaceId : ");
        sb.append(outputSpaceId);
        sb.append("\n\treportContentId : ");
        sb.append(reportContentId);
        log.debug(sb.toString());
    }

    @Override
    protected void doWork() {
        log.debug("VerifyHashesPostJobWorker.doWork()");
        startFixityService();
    }

    private void startFixityService() {
        try {
            fixityService.start();
            while (stillRunning(fixityService)) {

                log.debug("waiting for fixityService to complete.");
                sleep(20000);
            }

        } catch (Exception e) {
            log.error("Error performing verification task", e);
            throw new DuraCloudRuntimeException(e);
        }
    }

    private boolean stillRunning(FixityService fixityService) {
        Map<String, String> props = fixityService.getServiceProps();
        String status = props.get(ServiceResultProcessor.STATUS_KEY);
        if (null == status) {
            return true;
        }

        ServiceResultListener.StatusMsg msg = new ServiceResultListener.StatusMsg(
            status);

        String phase = msg.getPhase();
        if (null == phase || !phase.equals(FixityService.PHASE_COMPARE)) {
            return true;
        }

        ServiceResultListener.State state = msg.getState();
        if (null == state ||
            !state.equals(ServiceResultListener.State.COMPLETE)) {
            return true;
        }

        return false;
    }

    @Override
    public void shutdown() {
        try {
            super.shutdown();
            fixityService.stop();
        } catch (Exception e) {
            log.warn("Error shutting down fixity-post-processor.", e);
        }
    }

}
