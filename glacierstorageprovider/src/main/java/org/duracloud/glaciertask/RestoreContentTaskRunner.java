/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.glaciertask;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.duracloud.glacierstorage.GlacierStorageProvider;
import org.duracloud.storage.error.StorageStateException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskRunner;

/**
 * @author: Bill Branan
 * Date: 2/1/13
 */
public class RestoreContentTaskRunner implements TaskRunner {

    protected static final String RESTORE_IN_PROGRESS =
        "RestoreAlreadyInProgress";

    private static final String TASK_NAME = "restore-content";
    private static final int RESTORE_EXPIRATION_DAYS = 14;

    private StorageProvider glacierProvider;
    private GlacierStorageProvider unwrappedGlacierProvider;
    private AmazonS3Client s3Client;

    public RestoreContentTaskRunner(StorageProvider glacierProvider,
                                    GlacierStorageProvider unwrappedGlacierProvider,
                                    AmazonS3Client s3Client) {
        this.glacierProvider = glacierProvider;
        this.unwrappedGlacierProvider = unwrappedGlacierProvider;
        this.s3Client = s3Client;
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        // Parse params. Expected value contains space ID and content ID
        // separated by a '/'. Example: my-space/image-42.jpg
        String spaceId = null;
        String contentId = null;
        if(null != taskParameters && taskParameters.length() > 0) {
            int separator = taskParameters.indexOf("/");
            spaceId = taskParameters.substring(0, separator);
            contentId = taskParameters.substring(separator+1);
        }
        if(null == spaceId || null == contentId ||
           spaceId.equals("") || contentId.equals("")) {
            throw new RuntimeException("A path including a space ID and an " +
                "content ID (separated by a '/') is required.");
        }

        // Restore content item
        try {
            s3Client.restoreObject(unwrappedGlacierProvider.getBucketName(spaceId),
                                   contentId,
                                   RESTORE_EXPIRATION_DAYS);
        } catch(AmazonS3Exception e) {
            if(RESTORE_IN_PROGRESS.equals(e.getErrorCode())) {
                throw new StorageStateException(
                    "Restore is already in progress for " + taskParameters, e);
            }
        }

        return "Request to restore content item " + contentId + " in space " +
               spaceId + " was successful.";
    }

}
