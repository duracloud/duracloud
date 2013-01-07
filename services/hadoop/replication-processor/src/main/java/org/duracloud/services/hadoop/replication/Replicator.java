/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.replication;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.duracloud.services.hadoop.store.FileWithMD5;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.duracloud.services.hadoop.replication.RepMapper.REP_RESULT;
import static org.duracloud.services.hadoop.replication.RepMapper.RESULT_PATH;

/**
 * Performs the work of moving a content item to be replicated to the
 * destination location.
 *
 * @author: Bill Branan
 * Date: Jan 5, 2011
 */
public class Replicator implements Runnable {

    private boolean success = false;
    private Exception exception = null;
    
    private ContentStore primaryStore = null;
    private String fromSpaceId = null;
    private String contentId = null;
    private ContentStore repStore = null;
    private String repSpaceId = null;
    private FileWithMD5 fileWithMD5 = null;    
    private Map<String, String> resultInfo = null;

    public Replicator(ContentStore primaryStore, 
                      String fromSpaceId,
                      String contentId,
                      ContentStore repStore,
                      String repSpaceId,
                      FileWithMD5 fileWithMD5,
                      Map<String, String> resultInfo) {
        this.primaryStore = primaryStore;
        this.fromSpaceId = fromSpaceId;
        this.contentId = contentId;
        this.repStore = repStore;
        this.repSpaceId = repSpaceId;
        this.fileWithMD5 = fileWithMD5;
        this.resultInfo = resultInfo;
    }

    @Override
    public void run() {
        try {
            Map<String, String> properties =
                primaryStore.getContentProperties(fromSpaceId, contentId);
            try {
                replicate(repStore,
                          repSpaceId,
                          contentId,
                          fileWithMD5,
                          properties);
                success = true;
            } catch(NotFoundException e) {
                System.out.println("NotFoundException: " + e.getMessage());
                checkSpace(primaryStore, repStore, repSpaceId, fromSpaceId);
            }
        } catch(ContentStoreException e) {
            System.out.println("ContentStoreException: " + e.getMessage());
            exception = e;
        } catch(IOException e) {
            System.out.println("IOException: " + e.getMessage());
            exception = e;
        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
            exception = e;
        }
    }

    private void checkSpace(ContentStore primaryStore,
                            ContentStore toStore,
                            String toSpaceId,
                            String fromSpaceId)
        throws ContentStoreException {
        try {
            toStore.getSpaceProperties(toSpaceId);
        } catch(NotFoundException e) {
            // Create Space
            System.out.println("Creating space: " + toSpaceId);
            toStore.createSpace(toSpaceId);
        }
    }

    private void replicate(ContentStore toStore,
                           String toSpaceId,
                           String contentId,
                           FileWithMD5 fileWithMD5,
                           Map<String, String> origProperties)
        throws ContentStoreException, IOException {
        System.out.println("Replicating " + contentId + " to " + toSpaceId);

        if (null == fileWithMD5 || null == fileWithMD5.getFile()) {
            throw new IOException("arg file is null");
        }
        File file = fileWithMD5.getFile();

        String origMimetype = "application/octet-stream";
        String origChecksum = "";
        if(null != origProperties) {
            origMimetype = origProperties.get(ContentStore.CONTENT_MIMETYPE);
            origChecksum = origProperties.get(ContentStore.CONTENT_CHECKSUM);
        }

        if (null == origChecksum) {
            origChecksum = fileWithMD5.getMd5();
        }

        // Check to see if file already exists
        boolean exists = false;
        if(null != origChecksum) {
            Map<String, String> destProperties;
            try {
                destProperties = toStore.getContentProperties(toSpaceId, contentId);
            } catch(NotFoundException e) {
                destProperties = null;
            }
            if(null != destProperties) {
                String destChecksum =
                    destProperties.get(ContentStore.CONTENT_CHECKSUM);
                if(null != destChecksum) {
                    if(origChecksum.equals(destChecksum)) {
                        exists = true;
                    }
                }
            }
        }

        if(exists) {
            System.out.println(contentId + " already exists in " + toSpaceId);
            resultInfo.put(REP_RESULT,
                           "file exists at destination, not replicated");
        } else {
            System.out.println("Adding " + contentId + " to " + toSpaceId);
            // Replicate content
            InputStream content =
                new AutoCloseInputStream(new FileInputStream(file));
            toStore.addContent(toSpaceId,
                               contentId,
                               content,
                               file.length(),
                               origMimetype,
                               origChecksum,
                               origProperties);
            
            System.out.println("Completed replication of " + contentId +
                               " to " + toSpaceId);
            resultInfo.put(REP_RESULT, "file replicated");
        }

        resultInfo.put(RESULT_PATH, toSpaceId + "/" + contentId);
    }

    public boolean isSuccess() {
        return success;
    }

    public Exception getException() {
        return exception;
    }
}
