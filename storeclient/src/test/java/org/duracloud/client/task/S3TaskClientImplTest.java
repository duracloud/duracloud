/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.task;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.duracloud.StorageTaskConstants;
import org.duracloud.client.ContentStore;
import org.duracloud.s3storageprovider.dto.DeleteStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.DisableStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.GetSignedCookiesUrlTaskResult;
import org.duracloud.s3storageprovider.dto.GetUrlTaskResult;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 3/5/14
 */
public class S3TaskClientImplTest {

    private S3TaskClientImpl taskClient;
    private ContentStore contentStore;

    private String spaceId = "space-id";
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
    public void testEnableHlsStreaming() throws Exception {
        String taskName = StorageTaskConstants.ENABLE_HLS_TASK_NAME;
        String streamingHost = "streaming-host";

        EnableStreamingTaskResult preparedResult = new EnableStreamingTaskResult();
        preparedResult.setResult(completionResult);
        preparedResult.setStreamingHost(streamingHost);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        EnableStreamingTaskResult result = taskClient.enableHlsStreaming(spaceId, secure);
        assertThat(completionResult, equalTo(result.getResult()));
        assertThat(streamingHost, equalTo(result.getStreamingHost()));
    }

    @Test
    public void testDisableHlsStreaming() throws Exception {
        String taskName = StorageTaskConstants.DISABLE_HLS_TASK_NAME;

        DisableStreamingTaskResult preparedResult = new DisableStreamingTaskResult();
        preparedResult.setResult(completionResult);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        DisableStreamingTaskResult result = taskClient.disableHlsStreaming(spaceId);
        assertThat(completionResult, equalTo(result.getResult()));
    }

    @Test
    public void testDeleteHlsStreaming() throws Exception {
        String taskName = StorageTaskConstants.DELETE_HLS_TASK_NAME;

        DeleteStreamingTaskResult preparedResult = new DeleteStreamingTaskResult();
        preparedResult.setResult(completionResult);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        DeleteStreamingTaskResult result = taskClient.deleteHlsStreaming(spaceId);
        assertThat(completionResult, equalTo(result.getResult()));
    }

    @Test
    public void testGetHlsUrl() throws Exception {
        String taskName = StorageTaskConstants.GET_HLS_URL_TASK_NAME;
        final String streamUrl = "stream-url";
        GetUrlTaskResult preparedResult = new GetUrlTaskResult();
        preparedResult.setStreamUrl(streamUrl);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        GetUrlTaskResult result = taskClient.getHlsUrl(spaceId, "content");
        assertThat(streamUrl, equalTo(result.getStreamUrl()));
    }

    @Test
    public void testGetSignedCookiesUrl() throws Exception {
        final String taskName = StorageTaskConstants.GET_SIGNED_COOKIES_URL_TASK_NAME;
        final int minutesToExpiration = 10;
        final String ipAddress = "0.0.0.0";
        final String redirectUrl = "https://redirect.url";

        GetSignedCookiesUrlTaskResult preparedResult = new GetSignedCookiesUrlTaskResult();
        String signedCookiesUrl = "https://signed-cookies.url";
        preparedResult.setSignedCookiesUrl(signedCookiesUrl);
        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        GetSignedCookiesUrlTaskResult result =
            taskClient.getSignedCookiesUrl(spaceId, ipAddress, minutesToExpiration, redirectUrl);
        assertThat(signedCookiesUrl, equalTo(result.getSignedCookiesUrl()));
    }

}
