/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.glacierstorage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.StorageClass;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storage.StoragePolicy;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.error.StorageStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides content storage backed by Amazon's Glacier storage system.
 *
 * @author Bill Branan
 * Date: Dec 6, 2012
 */
public class GlacierStorageProvider extends S3StorageProvider {

    protected static final String INVALID_OBJECT_STATE = "InvalidObjectState";

    private final Logger log =
        LoggerFactory.getLogger(GlacierStorageProvider.class);

    public GlacierStorageProvider(String accessKey, String secretKey) {
        super(accessKey, secretKey);
    }

    public GlacierStorageProvider(AmazonS3Client s3Client, String accessKey) {
        super(s3Client, accessKey, null);
    }

    @Override
    protected StoragePolicy getStoragePolicy() {
        return new StoragePolicy(StorageClass.Glacier, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String copyContent(String sourceSpaceId,
                              String sourceContentId,
                              String destSpaceId,
                              String destContentId) {
        try {
            return super.copyContent(sourceSpaceId,
                                     sourceContentId,
                                     destSpaceId,
                                     destContentId);
        } catch (StorageException e) {
            checkStorageState(e);
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getContent(String spaceId, String contentId) {
        log.debug("getContent(" + spaceId + ", " + contentId + ")");

        try {
            return super.getContent(spaceId, contentId);
        } catch (StorageException e) {
            checkStorageState(e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setContentProperties(String spaceId,
                                     String contentId,
                                     Map<String, String> contentProperties) {
        log.debug("setContentProperties(" + spaceId + ", " + contentId + ")");

        try {
            super.setContentProperties(spaceId,
                                       contentId,
                                       contentProperties);
        } catch (StorageException e) {
            checkStorageState(e);
            throw e; 
        }
    }

    /**
     * Recognize and handle exceptions due to content which resides in Glacier
     * but has not been retrieved for access.
     */
    private void checkStorageState(StorageException e) {
        if(e.getCause() instanceof AmazonS3Exception) {
            String errorCode =
                ((AmazonS3Exception)e.getCause()).getErrorCode();
            if(INVALID_OBJECT_STATE.equals(errorCode)) {
                String message = "The storage state of this content item " +
                    "does not allow for this action to be taken. To resolve " +
                    "this issue: 1. Request that this content item be " +
                    "retrieved from offline storage 2. Wait (retrieval may " +
                    "take up to 5 hours) 3. Retry this request";
                throw new StorageStateException(message, e);
            }
        }
    }

}
