/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.hadoop;

import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.domain.HadoopTypes;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * @author: Bill Branan
 * Date: Aug 23, 2010
 */
public class RunHadoopJobTaskRunnerTest {

    private S3StorageProvider s3Provider;
    private AmazonS3Client s3Client;
    private AmazonElasticMapReduceClient emrClient;

    @Before
    public void setUp() throws Exception {
        s3Provider = createS3ProviderMock();
        s3Client = createS3ClientMock();
        emrClient = createEMRClientMock();
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(s3Provider);
        s3Provider = null;

        EasyMock.verify(s3Client);
        s3Client = null;

        EasyMock.verify(emrClient);
        emrClient = null;
    }

    private S3StorageProvider createS3ProviderMock() {
        S3StorageProvider mock = EasyMock.createMock(S3StorageProvider.class);

        EasyMock
            .expect(mock.getBucketName(EasyMock.isA(String.class)))
            .andReturn("bucket-name")
            .times(3);

        EasyMock.replay(mock);
        return mock;
    }

    private AmazonS3Client createS3ClientMock() {
        AmazonS3Client mock = EasyMock.createMock(AmazonS3Client.class);

        EasyMock
            .expect(mock.doesBucketExist(EasyMock.isA(String.class)))
            .andReturn(true)
            .times(3);

        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setHeader("key", "value");
        EasyMock
            .expect(mock.getObjectMetadata(EasyMock.isA(String.class),
                                           EasyMock.isA(String.class)))
            .andReturn(objMeta)
            .times(2);

        EasyMock.replay(mock);
        return mock;
    }

    private AmazonElasticMapReduceClient createEMRClientMock() {
        AmazonElasticMapReduceClient mock =
            EasyMock.createMock(AmazonElasticMapReduceClient.class);

        RunJobFlowResult result = new RunJobFlowResult();
        result.setJobFlowId("1");
        EasyMock
            .expect(mock.runJobFlow(EasyMock.isA(RunJobFlowRequest.class)))
            .andReturn(result)
            .times(1);

        EasyMock.replay(mock);        
        return mock;
    }

    @Test
    public void testPerformTask() {
        RunHadoopJobTaskRunner runner =
            new RunHadoopJobTaskRunner(s3Provider, s3Client, emrClient);

        Map<String, String> taskParams = new HashMap<String, String>();
        String params = SerializationUtil.serializeMap(taskParams);

        try {
            runner.performTask(params);
            fail("Exception expected performing task with no params");
        } catch(RuntimeException expected) {
            assertNotNull(expected);
        }

        taskParams.put(TASK_PARAMS.JOB_TYPE.name(), "bulk-image-conversion");
        taskParams.put(TASK_PARAMS.WORKSPACE_ID.name(), "work");
        taskParams.put(TASK_PARAMS.SOURCE_SPACE_ID.name(), "source");
        taskParams.put(TASK_PARAMS.DEST_SPACE_ID.name(), "dest");
        taskParams.put(TASK_PARAMS.BOOTSTRAP_CONTENT_ID.name(), "boot");
        taskParams.put(TASK_PARAMS.JAR_CONTENT_ID.name(), "jar");
        taskParams.put(TASK_PARAMS.INSTANCE_TYPE.name(), "instance");
        taskParams.put(TASK_PARAMS.NUM_INSTANCES.name(), "2");
        taskParams.put(TASK_PARAMS.DEST_FORMAT.name(), "png");
        taskParams.put(TASK_PARAMS.COLOR_SPACE.name(), "sRGB");
        taskParams.put(TASK_PARAMS.MAPPERS_PER_INSTANCE.name(), "1");

        params = SerializationUtil.serializeMap(taskParams);
        String result = runner.performTask(params);
        assertNotNull(result);

        Map<String, String> resultMap =
            SerializationUtil.deserializeMap(result);
        assertNotNull(resultMap);
        assertEquals("1", resultMap.get(HadoopTypes.TASK_OUTPUTS.JOB_FLOW_ID.name()));
    }


}
