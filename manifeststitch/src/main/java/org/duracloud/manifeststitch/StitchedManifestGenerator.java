/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifeststitch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean.ManifestHeader;
import org.duracloud.chunk.manifest.xml.ManifestDocumentBinding;
import org.duracloud.client.ContentStore;
import org.duracloud.common.constant.ManifestFormat;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.manifest.ManifestFormatter;
import org.duracloud.manifest.impl.ManifestFormatterFactory;
import org.duracloud.mill.db.model.ManifestItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes a manifest and generates a "stitched" view by 1) writing out
 * all the unchunked items as they appear in the original manifest, 2) filtering
 * out all chunks, and 3) reading and parsing from any *.dura-manifest files the
 * checksum of the stitched file.
 * 
 * @author Daniel Bernstein Date: 08/28/2015
 */
public class StitchedManifestGenerator {
    private Logger log =
        LoggerFactory.getLogger(StitchedManifestGenerator.class);
    private ContentStore store;

    public StitchedManifestGenerator(ContentStore store) {
        this.store = store;
    }

    public InputStream generate(String spaceId, ManifestFormat format) throws IOException  {

        final File stitchedManifestFile =
            File.createTempFile("stitched-manifest-" + spaceId,
                                "." + format.name().toLowerCase());
        //download manifest and process each line.
        try {
            InputStream manifest = store.getManifest(spaceId, format);
            BufferedReader reader = new BufferedReader(new InputStreamReader(manifest));
            
            BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stitchedManifestFile)));
            ManifestFormatter formatter = new ManifestFormatterFactory().create(format);
            String header = formatter.getHeader();
            String line = null;
            try {
                while((line = reader.readLine()) != null){
                    //ignore any whitespace
                    if(line.trim().length() == 0){
                        continue;
                    }
                    
                    //write header if there is one.
                    if(header != null && line.equals(header)){
                        writeLine(line, writer);
                        continue;
                    }
                    
                    //process the line
                    processLine(line, formatter, writer);
                }
            } catch (IOException e) {
                log.error("failed to complete manifest stiching.", e);
            }finally{
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error("failed to close piped input stream", e);
                }
            }
        } catch (ContentStoreException e) {
            log.error("failed to generate stitched manifest: " + e.getMessage(), e);
            throw new IOException(e);
        }
        
        return  new AutoCloseInputStream(new FileInputStream(stitchedManifestFile){
            @Override
            public void close() throws IOException {
                super.close();
                stitchedManifestFile.delete();
            }
        });
    }

    private void processLine(String line,
                             ManifestFormatter formatter,
                             BufferedWriter writer)
                                 throws IOException,
                                     ContentStoreException {
        // parse manifest entry
        ManifestItem item = null;
        try {
            item = formatter.parseLine(line);
        } catch (ParseException e) {
            throw new IOException(e);
        }
        // if .dura-manifest file process accordingly
        String contentId = item.getContentId();

        if (contentId.endsWith(ChunksManifest.manifestSuffix)) {
            writeLine(processChunkManifest(item, formatter), writer);
        } else if (contentId.contains(ChunksManifest.chunkSuffix)) {
            // ignore chunks
            return;
        } else {
            // else write it.
            writeLine(line, writer);
        }
    }

    private String processChunkManifest(ManifestItem item,
                                        ManifestFormatter formatter) throws ContentStoreException {
        String contentId = item.getContentId();
        String spaceId = item.getSpaceId();
        
        //extract checksum from chunk manifest.
        Content content = store.getContent(spaceId, contentId);
        
        ChunksManifest chunkManifest =
            ManifestDocumentBinding.createManifestFrom(content.getStream());
        
        ManifestHeader header = chunkManifest.getHeader();
        String checksum = header.getSourceMD5();
        String newContentId  = header.getSourceContentId();
        ManifestItem newItem = new ManifestItem();
        newItem.setSpaceId(spaceId);
        newItem.setContentId(newContentId);
        newItem.setContentChecksum(checksum);
        
        //retrieve new 
        return formatter.formatLine(newItem);
    }

    protected void writeLine(String line, BufferedWriter writer)
        throws IOException {
        writer.write(line);
        writer.newLine();
    }

}
