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
import org.duracloud.services.amazonfixity.postprocessing.FixityPassFailPostJobWorker;
import org.duracloud.services.amazonfixity.postprocessing.VerifyHashesPostJobWorker;
import org.duracloud.services.amazonfixity.postprocessing.WrapperPostJobWorker;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceService;
import org.duracloud.services.amazonmapreduce.postprocessing.HeaderPostJobWorker;
import org.duracloud.services.amazonmapreduce.postprocessing.MimePostJobWorker;
import org.duracloud.services.amazonmapreduce.postprocessing.MultiPostJobWorker;
import org.duracloud.services.amazonmapreduce.postprocessing.DeletePostJobWorker;
import org.duracloud.services.fixity.FixityService;
import org.duracloud.storage.domain.HadoopTypes;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
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

    private static final String PREFIX = "bit-integrity-bulk/bit-integrity-";

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
            // FIXME: this propertiesMd5ContentId value is currently hard-coded
            // into the FixityPropertiesOutputFormat.java.
            String date = DateUtil.nowMid();
            String genMd5ContentId = PREFIX + "results.tsv";
            String newContentId = PREFIX + "results-" + date + ".tsv";
            String propertiesMd5ContentId = PREFIX + "properties-results.tsv";
            String newMetadatContentId = PREFIX + "properties-results-" + date + ".tsv";
            String header = "space-id" + DELIM + "content-id" + DELIM + "hash";
            List<String> deleteFiles = new ArrayList<String>();
            deleteFiles.add(newContentId);
            deleteFiles.add(newMetadatContentId);

            AmazonMapReduceJobWorker headerWorker = new HeaderPostJobWorker(
                getJobWorker(),
                getContentStore(),
                getServiceWorkDir(),
                getDestSpaceId(),
                genMd5ContentId,
                newContentId,
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
                AmazonMapReduceJobWorker propertiesWorker = new AmazonFixityPropertiesJobWorker(
                    getContentStore(),
                    getWorkSpaceId(),
                    collectTaskParamsPostProcessor(),
                    getServiceWorkDir());

                wrapperWorker = new WrapperPostJobWorker(headerWorker,
                                                         propertiesWorker);

                headerWorker2 = new HeaderPostJobWorker(wrapperWorker,
                                                        getContentStore(),
                                                        getServiceWorkDir(),
                                                        getDestSpaceId(),
                                                        propertiesMd5ContentId,
                                                        newMetadatContentId,
                                                        header);


                providedListingSpaceIdB = getDestSpaceId();
                providedListingContentIdB = newMetadatContentId;
                previousWorker = headerWorker2;

            } else {
                log.info("second hadoop worker not added: " + mode);
            }

            String reportContentId = PREFIX + "report-" + date + ".tsv";
            super.setReportId(getDestSpaceId(), reportContentId);

            String errorReportContentId = PREFIX + "report-" + date + "-errors.tsv";
            super.setErrorReportId(getDestSpaceId(), errorReportContentId);

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
                newContentId,
                providedListingSpaceIdB,
                providedListingContentIdB,
                getDestSpaceId(),
                reportContentId);

            AmazonMapReduceJobWorker mimeWorker = new MimePostJobWorker(
                verifyWorker,
                getContentStore(),
                getDestSpaceId());

            AmazonMapReduceJobWorker deleteWorker = new DeletePostJobWorker(
                mimeWorker,
                getContentStore(),
                getDestSpaceId(),
                deleteFiles);

            AmazonMapReduceJobWorker passFailWorker = new FixityPassFailPostJobWorker(
                deleteWorker,
                getContentStore(),
                getServiceWorkDir(),
                getDestSpaceId(),
                reportContentId,
                errorReportContentId);

            AmazonMapReduceJobWorker[] postWorkers;
            if (null != wrapperWorker && null != headerWorker2) {
                postWorkers = new AmazonMapReduceJobWorker[]{headerWorker,
                                                             wrapperWorker,
                                                             headerWorker2,
                                                             verifyWorker,
                                                             mimeWorker,
                                                             deleteWorker,
                                                             passFailWorker};
            } else {
                postWorkers = new AmazonMapReduceJobWorker[]{headerWorker,
                                                             verifyWorker,
                                                             mimeWorker,
                                                             deleteWorker,
                                                             passFailWorker};
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
        params.put(JOB_TYPE.name(), AMAZON_FIXITY_PROPERTIES.name());
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
