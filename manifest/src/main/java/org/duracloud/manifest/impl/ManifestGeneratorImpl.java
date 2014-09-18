/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Iterator;

import org.duracloud.manifest.ManifestFormatter;
import org.duracloud.manifest.ManifestGenerator;
import org.duracloud.manifest.ManifestGenerator.FORMAT;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestEmptyException;
import org.duracloud.manifest.error.ManifestGeneratorException;
import org.duracloud.mill.db.model.ManifestItem;
import org.duracloud.mill.manifest.ManifestStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel Bernstein
 *         Date: Sept. 16, 2014
 */
public class ManifestGeneratorImpl implements ManifestGenerator{
    private ManifestStore manifestStore;
    private static Logger log = LoggerFactory.getLogger(ManifestGeneratorImpl.class);
    
    public ManifestGeneratorImpl(ManifestStore manifestStore) {
        super();
        this.manifestStore = manifestStore;
    }

    @Override
    public InputStream getManifest(final String storeId,
                                   final String spaceId,
                                   final FORMAT format)
        throws ManifestArgumentException,
            ManifestEmptyException {
        
        log.info("retrieving manifest for storeId:{}, spaceId:{}, format:{}, ignoring asOfDate: {}");
        try{
            PipedInputStream is = new PipedInputStream(10*1024);
            final PipedOutputStream os =  new PipedOutputStream(is);
            final Iterator<ManifestItem> it = this.manifestStore.getItems(storeId, spaceId);
            final ManifestFormatter formatter = getFormatter(format);
            if (!it.hasNext()) {
                throw new ManifestEmptyException("the manifest for  storeId:"
                    + storeId + ", spaceId:" + spaceId + " is empty.");
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (it.hasNext()) {
                            formatter.writeManifestItemToOutput(it.next(), os);
                        }
                        try {
                            os.close();
                        } catch (IOException e) {
                            log.error("failed to close piped output stream : " + e.getMessage(), e);
                        }

                    } catch (Exception e) {
                        log.error("error writing to piped output stream : " + e.getMessage(), e);
                    } 
                }
            }).start();
            return is;
            
        }catch(IOException | RuntimeException ex){
            log.error("failed to retrieve manifest: " + ex.getMessage(), ex);
            throw new ManifestGeneratorException(ex.getMessage());
        }
        
    }

    protected ManifestFormatter getFormatter(final FORMAT format)
        throws ManifestArgumentException {
        ManifestFormatter formatter;
        switch (format) {
            case BAGIT:
                formatter = new BagitManifestFormatter();
                break;
            case TSV:
                formatter = new TsvManifestFormatter();
                break;
            default:
                String err = "Unexpected format: " + format.name();
                log.error(err);
                throw new ManifestArgumentException(err);
        }
        
        return formatter;
    }
    
}
