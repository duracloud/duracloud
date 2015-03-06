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

/**
 * Provides a client interface for S3StorageProvider's set of tasks.
 *
 * @author Bill Branan
 *         Date: 3/6/15
 */
public interface S3TaskClient {

    /**
     * Enables streaming on a space.
     *
     * @param spaceId the ID of the space where streaming will be enabled
     * @param secure true if signed URLs should be required to stream space content
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

}
