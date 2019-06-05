/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.task;

import org.duracloud.error.ContentStoreException;
import org.duracloud.s3storageprovider.dto.DeleteStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.DisableStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskResult;
import org.duracloud.s3storageprovider.dto.GetSignedCookiesUrlTaskResult;
import org.duracloud.s3storageprovider.dto.GetSignedUrlTaskResult;
import org.duracloud.s3storageprovider.dto.GetUrlTaskResult;

/**
 * Provides a client interface for S3StorageProvider's set of tasks.
 *
 * @author Bill Branan
 * Date: 3/6/15
 */
public interface S3TaskClient {

    /**
     * Enables streaming on a space. Streaming can be either open or secure.
     *
     * @param spaceId the ID of the space where streaming will be enabled
     * @param secure  true if signed URLs should be required to stream space content
     * @return resulting status and streaming host value
     * @throws ContentStoreException on error
     */
    public EnableStreamingTaskResult enableStreaming(String spaceId, boolean secure)
        throws ContentStoreException;

    /**
     * Disables streaming on a space.
     *
     * @param spaceId the ID of the space where streaming will be disabled
     * @return resulting status
     * @throws ContentStoreException on erro
     */
    public DisableStreamingTaskResult disableStreaming(String spaceId)
        throws ContentStoreException;

    /**
     * Deletes a streaming distribution for a space. This ensures that content that
     * was available for streaming will no longer be available.
     *
     * @param spaceId the ID of the space where streaming will be disabled
     * @return resulting status
     * @throws ContentStoreException on error
     */
    public DeleteStreamingTaskResult deleteStreaming(String spaceId)
        throws ContentStoreException;

    /**
     * Retrieves a URL for a media file that is streamed via an open distribution
     *
     * @param spaceId        name of the space where the content to be streamed is stored
     * @param contentId      name of the content item to be streamed
     * @param resourcePrefix a prefix on the content item which may be required by
     *                       the streaming viewer/player (may be null)
     * @return RTMP streaming URL
     * @throws ContentStoreException on error
     */
    public GetUrlTaskResult getUrl(String spaceId,
                                   String contentId,
                                   String resourcePrefix)
        throws ContentStoreException;

    /**
     * Retrieves a signed URL for a media file that is streamed through a secure
     * distribution; uses default minutes to expire (480) and allows streaming to
     * any IP address
     *
     * @param spaceId        name of the space where the content to be streamed is stored
     * @param contentId      name of the content item to be streamed
     * @param resourcePrefix a prefix on the content item which may be required by
     *                       the streaming viewer/player (may be null)
     * @return RTMP streaming URL
     * @throws ContentStoreException on error
     */
    public GetSignedUrlTaskResult getSignedUrl(String spaceId,
                                               String contentId,
                                               String resourcePrefix)
        throws ContentStoreException;

    /**
     * Retrieves a signed URL for a media file that is streamed through a secure
     * distribution
     *
     * @param spaceId         name of the space where the content to be streamed is stored
     * @param contentId       name of the content item to be streamed
     * @param resourcePrefix  a prefix on the content item which may be required by
     *                        the streaming viewer/player
     * @param minutesToExpire number of minutes that the stream should be available
     *                        for viewing through the retrieved URL
     * @param ipAddress       ip address range where requests to stream must originate, in
     *                        CIDR notation (e.g. 1.2.3.4/32)
     * @return RTMP streaming URL
     * @throws ContentStoreException on error
     */
    public GetSignedUrlTaskResult getSignedUrl(String spaceId,
                                               String contentId,
                                               String resourcePrefix,
                                               int minutesToExpire,
                                               String ipAddress)
        throws ContentStoreException;

    /**
     * Enables HLS streaming on a space. Streaming can be either open or secure.
     *
     * @param spaceId the ID of the space where streaming will be enabled
     * @param secure  true if signed URLs should be required to stream space content
     * @return resulting status and streaming host value
     * @throws ContentStoreException on error
     */
    public EnableStreamingTaskResult enableHlsStreaming(String spaceId, boolean secure)
        throws ContentStoreException;

    /**
     * Disables HLS streaming on a space.
     *
     * @param spaceId the ID of the space where streaming will be disabled
     * @return resulting status
     * @throws ContentStoreException on erro
     */
    public DisableStreamingTaskResult disableHlsStreaming(String spaceId)
        throws ContentStoreException;

    /**
     * Deletes a streaming distribution for a space. This ensures that content that
     * was available for streaming will no longer be available.
     *
     * @param spaceId the ID of the space where streaming will be disabled
     * @return resulting status
     * @throws ContentStoreException on error
     */
    public DeleteStreamingTaskResult deleteHlsStreaming(String spaceId)
        throws ContentStoreException;

    /**
     * Returns a URL representing an HLS streamable resource.
     * @param spaceId the space ID that contains the content to be streamed
     * @param contentId the content ID to be converted to a streaming URL
     * @return
     * @throws ContentStoreException
     */
    public GetUrlTaskResult getHlsUrl(String spaceId,
                                      String contentId) throws ContentStoreException;

    /**
     * Generates signed cookies and provides a URL at which those cookies can be
     * set on the user's browser
     *
     * @param spaceId
     * @param ipAddress
     * @param minutesToExpire
     * @return
     * @throws ContentStoreException
     */
    public GetSignedCookiesUrlTaskResult getSignedCookiesUrl(String spaceId,
                                                             String ipAddress,
                                                             int minutesToExpire,
                                                             String redirectUrl) throws ContentStoreException;

}
