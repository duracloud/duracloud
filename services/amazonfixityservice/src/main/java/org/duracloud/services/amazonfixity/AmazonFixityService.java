/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonfixity;

import org.duracloud.common.util.DateUtil;
import org.duracloud.services.ComputeService;
import org.duracloud.services.amazonfixity.postprocessing.VerifyHashesPostJobWorker;
import org.duracloud.services.amazonfixity.postprocessing.WrapperPostJobWorker;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceService;
import org.duracloud.services.amazonmapreduce.postprocessing.HeaderPostJobWorker;
import org.duracloud.services.amazonmapreduce.postprocessing.MimePostJobWorker;
import org.duracloud.services.amazonmapreduce.postprocessing.MultiPostJobWorker;
import org.duracloud.services.fixity.FixityService;
import org.duracloud.storage.domain.HadoopTypes;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.JOB_TYPES.*;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS.*;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS.MAPPERS_PER_INSTANCE;

/**
 * This service runs the fixity service in the Amazon elastic map reduce framework.
 *
 * @author Andrew Woods
 *         Date: Sept 21, 2010
 */
public class AmazonFixityService extends BaseAmazonMapReduceService implements ManagedService, ComputeService {

    private final Logger log = LoggerFactory.getLogger(AmazonFixityService.class);

    private static final String PREFIX = "bitIntegrity-bulk/bitIntegrity-";

    private AmazonMapReduceJobWorker worker;
    private AmazonMapReduceJobWorker postWorker;

    private String providedListingSpaceIdB;
    private String providedListingContentIdB;
    private String mode;

    @Override
    protected AmazonMapReduceJobWorker getJobWorker() {
        if (null == worker) {
            worker = new AmazonFixityJobWorker(getContentStore(),
                                               getWorkSpaceId(),
                                               collectTaskParams(),
                                               getServiceWorkDir());
        }
        return worker;
    }

    @Override
    protected AmazonMapReduceJobWorker getPostJobWorker() {
        if (null == postWorker) {
            // FIXME: this genMd5ContentId value is currently hard-coded into the
            // FixityOutputFormat.
            // The dynamic population of this value could be passed in as a
            // service parameter, but the FixityOutputFormat is passed into
            // hadoop as a class, not an object.
            // FIXME: this metadataMd5ContentId value is currently hard-coded
            // into the FixityMetadataOutputFormat.java.
            String genMd5ContentId = PREFIX + "results.csv";
            String metadataMd5ContentId = PREFIX + "metadata-results.csv";
            String header = "space-id,content-id,hash";

            AmazonMapReduceJobWorker headerWorker = new HeaderPostJobWorker(
                getJobWorker(),
                getContentStore(),
                getServiceWorkDir(),
                getDestSpaceId(),
                genMd5ContentId,
                header);

            AmazonMapReduceJobWorker previousWorker = headerWorker;
            AmazonMapReduceJobWorker wrapperWorker = null;
            AmazonMapReduceJobWorker headerWorker2 = null;
            String mode = getMode();
            String providedListingSpaceIdB = getProvidedListingSpaceIdB();
            String providedListingContentIdB = getProvidedListingContentIdB();

            // This string is defined in AmazonFixityServiceInfo.ModeType
            String modeForGeneratedList = "all-in-one-for-list";
            if (null != mode && modeForGeneratedList.equals(mode)) {
                log.info("Adding second hadoop worker.");
                AmazonMapReduceJobWorker metadataWorker = new AmazonFixityMetadataJobWorker(
                    getContentStore(),
                    getWorkSpaceId(),
                    collectTaskParamsPostProcessor(),
                    getServiceWorkDir());

                providedListingSpaceIdB = getDestSpaceId();
                providedListingContentIdB = metadataMd5ContentId;

                wrapperWorker = new WrapperPostJobWorker(headerWorker,
                                                         metadataWorker);

                headerWorker2 = new HeaderPostJobWorker(wrapperWorker,
                                                        getContentStore(),
                                                        getServiceWorkDir(),
                                                        getDestSpaceId(),
                                                        metadataMd5ContentId,
                                                        header);

                previousWorker = headerWorker2;

            } else {
                log.info("second hadoop worker not added: " + mode);
            }

            String prefix = PREFIX + "report-";
            String reportContentId = prefix + DateUtil.nowShort() + ".csv";
            VerifyHashesPostJobWorker verifyWorker = new VerifyHashesPostJobWorker(
                previousWorker,
                getContentStore(),
                new FixityService(),
                getServiceWorkDir(),
                getDuraStoreHost(),
                getDuraStorePort(),
                getDuraStoreContext(),
                getContentStore().getStoreId(),
                getUsername(),
                getPassword(),
                genMd5ContentId,
                providedListingSpaceIdB,
                providedListingContentIdB,
                getDestSpaceId(),
                reportContentId);

            AmazonMapReduceJobWorker mimeWorker = new MimePostJobWorker(
                verifyWorker,
                getContentStore(),
                getDestSpaceId());

            AmazonMapReduceJobWorker[] postWorkers;
            if (null != wrapperWorker && null != headerWorker2) {
                postWorkers = new AmazonMapReduceJobWorker[]{headerWorker,
                                                             wrapperWorker,
                                                             headerWorker2,
                                                             verifyWorker,
                                                             mimeWorker};
            } else {
                postWorkers = new AmazonMapReduceJobWorker[]{headerWorker,
                                                             verifyWorker,
                                                             mimeWorker};
            }
            postWorker = new MultiPostJobWorker(getJobWorker(), postWorkers);

        }
        return postWorker;
    }

    @Override
    public void start() throws Exception {
        createDestSpace();

        super.start();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        worker = null;
        postWorker = null;
    }

    @Override
    protected String getJobType() {
        return AMAZON_FIXITY.name();
    }

    private Map<String, String> collectTaskParamsPostProcessor() {
        Map<String, String> params = super.collectTaskParams();

        String smallInstance = HadoopTypes.INSTANCES.SMALL.getId();
        String largeInstance = HadoopTypes.INSTANCES.LARGE.getId();
        params.put(INSTANCE_TYPE.name(), largeInstance);
        params.put(MAPPERS_PER_INSTANCE.name(), getNumMappers(smallInstance));
        params.put(JOB_TYPE.name(), AMAZON_FIXITY_METADATA.name());
        return params;
    }

    @Override
    protected String getNumMappers(String instanceType) {
        String mappers = "2";
        if (HadoopTypes.INSTANCES.LARGE.getId().equals(instanceType)) {
            mappers = "4";

        } else if (HadoopTypes.INSTANCES.XLARGE.getId().equals(instanceType)) {
            mappers = "8";
        }
        return mappers;
    }

    public String getProvidedListingSpaceIdB() {
        return providedListingSpaceIdB;
    }

    public void setProvidedListingSpaceIdB(String providedListingSpaceIdB) {
        this.providedListingSpaceIdB = providedListingSpaceIdB;
    }

    public String getProvidedListingContentIdB() {
        return providedListingContentIdB;
    }

    public void setProvidedListingContentIdB(String providedListingContentIdB) {
        this.providedListingContentIdB = providedListingContentIdB;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
