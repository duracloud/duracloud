/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.hadoop;

import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.TerminateJobFlowsRequest;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.storage.provider.TaskRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Aug 24, 2010
 */
public class StopHadoopJobTaskRunner implements TaskRunner {

    private static final String TASK_NAME = "stop-hadoop-job";
    
    private AmazonElasticMapReduceClient emrClient;

    public StopHadoopJobTaskRunner(AmazonElasticMapReduceClient emrClient) {
        this.emrClient = emrClient;
    }

    public String getName() {
        return TASK_NAME;
    }

    public String performTask(String taskParameters) {

        String jobFlowId = taskParameters;
        if(jobFlowId == null) {
            throw new RuntimeException("Job ID is required to stop hadoop job");
        }

        TerminateJobFlowsRequest terminateRequest =
            new TerminateJobFlowsRequest().withJobFlowIds(jobFlowId);
        emrClient.terminateJobFlows(terminateRequest);

        Map<String, String> returnInfo = new HashMap<String, String>();
        returnInfo.put("results", "success");
        String toReturn = SerializationUtil.serializeMap(returnInfo);
        return toReturn;
    }
}
