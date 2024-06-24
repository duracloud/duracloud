/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.chunk;

import org.duracloud.chunk.FileChunkerOptions;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shake
 */
public class ChunkingContentStoreManagerImpl extends ContentStoreManagerImpl {

    private static final Logger log = LoggerFactory.getLogger(ChunkingContentStoreManagerImpl.class);

    private FileChunkerOptions options;

    public ChunkingContentStoreManagerImpl(String host, String port) {
        this(host, port, 1000L * 1000 * 1000);
    }

    public ChunkingContentStoreManagerImpl(String host,
                                           String port,
                                           long maxFileSize) {
        super(host, port);
        this.options = new FileChunkerOptions(maxFileSize);

    }

    @Override
    public ContentStore getPrimaryContentStore() throws ContentStoreException {
        return getPrimaryContentStore(-1);
    }

    @Override
    public ContentStore getPrimaryContentStore(int maxRetries) throws ContentStoreException {
        StorageAccountManager acctManager = getStorageAccounts();
        StorageAccount acct = acctManager.getPrimaryStorageAccount();
        return newContentStoreImpl(acct, maxRetries);
    }

    @Override
    protected ContentStore newContentStoreImpl(StorageAccount acct, int maxRetries) {
        log.info("Creating new ChunkingContentStoreImpl");
        return new ChunkingContentStoreImpl(getBaseURL(),
                                    acct.getType(),
                                    acct.getId(),
                                    isWritable(acct),
                                    getRestHelper(),
                                    maxRetries,
                                    options);
    }

}
