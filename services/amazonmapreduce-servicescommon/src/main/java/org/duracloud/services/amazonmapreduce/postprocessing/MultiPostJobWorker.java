/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.postprocessing;

import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReducePostJobWorker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is designed to run after the completion of a hadoop job.
 * It sequentially executes the provided list of workers upon the completion
 * of the provided 'predecessor' worker.
 *
 * @author Andrew Woods
 *         Date: Oct 1, 2010
 */
public class MultiPostJobWorker extends BaseAmazonMapReducePostJobWorker {

    private List<AmazonMapReduceJobWorker> workers;

    public MultiPostJobWorker(AmazonMapReduceJobWorker predecessor,
                              AmazonMapReduceJobWorker... workers) {
        super(predecessor);
        this.workers = Arrays.asList(workers);
    }

    public MultiPostJobWorker(AmazonMapReduceJobWorker predecessor,
                              long sleepMillis,
                              AmazonMapReduceJobWorker... workers) {
        super(predecessor, sleepMillis);
        this.workers = Arrays.asList(workers);
    }

    @Override
    protected void doWork() {
        for (AmazonMapReduceJobWorker worker : workers) {
            worker.run();
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        for (AmazonMapReduceJobWorker worker : workers) {
            worker.shutdown();
        }
    }
    
    @Override
    public JobStatus getJobStatus() {
        AmazonMapReduceJobWorker worker = getActiveWorker();
        if (null != worker) {
            return worker.getJobStatus();
        }
        return super.getJobStatus();
    }

    @Override
    public Map<String, String> getJobDetailsMap() {
        AmazonMapReduceJobWorker worker = getActiveWorker();
        if (null != worker) {
            return worker.getJobDetailsMap();
        }
        return super.getJobDetailsMap();
    }

    @Override
    public String getJobId() {
        AmazonMapReduceJobWorker worker = getActiveWorker();
        if (null != worker) {
            return worker.getJobId();
        }
        return super.getJobId();
    }

    private AmazonMapReduceJobWorker getActiveWorker() {
        for (AmazonMapReduceJobWorker worker : workers) {
            if (worker.getJobStatus() == JobStatus.POST_PROCESSING) {
                return worker;
            }
        }
        return null;
    }

}
