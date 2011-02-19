/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.fixitymetadata;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.duracloud.client.ContentStore;
import org.duracloud.client.StoreCaller;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.hadoop.base.ProcessFileMapper;
import org.duracloud.storage.provider.StorageProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Mapper used to perform fixity service.
 *
 * @author: Andrew Woods
 * Date: Feb 9, 2011
 */
public class HashMetadataFinderMapper extends ProcessFileMapper {

    private static final String HASH = "hash-key";

    @Override
    public void map(Text key,
                    Text value,
                    OutputCollector<Text, Text> output,
                    Reporter reporter) throws IOException {

        String filePath = key.toString();
        try {
            reporter.setStatus("Processing file: " + filePath);
            System.out.println("Starting map processing for file: " + filePath);
            String outcome = SUCCESS;
            resultInfo.put(INPUT_PATH, filePath);

            ContentStore contentStore = getContentStore();
            String spaceId = pathUtil.getSpaceId(filePath);
            String contentId = pathUtil.getContentId(filePath);

            Map<String, String> metadata = getContentMetadata(contentStore,
                                                              spaceId,
                                                              contentId);
            if (null == metadata) {
                System.out.println("metadata is null for: " + filePath);
                metadata = new HashMap<String, String>();
            }

            String hash = metadata.get(StorageProvider.METADATA_CONTENT_MD5);
            if (null == hash) {
                hash = metadata.get(StorageProvider.METADATA_CONTENT_CHECKSUM);
            }
            if (null == hash) {
                System.out.println("hash metadata not found: " + filePath);
                hash = "not-found";
                outcome = FAILURE;
            }

            System.out.println("item: " + filePath + ", hash: " + hash);
            resultInfo.put(HASH, hash);

            // Collect result information
            resultInfo.put(RESULT, outcome);

            System.out.println(
                "Map processing completed: " + outcome + ", for: " + filePath);

        } catch (IOException e) {
            resultInfo.put(RESULT, FAILURE);
            resultInfo.put(ERR_MESSAGE, e.getMessage());

            System.out.println(
                "Map processing failed for: " + filePath + " due to: " +
                    e.getMessage());
            e.printStackTrace(System.err);

        } finally {
            output.collect(new Text(collectResult()), new Text(""));
            reporter.setStatus("Processing complete for file: " + filePath);
        }
    }

    /**
     * This method leverages the StoreCaller abstract class to loop on failed
     * contentStore calls.
     *
     * @return
     */
    private Map<String, String> getContentMetadata(final ContentStore contentStore,
                                                   final String spaceId,
                                                   final String contentId) {
        StoreCaller<Map<String, String>> caller = new StoreCaller<Map<String, String>>() {
            protected Map<String, String> doCall()
                throws ContentStoreException {
                return contentStore.getContentMetadata(spaceId, contentId);
            }

            public String getLogMessage() {
                return "Error calling contentStore.getContentMetadata() for: " +
                    spaceId + "/" + contentId;
            }
        };
        return caller.call();
    }

    @Override
    protected String collectResult() throws IOException {
        String path = super.resultInfo.get(INPUT_PATH);

        StringBuilder sb = new StringBuilder();
        sb.append(pathUtil.getSpaceId(path));
        sb.append(",");
        sb.append(pathUtil.getContentId(path));
        sb.append(",");
        sb.append(super.resultInfo.get(HASH));
        return sb.toString();
    }

}
