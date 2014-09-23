/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.reader.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.MessageFormat;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.duracloud.audit.AuditConfig;
import org.duracloud.audit.reader.AuditLogEmptyException;
import org.duracloud.audit.reader.AuditLogReader;
import org.duracloud.audit.reader.AuditLogReaderException;
import org.duracloud.client.ContentStore;
import org.duracloud.client.util.StoreClientUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel Bernstein 
 *         Date: Sept. 17, 2014
 * 
 */
public class AuditLogReaderImpl implements AuditLogReader {
    private static Logger log =
        LoggerFactory.getLogger(AuditLogReaderImpl.class);

    private AuditConfig auditConfig;

    public AuditLogReaderImpl(AuditConfig auditConfig) {
        this.auditConfig = auditConfig;

    }

    @Override
    public InputStream gitAuditLog(final String account, final String storeId, final String spaceId)
        throws AuditLogEmptyException {
        final String auditSpaceId = auditConfig.getSpaceId();
        final ContentStore contentStore = getContentStore(auditConfig);

        String prefix = MessageFormat.format("{0}/{1}/{2}/",account, storeId, spaceId);
        final PipedInputStream is = new PipedInputStream(10 * 1024);
        final PipedOutputStream os;
        try {
            os = new PipedOutputStream(is);
        } catch (IOException e) {
            throw new AuditLogReaderException(e);
        }
        

        try {

            final Iterator<String> it =
                contentStore.getSpaceContents(auditSpaceId, prefix);
            if (!it.hasNext()) {
                throw new AuditLogEmptyException("there are no items logged for storeId: " + storeId
                                                 + " and spaceId: "
                                                 + spaceId);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int count = 0;

                        
                        while (it.hasNext()) {
                            String contentId = it.next();
                            writeToOutputStream(auditSpaceId,
                                                contentStore,
                                                os,
                                                count,
                                                contentId);

                            count++;
                        }

                        os.close();

                    } catch (ContentStoreException | IOException ex) {
                        log.error(MessageFormat.format("failed to complete audit log read routine for space: storeId={0}, spaceId={1}",
                                                       storeId,
                                                       spaceId),
                                  ex);
                    }
                }
            }).start();

        } catch (ContentStoreException e) {
            throw new AuditLogReaderException(e);
        }

        return is;
    }

    protected void writeToOutputStream(String auditSpaceId,
                                       ContentStore contentStore,
                                       final PipedOutputStream os,
                                       int count,
                                       String contentId)
        throws ContentStoreException,
            IOException {
        Content content = contentStore.getContent(auditSpaceId, contentId);
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(content.getStream()));
        if (count > 0) {
            // skip header if not hte first file
            reader.readLine();
        }

        while (true) {
            String line = reader.readLine();
            if (line != null) {
                IOUtils.write(line+"\n", os);
            } else {
                break;
            }
        }
    }

    protected ContentStore getContentStore(AuditConfig auditConfig) {
        StoreClientUtil storeUtil = new StoreClientUtil();
        ContentStore store =
            storeUtil.createContentStore(auditConfig.getDuracloudHost(),
                                         auditConfig.getDuracloudPort(),
                                         "durastore",
                                         auditConfig.getDuracloudUsername(),
                                         auditConfig.getDuracloudPassword(),
                                         auditConfig.getStoreId());
        return store;
    }
}
