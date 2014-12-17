/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.util;

import java.io.IOException;
import java.io.InputStream;

import org.duracloud.storage.domain.DuraStoreInitConfig;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.xml.DuraStoreInitDocumentBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class InitConfigParser {
    
    private static Logger log = LoggerFactory.getLogger(InitConfigParser.class);
    
    public static DuraStoreInitConfig parseInitXml(InputStream initXml) {
        DuraStoreInitDocumentBinding binding =
            new DuraStoreInitDocumentBinding();

        DuraStoreInitConfig initConfig;
        try {
            initConfig = binding.createFromXml(initXml);
        } catch (Exception e) {
            String error = "Unable to read DuraStore init due to error: " +
                           e.getMessage();
            log.error(error);
            throw new StorageException(error, e);
        } finally {
            try {
                initXml.close();
            } catch(IOException e) {
                log.warn(e.getMessage());
            }
        }
        return initConfig;
    }
}
