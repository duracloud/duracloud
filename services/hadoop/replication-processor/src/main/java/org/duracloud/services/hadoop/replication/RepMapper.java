/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.replication;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.duracloud.services.hadoop.base.ProcessFileMapper;
import org.duracloud.services.hadoop.base.ProcessResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Mapper used to perform replication.
 *
 * @author: Bill Branan
 * Date: Sept 23, 2010
 */
public class RepMapper extends ProcessFileMapper {

    public static final String DATE = "date";
    public static final String SRC_SIZE = "source-file-bytes";
    public static final String REP_RESULT = "replication-result";
    public static final String REP_ATTEMPTS = "replication-attempts";

    private static final int MAX_ATTEMPTS = 5;

    /**
     * Replicate a file to another storage location
     *
     * @param file the file to be replicated
     * @param origContentId the original ID of the file
     * @return null (there is no resultant file)
     */
    @Override
    protected ProcessResult processFile(File file, String origContentId) 
        throws IOException {
        resultInfo.put(SRC_SIZE, String.valueOf(file.length()));

        String dcHost = jobConf.get(RepInitParamParser.DC_HOST);
        String dcPort = jobConf.get(RepInitParamParser.DC_PORT);
        String dcContext = jobConf.get(RepInitParamParser.DC_CONTEXT);
        String dcUser = jobConf.get(RepInitParamParser.DC_USERNAME);
        String dcPass = jobConf.get(RepInitParamParser.DC_PASSWORD);
        String repStoreId = jobConf.get(RepInitParamParser.REP_STORE_ID);
        String repSpaceId = jobConf.get(RepInitParamParser.REP_SPACE_ID);
        String fromSpaceId = jobConf.get(RepInitParamParser.SOURCE_SPACE_ID);

        System.out.println("Performing replication of file " + file.getName() +
                           " from space " + fromSpaceId + " to space " +
                           repSpaceId + " at host " + dcHost + " as " + dcUser);

        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(dcHost, dcPort, dcContext);
        Credential credential = new Credential(dcUser, dcPass);
        storeManager.login(credential);

        ContentStore primaryStore;
        ContentStore repStore;
        try {
            primaryStore = storeManager.getPrimaryContentStore();
            repStore = storeManager.getContentStore(repStoreId);
        } catch(ContentStoreException e) {
            throw new IOException(e);
        }

        if(primaryStore == null) {
            throw new IOException("Could not connect to FROM store");
        } else if(repStore == null) {
            throw new IOException("Could not connect to TO store");
        }

        System.out.println("Connected to both to and from stores");

        String contentId = origContentId;

        Exception exception = null;
        int attempts;
        for(attempts = 1; attempts <= MAX_ATTEMPTS; attempts++) {
            try {
                Map<String, String> metadata =
                    primaryStore.getContentMetadata(fromSpaceId, contentId);
                try {
                    replicate(repStore, repSpaceId, contentId, file, metadata);
                    break;
                } catch(NotFoundException e) {
                    System.out.println("NFE: " + e.getMessage());
                    checkSpace(primaryStore, repStore, repSpaceId, fromSpaceId);
                }
            } catch(ContentStoreException e) {
                System.out.println("CSE: " + e.getMessage());
                exception = e;
            }
        }

        resultInfo.put(REP_ATTEMPTS, String.valueOf(attempts));        

        if(attempts >= MAX_ATTEMPTS && exception != null) {
            throw new IOException("Unable to replicate file " +
                                  file.getName() + " due to " +
                                  exception.getMessage(), exception);
        }

        return null;
    }

    private void checkSpace(ContentStore primaryStore,
                            ContentStore toStore,
                            String toSpaceId,
                            String fromSpaceId)
        throws ContentStoreException {
        try {
            toStore.getSpaceMetadata(toSpaceId);
        } catch(NotFoundException e) {
            // Create Space
            System.out.println("Creating space: " + toSpaceId);
            toStore.createSpace(toSpaceId,
                                primaryStore.getSpaceMetadata(fromSpaceId));
        }
    }

    private void replicate(ContentStore toStore,
                           String toSpaceId,
                           String contentId,
                           File file,
                           Map<String, String> origMetadata)
        throws ContentStoreException, IOException {
        System.out.println("Replicating " + contentId + " to " + toSpaceId);        

        String origMimetype = "application/octet-stream";
        String origChecksum = "";
        if(null != origMetadata) {
            origMimetype = origMetadata.get(ContentStore.CONTENT_MIMETYPE);
            origChecksum = origMetadata.get(ContentStore.CONTENT_CHECKSUM);
        }

        // Check to see if file already exists
        boolean exists = false;
        if(null != origChecksum) {
            Map<String, String> destMetadata = null;
            try {
                destMetadata = toStore.getContentMetadata(toSpaceId, contentId);
            } catch(NotFoundException e) {
                destMetadata = null;
            }
            if(null != destMetadata) {
                String destChecksum =
                    destMetadata.get(ContentStore.CONTENT_CHECKSUM);
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
            toStore.addContent(toSpaceId,
                               contentId,
                               new FileInputStream(file),
                               file.length(),
                               origMimetype,
                               origChecksum,
                               origMetadata);
            resultInfo.put(REP_RESULT, "file replicated");
        }
    }

    @Override
    protected String collectResult() throws IOException {
        String result = super.collectResult();
        result += ", " + REP_RESULT + "=" +  resultInfo.get(REP_RESULT);
        result += ", " + SRC_SIZE + "=" +  resultInfo.get(SRC_SIZE);
        result += ", " + REP_ATTEMPTS + "=" + resultInfo.get(REP_ATTEMPTS);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String now = format.format(new Date(System.currentTimeMillis()));
        result += ", " + DATE + "=" + now;

        return result;
    }

}
