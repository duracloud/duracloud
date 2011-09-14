/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.common.util.SerializationUtil;
import org.jets3t.service.CloudFrontService;
import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author: Bill Branan
 * Date: 9/14/11
 */
public class AddStreamingItemTaskRunnerTest extends StreamingTaskRunnerTestBase {

    private String spaceId = "spaceId";
    private String contentId = "contentId";
    private String params = spaceId + ":" + contentId;

    @Test
    public void testGetName() throws Exception {
        AddStreamingItemTaskRunner runner =
            createRunner(createMockS3ClientV1(), createMockCFServiceV1());

        String name = runner.getName();
        assertEquals("add-streaming-item", name);
    }

    private AddStreamingItemTaskRunner createRunner(AmazonS3Client s3Client,
                                                    CloudFrontService cfService) {
        this.s3Provider = createMockS3StorageProvider();
        this.s3Client = s3Client;
        this.cfService = cfService;
        return new AddStreamingItemTaskRunner(s3Provider, s3Client, cfService);
    }

    @Test
    public void testTaskParams() throws Exception {
        AddStreamingItemTaskRunner runner =
            createRunner(createMockS3ClientV1(), createMockCFServiceV1());

        AddStreamingItemTaskRunner.TaskParams taskParams =
            runner.parseTaskParams(params);

        assertEquals(spaceId, taskParams.getSpaceId());
        assertEquals(contentId, taskParams.getContentId());

        // Test failure
        try {
            runner.parseTaskParams("");
            fail("Exception expected when parsing invalid parameters");
        } catch(RuntimeException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void testPerformTask() throws Exception {
        AddStreamingItemTaskRunner runner =
            createRunner(createMockS3ClientV2(1), createMockCFServiceV2());

        String results = runner.performTask(params);
        assertNotNull(results);
        testResults(results);
    }

    private void testResults(String results) {
        Map<String, String> resultMap =
            SerializationUtil.deserializeMap(results);
        assertNotNull(resultMap);
        assertTrue(resultMap.get("results").contains("completed"));
    }

}
