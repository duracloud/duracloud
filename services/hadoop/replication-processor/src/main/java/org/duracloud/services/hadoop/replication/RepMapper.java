/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.replication;

import org.apache.hadoop.mapred.Reporter;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.hadoop.base.ProcessFileMapper;
import org.duracloud.services.hadoop.base.ProcessResult;
import org.duracloud.services.hadoop.store.FileWithMD5;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.duracloud.services.hadoop.base.Constants.DELIM;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * Mapper used to perform replication.
 *
 * @author: Bill Branan
 * Date: Sept 23, 2010
 */
public class RepMapper extends ProcessFileMapper {

    public static final String DATE = "date";
    public static final String SRC_SIZE = "source-file-bytes";
    public static final String REP_RESULT = "duplication-result";
    public static final String REP_ATTEMPTS = "duplication-attempts";

    private static final int MAX_ATTEMPTS = 5;

    /**
     * Replicate a file to another storage location
     *
     * @param fileWithMD5 the file to be replicated
     * @param origContentId the original ID of the file
     * @return null (there is no resultant file)
     */
    @Override
    protected ProcessResult processFile(FileWithMD5 fileWithMD5,
                                        String origContentId,
                                        Reporter reporter)
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
            Replicator replicator = new Replicator(primaryStore,
                                                   fromSpaceId,
                                                   contentId,
                                                   repStore,
                                                   repSpaceId,
                                                   fileWithMD5,
                                                   resultInfo);
            Thread thread = new Thread(replicator);
            thread.start();

            while (thread.isAlive()) {
                sleep(1000);
                reporter.progress();
            }

            if(replicator.isSuccess()) {
                break;
            } else {
                exception = replicator.getException();
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

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    @Override
    protected String collectResult() throws IOException {
        StringBuilder result = new StringBuilder(resultInfo.get(RESULT));

        String errMsg = resultInfo.get(ERR_MESSAGE);
        if (errMsg != null) {
            result.append("-" + errMsg);
        }

        result.append(DELIM);
        result.append(resultInfo.get(INPUT_PATH));
        result.append(DELIM);
        result.append(resultInfo.get(RESULT_PATH));
        result.append(DELIM);
        result.append(resultInfo.get(REP_RESULT));
        result.append(DELIM);
        result.append(resultInfo.get(SRC_SIZE));
        result.append(DELIM);
        result.append(resultInfo.get(REP_ATTEMPTS));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String now = format.format(new Date(System.currentTimeMillis()));
        result.append(DELIM);
        result.append(now);

        System.out.println("collected result: '" + result + "'");

        return result.toString();
    }

}
