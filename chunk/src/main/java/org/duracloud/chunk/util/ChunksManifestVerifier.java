/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean.ManifestEntry;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.util.CollectionUtils;

/**
 * This class is responsible for verifying that all the chunks listed in a given
 * chunk manifest exactly match the actual chunks stored in a specified space.
 * 
 *
 * 
 * @author dbernstein
 *
 */
public class ChunksManifestVerifier {
    private static final Logger log = LoggerFactory.getLogger(ChunksManifestVerifier.class);
    private ContentStore contentStore;

    public ChunksManifestVerifier(ContentStore contentStore) {
        assert contentStore != null;
        this.contentStore = contentStore;
    }

    /**
     * Verifies the bytes and checksums of the chunks specified in the manifest
     * match what is in DuraCloud and returns a listing of the chunks with a flag 
     * indicating whether the check was successful as well as an error message if it
     * was not. You can use the result.isSuccess() method as a shortcut for determining
     * whether all the items in the manifest matched on another.
     * 
     * @param spaceId
     * @param manifest
     * @return a list of results - one for each chunk.
     * @throws ContentStoreException
     */
    public Results verifyAllChunks(String spaceId, ChunksManifest manifest) {

        Results results = new Results();
        for (ManifestEntry entry : manifest.getEntries()) {
            String chunkId = entry.getChunkId();
            String checksum = entry.getChunkMD5();
            long byteSize = entry.getByteSize();

            try {
                Map<String, String> props =
                    this.contentStore.getContentProperties(spaceId,
                                                           entry.getChunkId());
                String remoteChecksum = props.get(ContentStore.CONTENT_CHECKSUM);
                long remoteByteSize = Long.valueOf(props.get(ContentStore.CONTENT_SIZE));

                if(!checksum.equals(remoteChecksum)){
                    results.add(chunkId,
                                "manifest checksum (" + checksum
                                         + ") does not match DuraCloud checksum ("
                                         + remoteChecksum
                                         + ")",
                                false);
                } else if(byteSize != remoteByteSize){
                    results.add(chunkId,
                                "manifest byte size (" + byteSize
                                         + ") does not match DuraCloud byte size ("
                                         + remoteByteSize
                                         + ")",
                                false);
                } else {
                    results.add(chunkId, null, true);
                }

            } catch (Exception ex) {
                results.add(chunkId, ex.getMessage(), false);
            }
        }

        if (CollectionUtils.isNullOrEmpty(results.get())) {
            throw new DuraCloudRuntimeException("failed to retrieve any chunks at list in chunk manifest:  "
                                            + spaceId
                                            + "/"
                                            + manifest.getManifestId());
        }else{
            return results;
        }
    }

    public static final class Results {
        private List<Result> resultList = null;
        private Results() {}
        
        @SuppressWarnings("unused")
        public boolean isSuccess() {
            if (!CollectionUtils.isNullOrEmpty(resultList)) {
                for (Result r : this.resultList) {
                    if (!r.isSuccess()) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        }

        public void add(String chunkId, String error, boolean success) {
            if (this.resultList == null) {
                this.resultList = new LinkedList<>();
            }

            this.resultList.add(new Result(chunkId, error, success));
            
            if(success){
                log.debug("chunk successfully verified: {}", chunkId);
            }else{
                log.warn("unable to verify chunk {} due to : {}",  chunkId, error);
            }

        }

        public List<Result> get() {
            return this.resultList;
        }
    }

    public static final class Result {
        private String chunkId;
        private String error;
        private boolean success;

        private Result(String chunkId, String error, boolean success) {
            this.chunkId = chunkId;
            this.error = error;
            this.success = success;
        }

        public String getChunkId() {
            return chunkId;
        }

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
