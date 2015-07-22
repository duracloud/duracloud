/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import org.duracloud.chunk.util.ChunkUtil;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

/**
 * Handles the retrieving of a single file from DuraCloud.
 *
 * @author: Erik Paulsson
 * Date: June 27, 2013
 */
public class SpaceListWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SpaceListWorker.class);

    private ContentStore contentStore;
    private String spaceId;
    private File contentDir;
    private File outputFile;
    private boolean overwrite;
    private ChunkUtil chunkUtil;
    
    /**
     * Creates a Space List Worker to handle retrieving a list of space contents
     */
    public SpaceListWorker(ContentStore contentStore,
                           String spaceId,
                           File contentDir,
                           boolean overwrite) {
        this.contentStore = contentStore;
        this.spaceId = spaceId;
        this.contentDir = contentDir;
        this.overwrite = overwrite;
        this.chunkUtil = new ChunkUtil();
    }

    public void run() {
        createSpaceListFile();
    }

    public File getOutputFile() {
        return outputFile;
    }

    protected void createSpaceListFile() {
        try {
            String providerType = contentStore.getStorageProviderType().toLowerCase();
            outputFile = new File(contentDir, spaceId + "-content-listing-" + 
                                   providerType + ".txt");
            if(outputFile.exists() && overwrite) {
                outputFile.delete();
            } else if(outputFile.exists() && !overwrite) {
                outputFile = new File(contentDir, spaceId + "-content-listing-" + 
                                   providerType + "-" + DateUtil.nowPlain() + ".txt");
            }

            System.out.println("Writing space '" + spaceId + "' listing to: " +
                               outputFile.getAbsolutePath());
            Iterator<String> contentIterator = contentStore.getSpaceContents(spaceId);
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFile), "UTF-8"))) {
                while(contentIterator.hasNext()) {
                    String contentId = contentIterator.next();
                    if(!chunkUtil.isChunk(contentId) &&
                       !chunkUtil.isChunkManifest(contentId)) {
                        writer.write(contentId + System.lineSeparator());
                    } else if(chunkUtil.isChunkManifest(contentId)) {
                        writer.write(chunkUtil.preChunkedContentId(contentId) +
                                     System.lineSeparator());
                    }
                }
            }

        } catch(Exception e) {
            String error = "Failed to retrieve content listing for space: '" +
                       spaceId + "'.  Error message: " + e.getMessage();
            System.err.println(error);
            logger.error(error, e);
        }
    }
}