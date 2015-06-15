/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.task;

import org.duracloud.StorageTaskConstants;
import org.duracloud.client.ContentStore;
import org.duracloud.s3storageprovider.dto.DeleteStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.DisableStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskParameters;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.GetSignedUrlTaskResult;
import org.duracloud.s3storageprovider.dto.GetUrlTaskResult;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.RestoreStatus;
import org.duracloud.snapshot.dto.SnapshotContentItem;
import org.duracloud.snapshot.dto.SnapshotStatus;
import org.duracloud.snapshot.dto.SnapshotSummary;
import org.duracloud.snapshot.dto.task.*;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Bill Branan
 *         Date: 3/5/14
 */
public class S3TaskClientImplTest {

    private S3TaskClientImpl taskClient;
    private ContentStore contentStore;

    private String spaceId = "space-id";
    private String contentId = "content-id";
    private String resourcePrefix = null;
    private boolean secure = true;
    private String completionResult = "result";

    @Before
    public void setup() {
        contentStore = EasyMock.createMock(ContentStore.class);
        taskClient = new S3TaskClientImpl(contentStore);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore);
    }

    @After
    public void teardown() {
        EasyMock.verify(contentStore);
    }

    private void setupMock(String taskName, String preparedResult)
        throws Exception {
        EasyMock.expect(contentStore.performTask(EasyMock.eq(taskName),
                                                 EasyMock.isA(String.class)))
                .andReturn(preparedResult);
    }

    @Test
    public void testEnableStreaming() throws Exception {
        String taskName = StorageTaskConstants.ENABLE_STREAMING_TASK_NAME;
        String streamingHost = "streaming-host";

        EnableStreamingTaskResult preparedResult= new EnableStreamingTaskResult();
        preparedResult.setResult(completionResult);
        preparedResult.setStreamingHost(streamingHost);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        EnableStreamingTaskResult result = taskClient.enableStreaming(spaceId, secure);
        assertThat(completionResult, equalTo(result.getResult()));
        assertThat(streamingHost, equalTo(result.getStreamingHost()));
    }

    @Test
    public void testDisableStreaming() throws Exception {
        String taskName = StorageTaskConstants.DISABLE_STREAMING_TASK_NAME;

        DisableStreamingTaskResult preparedResult= new DisableStreamingTaskResult();
        preparedResult.setResult(completionResult);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        DisableStreamingTaskResult result = taskClient.disableStreaming(spaceId);
        assertThat(completionResult, equalTo(result.getResult()));
    }

    @Test
    public void testDeleteStreaming() throws Exception {
        String taskName = StorageTaskConstants.DELETE_STREAMING_TASK_NAME;

        DeleteStreamingTaskResult preparedResult= new DeleteStreamingTaskResult();
        preparedResult.setResult(completionResult);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        DeleteStreamingTaskResult result = taskClient.deleteStreaming(spaceId);
        assertThat(completionResult, equalTo(result.getResult()));
    }

    @Test
    public void testGetUrl() throws Exception {
        String taskName = StorageTaskConstants.GET_URL_TASK_NAME;
        String streamingUrl = "streaming-url";

        GetUrlTaskResult preparedResult= new GetUrlTaskResult();
        preparedResult.setStreamUrl(streamingUrl);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        GetUrlTaskResult result = taskClient.getUrl(spaceId, contentId, resourcePrefix);
        assertThat(streamingUrl, equalTo(result.getStreamUrl()));
    }

    @Test
    public void testGetSignedUrlShort() throws Exception {
        String taskName = StorageTaskConstants.GET_SIGNED_URL_TASK_NAME;
        String signedUrl = "signed-url";

        GetSignedUrlTaskResult preparedResult= new GetSignedUrlTaskResult();
        preparedResult.setSignedUrl(signedUrl);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        GetSignedUrlTaskResult result =
            taskClient.getSignedUrl(spaceId, contentId, resourcePrefix);
        assertThat(signedUrl, equalTo(result.getSignedUrl()));
    }

    @Test
    public void testGetSignedUrl() throws Exception {
        String taskName = StorageTaskConstants.GET_SIGNED_URL_TASK_NAME;
        String signedUrl = "signed-url";

        GetSignedUrlTaskResult preparedResult= new GetSignedUrlTaskResult();
        preparedResult.setSignedUrl(signedUrl);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        GetSignedUrlTaskResult result =
            taskClient.getSignedUrl(spaceId, contentId, resourcePrefix,
                                    42, "ip-address");
        assertThat(signedUrl, equalTo(result.getSignedUrl()));
    }

}
