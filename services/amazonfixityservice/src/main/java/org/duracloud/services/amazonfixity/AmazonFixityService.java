/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonfixity;

import org.duracloud.error.ContentStoreException;
import org.duracloud.services.ComputeService;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceService;
import org.duracloud.services.amazonmapreduce.postprocessing.HeaderPostJobWorker;
import org.duracloud.services.amazonmapreduce.postprocessing.MimePostJobWorker;
import org.duracloud.services.amazonmapreduce.postprocessing.MultiPostJobWorker;
import org.duracloud.storage.domain.HadoopTypes;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.INSTANCES.LARGE;
import static org.duracloud.storage.domain.HadoopTypes.INSTANCES.XLARGE;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS.*;

/**
 * This service runs the fixity service in the Amazon elastic map reduce framework.
 *
 * @author Andrew Woods
 *         Date: Sept 21, 2010
 */
public class AmazonFixityService extends BaseAmazonMapReduceService implements ManagedService, ComputeService {

    private final Logger log = LoggerFactory.getLogger(AmazonFixityService.class);

    private AmazonMapReduceJobWorker worker;
    private AmazonMapReduceJobWorker postWorker;

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
            // FIXME: this contentId value is currently hard-coded into the
            // FixityOutputFormat.
            // The dynamic population of this value could be passed in as a
            // service parameter, but the FixityOutputFormat is passed into
            // hadoop as a class, not an object.
            String contentId = "bitIntegrity-bulk/bitIntegrity-results.csv";
            String header = "space-id,content-id,hash";
            AmazonMapReduceJobWorker headerWorker = new HeaderPostJobWorker(
                getJobWorker(),
                getContentStore(),
                getServiceWorkDir(),
                getDestSpaceId(),
                contentId,
                header);

            AmazonMapReduceJobWorker mimeWorker = new MimePostJobWorker(
                headerWorker,
                getContentStore(),
                getDestSpaceId());

            postWorker = new MultiPostJobWorker(getJobWorker(),
                                                headerWorker,
                                                mimeWorker);
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
        return HadoopTypes.JOB_TYPES.AMAZON_FIXITY.name();
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

}
