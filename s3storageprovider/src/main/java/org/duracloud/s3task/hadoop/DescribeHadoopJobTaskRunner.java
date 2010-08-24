/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.hadoop;

import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.DescribeJobFlowsRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeJobFlowsResult;
import com.amazonaws.services.elasticmapreduce.model.JobFlowDetail;
import com.amazonaws.services.elasticmapreduce.model.JobFlowExecutionStatusDetail;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.storage.provider.TaskRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Aug 24, 2010
 */
public class DescribeHadoopJobTaskRunner implements TaskRunner {

    private static final String TASK_NAME = "describe-hadoop-job";

    private AmazonElasticMapReduceClient emrClient;

    public DescribeHadoopJobTaskRunner(AmazonElasticMapReduceClient emrClient) {
        this.emrClient = emrClient;
    }

    public String getName() {
        return TASK_NAME;
    }

    public String performTask(String taskParameters) {

        String jobFlowId = taskParameters;
        if(jobFlowId == null) {
            throw new RuntimeException("Job ID is required to describe " +
                                       "hadoop job");
        }

        DescribeJobFlowsRequest describeRequest =
            new DescribeJobFlowsRequest().withJobFlowIds(jobFlowId);
        DescribeJobFlowsResult describeResult =
            emrClient.describeJobFlows(describeRequest);

        Map<String, String> jobDetail = new HashMap<String, String>();

        List<JobFlowDetail> jobFlows = describeResult.getJobFlows();
        if(jobFlows != null && jobFlows.size() > 0) {
            JobFlowDetail jobFlow = jobFlows.get(0);
            JobFlowExecutionStatusDetail jobFlowExecution =
                jobFlow.getExecutionStatusDetail();

            SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd' at 'hh:mm:ss a z");

            Date jobStarted = jobFlowExecution.getCreationDateTime();
            if(jobStarted != null) {
                jobDetail.put("Job Started", dateFormat.format(jobStarted));
            }

            Date jobEnded = jobFlowExecution.getEndDateTime();
            if(jobEnded != null) {
                jobDetail.put("Job Ended", dateFormat.format(jobEnded));
            }

            String jobState = jobFlowExecution.getState();
            if(jobState != null) {
                jobDetail.put("Job State", jobState);
            }
        }

        String toReturn = SerializationUtil.serializeMap(jobDetail);
        return toReturn;
    }
}
