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
import org.duracloud.common.util.DateUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReducePostJobWorker;
import org.duracloud.services.fixity.FixityService;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.results.ServiceResultProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.duracloud.services.fixity.domain.FixityServiceOptions.HashApproach;

/**
 * @author Andrew Woods
 *         Date: Jan 12, 2011
 */
public class VerifyHashesPostJobWorker extends BaseAmazonMapReducePostJobWorker {

    private final Logger log = LoggerFactory.getLogger(VerifyHashesPostJobWorker.class);

    private FixityService fixityService;

    private ContentStore contentStore;

    private String outputSpaceId;
    private String outputContentId; // generated, interim hash listing

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
                                     String mode,
                                     String providedListingContentIdA,
                                     String providedListingSpaceIdB,
                                     String providedListingContentIdB,
                                     String outputSpaceId,
                                     String reportContentId) {
        super(predecessor);
        log.debug("constructing");

        this.contentStore = contentStore;
        this.outputSpaceId = outputSpaceId;

        this.fixityService = fixityService;
        fixityService.setDuraStoreHost(duraStoreHost);
        fixityService.setDuraStorePort(duraStorePort);
        fixityService.setDuraStoreContext(duraStoreContext);
        fixityService.setStoreId(storeId);
        fixityService.setUsername(username);
        fixityService.setPassword(password);

        String hashApproach;
        if (mode != null && mode.equals("compare")) {
            mode = FixityServiceOptions.Mode.COMPARE.name();

            hashApproach = null;
            this.outputContentId = null;

        } else {
            mode = FixityServiceOptions.Mode.ALL_IN_ONE_LIST.name();
            hashApproach = HashApproach.STORED.name();

            String now = DateUtil.nowShort();
            this.outputContentId = "bitIntegrity-bulk/gen-hash-" + now + ".csv";
        }

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

        if (null != outputContentId) {
            removeArtifacts();
        }
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

    private void removeArtifacts() {
        try {
            contentStore.deleteContent(outputSpaceId, outputContentId);

        } catch (ContentStoreException e) {
            log.error("Error cleaning artifacts: " + outputContentId, e);
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
        if (null == msg) {
            return true;
        }

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
}
