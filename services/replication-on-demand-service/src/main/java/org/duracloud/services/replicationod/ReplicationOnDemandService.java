/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.replicationod;

import org.duracloud.services.ComputeService;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceService;
import org.duracloud.services.amazonmapreduce.postprocessing.MimePostJobWorker;
import org.duracloud.services.amazonmapreduce.postprocessing.HeaderPostJobWorker;
import org.duracloud.services.amazonmapreduce.postprocessing.MultiPostJobWorker;
import org.duracloud.services.amazonmapreduce.postprocessing.SimplePassFailPostJobWorker;
import org.duracloud.storage.domain.HadoopTypes;
import org.duracloud.common.util.DateUtil;

import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * Service which replicates content from one space to another.
 *
 * @author Bill Branan
 *         Date: Sept 29, 2010
 */
public class ReplicationOnDemandService extends BaseAmazonMapReduceService implements ComputeService, ManagedService {

    private final Logger log =
        LoggerFactory.getLogger(ReplicationOnDemandService.class);

    private static final String DEFAULT_REP_STORE_ID = "0";
    private static final String DEFAULT_REP_SPACE_ID = "replication-space";
    
    private String repStoreId;
    private String repSpaceId;

    private ReplicationOnDemandJobWorker worker;
    private AmazonMapReduceJobWorker postWorker;
    
    @Override
    protected AmazonMapReduceJobWorker getJobWorker() {
        if (null == worker) {
            worker = new ReplicationOnDemandJobWorker(getContentStore(),
                                                      getWorkSpaceId(),
                                                      collectTaskParams(),
                                                      getServiceWorkDir());
        }
        return worker;
    }

    @Override
    protected AmazonMapReduceJobWorker getPostJobWorker() {
        if (null == postWorker) {
            String header = "result" + DELIM + "input-file-path" + DELIM +
                "result-file-path" + DELIM + "duplication-result" + DELIM +
                "source-file-bytes" + DELIM + "duplication-attempts" + DELIM +
                "date";

            String preName = "duplicate-on-demand/duplicate-results";
            String date = "-" + DateUtil.nowMid();
            String postName = ".tsv";

            String newContentId = preName + date + postName;
            super.setReportId(getDestSpaceId(), newContentId);
            AmazonMapReduceJobWorker headerWorker = new HeaderPostJobWorker(
                getJobWorker(),
                getContentStore(),
                getServiceWorkDir(),
                getDestSpaceId(),
                preName + postName,
                newContentId,
                header);

            AmazonMapReduceJobWorker mimeWorker = new MimePostJobWorker(headerWorker,
                                               getContentStore(),
                                               getDestSpaceId());

            AmazonMapReduceJobWorker passFailWorker = new SimplePassFailPostJobWorker(
                mimeWorker,
                getContentStore(),
                getServiceWorkDir(),
                getDestSpaceId(),
                newContentId);

            AmazonMapReduceJobWorker[] postWorkers =
                new AmazonMapReduceJobWorker[]{headerWorker,
                                               mimeWorker,
                                               passFailWorker};

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
        return HadoopTypes.JOB_TYPES.REP_ON_DEMAND.name();
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

    @Override
    protected Map<String, String> collectTaskParams() {
        Map<String, String> taskParams = super.collectTaskParams();

        taskParams.put(TASK_PARAMS.REP_STORE_ID.name(), getRepStoreId());
        taskParams.put(TASK_PARAMS.REP_SPACE_ID.name(), getRepSpaceId());
        taskParams.put(TASK_PARAMS.DC_HOST.name(), getDuraStoreHost());
        taskParams.put(TASK_PARAMS.DC_PORT.name(), getDuraStorePort());
        taskParams.put(TASK_PARAMS.DC_CONTEXT.name(), getDuraStoreContext());
        taskParams.put(TASK_PARAMS.DC_USERNAME.name(), getUsername());
        taskParams.put(TASK_PARAMS.DC_PASSWORD.name(), getPassword());       

        return taskParams;
    }

    public String getRepStoreId() {
        return repStoreId;
    }

    public void setRepStoreId(String repStoreId) {
        if(repStoreId != null && !repStoreId.equals("")) {
            this.repStoreId = repStoreId;
        } else {
            log("Attempt made to set repStoreId to to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_REP_STORE_ID);
            this.repStoreId = DEFAULT_REP_STORE_ID;
        }
    }

    public String getRepSpaceId() {
        if(DEFAULT_REP_SPACE_ID.equals(repSpaceId)) {
            repSpaceId = getSourceSpaceId();
        }
        return repSpaceId;
    }

    public void setRepSpaceId(String repSpaceId) {
        if(repSpaceId != null && !repSpaceId.equals("")) {
            this.repSpaceId = repSpaceId;
        } else {
            log("Attempt made to set repSpaceId to to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_REP_SPACE_ID);
            this.repSpaceId = DEFAULT_REP_SPACE_ID;
        }
    }

    private void log(String logMsg) {
        log.warn(logMsg);
    }
}