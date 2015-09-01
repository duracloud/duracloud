/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import org.apache.commons.lang3.StringUtils;
import org.duracloud.common.constant.ManifestFormat;
import org.duracloud.manifest.ManifestFormatter;
import org.duracloud.manifest.ManifestGenerator;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestGeneratorException;
import org.duracloud.manifest.error.ManifestNotFoundException;
import org.duracloud.mill.db.model.ManifestItem;
import org.duracloud.mill.manifest.ManifestStore;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Iterator;

/**
 * 
 * @author Daniel Bernstein Date: Sept. 16, 2014
 */
public class ManifestGeneratorImpl implements ManifestGenerator {
    private ManifestStore manifestStore;
    private StorageProviderFactory storageProviderFactory;
    private static Logger log =
        LoggerFactory.getLogger(ManifestGeneratorImpl.class);

    public ManifestGeneratorImpl(ManifestStore manifestStore,
                                 StorageProviderFactory storageProviderFactory) {
        super();
        this.manifestStore = manifestStore;
        this.storageProviderFactory = storageProviderFactory;
    }

    @Override
    public InputStream getManifest(String account,
                                   String storeId,
                                   String spaceId,
                                   ManifestFormat format)
        throws ManifestArgumentException,
            ManifestNotFoundException {

        log.info("retrieving manifest for account:{}, storeId:{}, spaceId:{}, format:{}",
                 account,
                 storeId,
                 spaceId,
                 format);
        try {

            storeId = validateStoreId(storeId);
            validateSpaceId(storeId, spaceId);
            PipedInputStream is = new PipedInputStream(10 * 1024);
            final PipedOutputStream os = new PipedOutputStream(is);
            final Iterator<ManifestItem> it =
                this.manifestStore.getItems(account, storeId, spaceId);
            final ManifestFormatter formatter = getFormatter(format);
            if (!it.hasNext()) {
                formatter.writeManifestItemToOutput(null, os);
                os.close();
                return is;
            } else {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (it.hasNext()) {
                                formatter.writeManifestItemToOutput(it.next(),
                                                                    os);
                            }
                            try {
                                os.close();
                            } catch (IOException e) {
                                log.error("failed to close piped output stream : " + e.getMessage(),
                                          e);
                            }

                        } catch (Exception e) {
                            log.error("error writing to piped output stream : " + e.getMessage(),
                                      e);
                        }
                    }
                }).start();
            }

            return is;

        } catch (IOException | RuntimeException ex) {
            log.error("failed to retrieve manifest: " + ex.getMessage(), ex);
            throw new ManifestGeneratorException(ex.getMessage());
        }

    }

    protected void validateSpaceId(String storeId, String spaceId)
        throws ManifestNotFoundException {
        StorageProvider store =
            this.storageProviderFactory.getStorageProvider(storeId);
        try {
            store.getSpaceProperties(spaceId);
        } catch (NotFoundException ex) {
            throw new ManifestNotFoundException("there is no manifest for space: " + spaceId
                                                + " where storeId = "
                                                + storeId
                                                + " : no such space exists.");
        }
    }

    protected String validateStoreId(String storeId)
        throws ManifestArgumentException {
        // validate storeId;
        if (StringUtils.isBlank(storeId)) {

            for (StorageAccount storageAccount : this.storageProviderFactory.getStorageAccounts()) {
                if (storageAccount.isPrimary()) {
                    storeId = storageAccount.getId();
                    break;
                }
            }

            if (StringUtils.isBlank(storeId)) {
                throw new ManifestArgumentException("storeId is blank and " + "no primary storage account is "
                                                    + "indicated for this account.");
            }

        } else {
            boolean matches = false;
            for (StorageAccount storageAccount : this.storageProviderFactory.getStorageAccounts()) {
                if (storageAccount.getId().equals(storeId)) {
                    matches = true;
                    break;
                }
            }

            if (!matches) {
                throw new ManifestArgumentException("The storeId you supplied (" + storeId
                                                    + ") is not associated with this domain.");
            }
        }
        return storeId;
    }

    protected ManifestFormatter getFormatter(final ManifestFormat format)
        throws ManifestArgumentException {
       ManifestFormatterFactory factory = new ManifestFormatterFactory();
       return factory.create(format);
    }

}
