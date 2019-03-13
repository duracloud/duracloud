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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.commons.io.IOUtils;
import org.duracloud.audit.AuditLogUtil;
import org.duracloud.audit.reader.AuditLogReader;
import org.duracloud.audit.reader.AuditLogReaderException;
import org.duracloud.audit.reader.AuditLogReaderNotEnabledException;
import org.duracloud.error.ContentStoreException;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.domain.AuditConfig;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Bernstein
 * Date: Sept. 17, 2014
 */
public class AuditLogReaderImpl implements AuditLogReader {
    private static Logger log =
        LoggerFactory.getLogger(AuditLogReaderImpl.class);

    private AuditConfig auditConfig;

    private StorageProvider storageProvider;

    public AuditLogReaderImpl() {
    }

    @Override
    public void initialize(AuditConfig auditConfig) {
        this.auditConfig = auditConfig;
    }

    @Override
    public InputStream getAuditLog(final String account, final String storeId, final String spaceId)
        throws AuditLogReaderException {

        checkEnabled();

        this.storageProvider = getStorageProvider();
        final String auditBucket = auditConfig.getAuditLogSpaceId();

        String prefix = MessageFormat.format("{0}/{1}/{2}/", account, storeId, spaceId);
        final PipedInputStream is = new PipedInputStream(10 * 1024);
        final PipedOutputStream os;
        try {
            os = new PipedOutputStream(is);
        } catch (IOException e) {
            throw new AuditLogReaderException(e);
        }

        try {
            final Iterator<String> it =
                this.storageProvider.getSpaceContents(auditBucket, prefix);
            if (!it.hasNext()) {
                os.write((AuditLogUtil.getHeader() + "\n").getBytes());
                os.close();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int count = 0;

                        while (it.hasNext()) {
                            String contentId = it.next();
                            writeToOutputStream(auditBucket,
                                                storageProvider,
                                                os,
                                                count,
                                                contentId);

                            count++;
                        }

                        os.close();

                    } catch (ContentStoreException | IOException ex) {
                        log.error(MessageFormat.format("failed to complete audit log read routine " +
                                                       "for space: storeId={0}, spaceId={1}",
                                                       storeId,
                                                       spaceId),
                                  ex);
                    }
                }
            }).start();
        } catch (StorageException | IOException e) {
            throw new AuditLogReaderException(e);
        }

        return is;
    }

    private void checkEnabled() throws AuditLogReaderNotEnabledException {
        if (auditConfig.getAuditLogSpaceId() == null ||
            auditConfig.getAuditQueueName() == null) {
            throw new AuditLogReaderNotEnabledException();
        }
    }

    protected StorageProvider getStorageProvider() {
        AWSCredentials creds = new DefaultAWSCredentialsProviderChain().getCredentials();
        AmazonS3 s3client = AmazonS3ClientBuilder.standard().build();
        return new S3StorageProvider(s3client, creds.getAWSAccessKeyId(), null);
    }

    protected void writeToOutputStream(String auditSpaceId,
                                       StorageProvider storageProvider,
                                       final PipedOutputStream os,
                                       int count,
                                       String contentId)
        throws ContentStoreException,
        IOException {

        try (BufferedReader reader =
                 new BufferedReader(new InputStreamReader(
                     storageProvider.getContent(auditSpaceId, contentId).getContentStream()))) {
            if (count > 0) {
                // skip header if not the first file
                reader.readLine();
            }

            while (true) {
                String line = reader.readLine();
                if (line != null) {
                    IOUtils.write(line + "\n", os);
                } else {
                    break;
                }
            }
        }
    }

}
