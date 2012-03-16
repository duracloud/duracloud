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
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: Aug 24, 2010
 */
public class DescribeHadoopJobTaskRunnerTest {

    private AmazonElasticMapReduceClient emrClient;

    @Before
    public void setUp() throws Exception {
        emrClient = createEMRClientMock();
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(emrClient);
        emrClient = null;
    }

    private AmazonElasticMapReduceClient createEMRClientMock() {
        AmazonElasticMapReduceClient mock =
            EasyMock.createMock(AmazonElasticMapReduceClient.class);

        JobFlowExecutionStatusDetail jobStatus =
            new JobFlowExecutionStatusDetail();
        jobStatus.setCreationDateTime(new Date());
        jobStatus.setEndDateTime(new Date());
        jobStatus.setState("RUNNING");

        JobFlowDetail jobDetail = new JobFlowDetail();
        jobDetail.setExecutionStatusDetail(jobStatus);

        List<JobFlowDetail> jobDetails = new ArrayList<JobFlowDetail>();
        jobDetails.add(jobDetail);

        DescribeJobFlowsResult result = new DescribeJobFlowsResult();
        result.setJobFlows(jobDetails);

        EasyMock
            .expect(mock.describeJobFlows(
                EasyMock.isA(DescribeJobFlowsRequest.class)))
            .andReturn(result)
            .times(1);

        EasyMock.replay(mock);
        return mock;
    }

    @Test
    public void testPerformTask() {
        DescribeHadoopJobTaskRunner runner =
            new DescribeHadoopJobTaskRunner(emrClient);

        String jobId = "1";
        String result = runner.performTask(jobId);
        assertNotNull(result);

        Map<String, String> resultMap =
            SerializationUtil.deserializeMap(result);
        assertNotNull(resultMap);

        assertNotNull(resultMap.get("Job Started"));
        assertNotNull(resultMap.get("Job Ended"));
        assertNotNull(resultMap.get("Job State"));
    }    

}
