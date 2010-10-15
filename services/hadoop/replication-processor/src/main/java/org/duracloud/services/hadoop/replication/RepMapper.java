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
import org.duracloud.services.hadoop.store.FileWithMD5;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.*;

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
     * @param fileWithMD5 the file to be replicated
     * @param origContentId the original ID of the file
     * @return null (there is no resultant file)
     */
    @Override
    protected ProcessResult processFile(FileWithMD5 fileWithMD5, String origContentId)
        throws IOException {
        File file = fileWithMD5.getFile();
        resultInfo.put(SRC_SIZE, String.valueOf(file.length()));

        String dcHost = jobConf.get(TASK_PARAMS.DC_HOST.getLongForm());
        String dcPort = jobConf.get(TASK_PARAMS.DC_PORT.getLongForm());
        String dcContext = jobConf.get(TASK_PARAMS.DC_CONTEXT.getLongForm());
        String dcUser = jobConf.get(TASK_PARAMS.DC_USERNAME.getLongForm());
        String dcPass = jobConf.get(TASK_PARAMS.DC_PASSWORD.getLongForm());
        String repStoreId = jobConf.get(TASK_PARAMS.REP_STORE_ID.getLongForm());
        String repSpaceId = jobConf.get(TASK_PARAMS.REP_SPACE_ID.getLongForm());
        String fromSpaceId = jobConf.get(TASK_PARAMS.SOURCE_SPACE_ID.getLongForm());

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
                    replicate(repStore, repSpaceId, contentId, fileWithMD5, metadata);
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
                           FileWithMD5 fileWithMD5,
                           Map<String, String> origMetadata)
        throws ContentStoreException, IOException {
        System.out.println("Replicating " + contentId + " to " + toSpaceId);        

        if (null == fileWithMD5 || null == fileWithMD5.getFile()) {
            throw new IOException("arg file is null");
        }
        File file = fileWithMD5.getFile();

        String origMimetype = "application/octet-stream";
        String origChecksum = "";
        if(null != origMetadata) {
            origMimetype = origMetadata.get(ContentStore.CONTENT_MIMETYPE);
            origChecksum = origMetadata.get(ContentStore.CONTENT_CHECKSUM);
        }

        if (null == origChecksum) {
            origChecksum = fileWithMD5.getMd5();
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
