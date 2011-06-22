/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.replication.retry;

import org.apache.hadoop.mapred.Reporter;

import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.hadoop.base.ProcessFileMapper;
import org.duracloud.services.hadoop.base.ProcessResult;
import org.duracloud.services.hadoop.replication.RepMapper;
import org.duracloud.services.hadoop.replication.RepOutputFormat;
import org.duracloud.services.hadoop.store.FileWithMD5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

public class RetryRepMapper extends RepMapper {

    private String result;

    /**
     * Replicate a file to another storage location if the
     * previous job failed to replicate the file
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
        result = null;

        if(verifyProcessFile(origContentId)) {
            System.out.println("RetryRepMapper found failure - " + origContentId);

            return super.processFile(fileWithMD5,
                                     origContentId,
                                     reporter);
        }
        System.out.println("RetryRepMapper found success - " + origContentId);

        return null;
    }

    @Override
    protected String collectResult() throws IOException {
        if(result != null) {
            return result;
        }
        return super.collectResult();
    }

    protected boolean verifyProcessFile(String filePath) {
        String spaceId = jobConf.get(TASK_PARAMS.OUTPUT_PATH.getLongForm());

        String dcHost = jobConf.get(TASK_PARAMS.DC_HOST.getLongForm());
        String dcPort = jobConf.get(TASK_PARAMS.DC_PORT.getLongForm());
        String dcContext = jobConf.get(TASK_PARAMS.DC_CONTEXT.getLongForm());
        String dcUser = jobConf.get(TASK_PARAMS.DC_USERNAME.getLongForm());
        String dcPass = jobConf.get(TASK_PARAMS.DC_PASSWORD.getLongForm());

        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(dcHost, dcPort, dcContext);
        Credential credential = new Credential(dcUser, dcPass);
        storeManager.login(credential);

        ContentStore primaryStore;
        Content content;
        BufferedReader reader = null;
        String line;

        try {
            primaryStore = storeManager.getPrimaryContentStore();

            RepOutputFormat format = new RepOutputFormat();

            content = primaryStore.getContent(pathUtil.getSpaceId(spaceId),
                                              format.getOutputFileName());

            reader = new BufferedReader(
                new InputStreamReader(content.getStream()));

            while ((line = reader.readLine()) != null)   {
                if(line.contains(filePath)) {
                    if(line.startsWith("failure"))
                        return true;
                    else {
                        result = line;
                        return false;
                    }
                }
            }
        }  catch(ContentStoreException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }
}
