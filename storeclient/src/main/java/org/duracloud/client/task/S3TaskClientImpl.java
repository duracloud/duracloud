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
import org.duracloud.s3storageprovider.dto.GetSignedUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetSignedUrlTaskResult;
import org.duracloud.s3storageprovider.dto.GetUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetUrlTaskResult;

/**
 * Implements the S3 task client interface by making task calls through
 * a ContentStore.
 *
 * @author Bill Branan
 *         Date: 3/6/15
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
    public EnableStreamingTaskResult enableStreaming(String spaceId, boolean secure)
        throws ContentStoreException {
        EnableStreamingTaskParameters taskParams = new EnableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setSecure(secure);

        return EnableStreamingTaskResult.deserialize(
            contentStore.performTask(StorageTaskConstants.ENABLE_STREAMING_TASK_NAME,
                                     taskParams.serialize()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DisableStreamingTaskResult disableStreaming(String spaceId)
        throws ContentStoreException {
        DisableStreamingTaskParameters taskParams = new DisableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);

        return DisableStreamingTaskResult.deserialize(
            contentStore.performTask(StorageTaskConstants.DISABLE_STREAMING_TASK_NAME,
                                     taskParams.serialize()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeleteStreamingTaskResult deleteStreaming(String spaceId)
        throws ContentStoreException {
        DeleteStreamingTaskParameters taskParams = new DeleteStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);

        return DeleteStreamingTaskResult.deserialize(
            contentStore.performTask(StorageTaskConstants.DELETE_STREAMING_TASK_NAME,
                                     taskParams.serialize()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetUrlTaskResult getUrl(String spaceId,
                                   String contentId,
                                   String resourcePrefix) throws ContentStoreException {
        GetUrlTaskParameters taskParams = new GetUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);
        taskParams.setResourcePrefix(resourcePrefix);

        return GetUrlTaskResult.deserialize(
            contentStore.performTask(StorageTaskConstants.GET_URL_TASK_NAME,
                                     taskParams.serialize()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetSignedUrlTaskResult getSignedUrl(String spaceId,
                                               String contentId,
                                               String resourcePrefix)
        throws ContentStoreException {
        int defaultExpire = GetSignedUrlTaskParameters.USE_DEFAULT_MINUTES_TO_EXPIRE;
        return getSignedUrl(spaceId, contentId, resourcePrefix, defaultExpire, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetSignedUrlTaskResult getSignedUrl(String spaceId,
                                               String contentId,
                                               String resourcePrefix,
                                               int minutesToExpire,
                                               String ipAddress)
        throws ContentStoreException {
        GetSignedUrlTaskParameters taskParams = new GetSignedUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);
        taskParams.setResourcePrefix(resourcePrefix);
        taskParams.setMinutesToExpire(minutesToExpire);
        taskParams.setIpAddress(ipAddress);

        return GetSignedUrlTaskResult.deserialize(
            contentStore.performTask(StorageTaskConstants.GET_SIGNED_URL_TASK_NAME,
                                     taskParams.serialize()));
    }
}
