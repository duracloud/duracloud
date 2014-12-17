/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.storage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.StorageClass;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskRunner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Aug 30, 2010
 */
public abstract class BaseStorageClassTaskRunner implements TaskRunner {

    private static final int MAX_FAILURES = 500;

    protected StorageProvider s3Provider;
    protected S3StorageProvider unwrappedS3Provider;
    protected AmazonS3Client s3Client;

    protected abstract StorageClass getStorageClass();

    public abstract String getName();

    @Override
    public String performTask(String taskParameters) {
        String spaceId = taskParameters;
        if(spaceId == null) {
            throw new RuntimeException("Space ID is required to " +
                                       "set storage class");
        }

        // Will throw if bucket does not exist
        String bucketName = unwrappedS3Provider.getBucketName(spaceId);

        Iterator<String> contentItems =
            s3Provider.getSpaceContents(spaceId, null);

        long successful = 0;
        Map<String, String> unsuccessful = new HashMap<String, String>();
        boolean broken = false;

        while(contentItems.hasNext()) {
            String contentId = contentItems.next();
            try {
                s3Client.changeObjectStorageClass(bucketName,
                                                  contentId,
                                                  getStorageClass());
                successful++;
            } catch(Exception e) {
                unsuccessful.put(contentId, e.getMessage());
                if(unsuccessful.size() > MAX_FAILURES) {
                    broken = true;
                    break;
                }
            }
        }

        String results = successful + " items updated successfully\n";
        if(unsuccessful.size() > 0) {
            results += unsuccessful.size() + " items failed:\n";
            for(String failedId : unsuccessful.keySet()) {
                results += "    " + failedId + " :" +
                        unsuccessful.get(failedId) + "\n";
            }

            if(broken) {
                results += "Max failures reached prior to task completion, " +
                           "this task did not complete";
            }
        }
        return results;
    }
}
