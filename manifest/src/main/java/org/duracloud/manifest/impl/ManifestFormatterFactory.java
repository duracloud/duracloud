/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import org.duracloud.common.constant.ManifestFormat;
import org.duracloud.manifest.ManifestFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates instances of ManifestFormatters based on ManifestFormat enum values.
 * @author Daniel Bernstein
 *
 */
public class ManifestFormatterFactory {
    private Logger log = LoggerFactory.getLogger(ManifestFormatterFactory.class);
    public ManifestFormatterFactory(){
        
    }
    
    public ManifestFormatter create(ManifestFormat format){
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
                throw new RuntimeException(err);
        }

        return formatter;
    }
}
