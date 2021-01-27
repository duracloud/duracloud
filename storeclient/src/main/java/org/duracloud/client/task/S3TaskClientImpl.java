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
import org.duracloud.error.ContentStoreException;
import org.duracloud.s3storageprovider.dto.DeleteStreamingTaskParameters;
import org.duracloud.s3storageprovider.dto.DeleteStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.DisableStreamingTaskParameters;
import org.duracloud.s3storageprovider.dto.DisableStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskParameters;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.GetHlsUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetSignedCookiesUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetSignedCookiesUrlTaskResult;
import org.duracloud.s3storageprovider.dto.GetUrlTaskResult;

/**
 * Implements the S3 task client interface by making task calls through
 * a ContentStore.
 *
 * @author Bill Branan
 * Date: 3/6/15
 */
public class S3TaskClientImpl implements S3TaskClient {

    private ContentStore contentStore;

    public S3TaskClientImpl(ContentStore contentStore) {
        this.contentStore = contentStore;
    }

   /**
     * {@inheritDoc}
     */
    @Override
    public EnableStreamingTaskResult enableHlsStreaming(String spaceId, boolean secure)
        throws ContentStoreException {
        EnableStreamingTaskParameters taskParams = new EnableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setSecure(secure);

        return EnableStreamingTaskResult.deserialize(
            contentStore.performTask(StorageTaskConstants.ENABLE_HLS_TASK_NAME,
                                     taskParams.serialize()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DisableStreamingTaskResult disableHlsStreaming(String spaceId)
        throws ContentStoreException {
        DisableStreamingTaskParameters taskParams = new DisableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);

        return DisableStreamingTaskResult.deserialize(
            contentStore.performTask(StorageTaskConstants.DISABLE_HLS_TASK_NAME,
                                     taskParams.serialize()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteStreamingTaskResult deleteHlsStreaming(String spaceId)
        throws ContentStoreException {
        DeleteStreamingTaskParameters taskParams = new DeleteStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);

        return DeleteStreamingTaskResult.deserialize(
            contentStore.performTask(StorageTaskConstants.DELETE_HLS_TASK_NAME,
                                     taskParams.serialize()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetUrlTaskResult getHlsUrl(String spaceId, String contentId) throws ContentStoreException {
        final GetHlsUrlTaskParameters params = new GetHlsUrlTaskParameters();
        params.setSpaceId(spaceId);
        params.setContentId(contentId);
        return GetUrlTaskResult.deserialize(
            contentStore.performTask(StorageTaskConstants.GET_HLS_URL_TASK_NAME, params.serialize()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetSignedCookiesUrlTaskResult getSignedCookiesUrl(String spaceId,
                                                             String ipAddress,
                                                             int minutesToExpire,
                                                             String redirectUrl)
        throws ContentStoreException {
        final GetSignedCookiesUrlTaskParameters params = new GetSignedCookiesUrlTaskParameters();
        params.setSpaceId(spaceId);
        params.setIpAddress(ipAddress);
        params.setMinutesToExpire(minutesToExpire);
        params.setRedirectUrl(redirectUrl);
        return GetSignedCookiesUrlTaskResult.deserialize(
            contentStore.performTask(StorageTaskConstants.GET_SIGNED_COOKIES_URL_TASK_NAME, params.serialize()));
    }
}
