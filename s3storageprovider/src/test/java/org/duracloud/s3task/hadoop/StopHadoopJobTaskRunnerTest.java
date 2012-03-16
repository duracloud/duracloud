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
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: Aug 24, 2010
 */
public class StopHadoopJobTaskRunnerTest {

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

        mock.terminateJobFlows(EasyMock.isA(TerminateJobFlowsRequest.class));
        EasyMock.expectLastCall().times(1);

        EasyMock.replay(mock);
        return mock;
    }

    @Test
    public void testPerformTask() {
        StopHadoopJobTaskRunner runner = new StopHadoopJobTaskRunner(emrClient);

        String jobId = "1";
        runner.performTask(jobId);
    }
}
