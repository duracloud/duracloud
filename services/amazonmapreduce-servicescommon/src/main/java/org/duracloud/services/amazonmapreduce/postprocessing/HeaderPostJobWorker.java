/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.postprocessing;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReducePostJobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is designed to run after the completion of a hadoop job.
 * It reads the content item located at the provided 'spaceId' and 'contentId'
 * and prepends that content with the provided 'header'.
 * This new content then overwrites the original content item.
 *
 * @author Andrew Woods
 *         Date: Oct 1, 2010
 */
public class HeaderPostJobWorker extends BaseAmazonMapReducePostJobWorker {

    private final Logger log = LoggerFactory.getLogger(HeaderPostJobWorker.class);

    private ContentStore contentStore;
    private String serviceWorkDir;
    private String spaceId;
    private String contentId;
    private String newContentId;
    private String header;

    public HeaderPostJobWorker(AmazonMapReduceJobWorker predecessor,
                               ContentStore contentStore,
                               String serviceWorkDir,
                               String spaceId,
                               String contentId,
                               String newContentId,
                               String header) {
        super(predecessor);
        init(contentStore, serviceWorkDir, spaceId, contentId, newContentId, header);
    }

    public HeaderPostJobWorker(AmazonMapReduceJobWorker predecessor,
                               ContentStore contentStore,
                               String serviceWorkDir,
                               String spaceId,
                               String contentId,
                               String newContentId,
                               String header,
                               long sleepMillis) {
        super(predecessor, sleepMillis);
        init(contentStore, serviceWorkDir, spaceId, contentId, newContentId, header);
    }

    private void init(ContentStore contentStore,
                      String serviceWorkDir,
                      String spaceId,
                      String contentId,
                      String newContentId,
                      String header) {
        this.contentStore = contentStore;
        this.serviceWorkDir = serviceWorkDir;
        this.spaceId = spaceId;
        this.contentId = contentId;
        this.newContentId = newContentId;
        this.header = header;
    }

    @Override
    protected void doWork() {
        InputStream originalStream = getContentStream();

        File fileWithHeader = new File(serviceWorkDir, contentId);
        OutputStream outputStream = createOutputStream(fileWithHeader);

        writeToOutputStream(header, outputStream);
        writeToOutputStream(System.getProperty("line.separator"), outputStream);
        writeToOutputStream(originalStream, outputStream);

        IOUtils.closeQuietly(outputStream);
        IOUtils.closeQuietly(originalStream);

        storeContentStream(fileWithHeader);

        deleteOldContent();
    }

    private InputStream getContentStream() {
        Content content = getContent();
        if (null == content) {
            StringBuilder sb = new StringBuilder("Error: content is null: ");
            sb.append(spaceId);
            sb.append("/");
            sb.append(contentId);
            log.error(sb.toString());
            throw new DuraCloudRuntimeException(sb.toString());
        }
        return new AutoCloseInputStream(content.getStream());
    }

    private void writeToOutputStream(String text, OutputStream outputStream) {
        try {
            IOUtils.write(text, outputStream);

        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Error ");
            sb.append("writing to outputstream.");
            log.error(sb.toString());
            throw new DuraCloudRuntimeException(sb.toString(), e);
        }
    }

    private void writeToOutputStream(InputStream inputStream,
                                     OutputStream outputStream) {
        try {
            IOUtils.copy(inputStream, outputStream);

        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Error ");
            sb.append("copying from inputstream to outputstream.");
            log.error(sb.toString());
            throw new DuraCloudRuntimeException(sb.toString(), e);
        }
    }

    private OutputStream createOutputStream(File file) {
        try {
            return FileUtils.openOutputStream(file);

        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Error ");
            sb.append("creating outputstream: ");
            sb.append(file.getPath());
            log.error(sb.toString());
            throw new DuraCloudRuntimeException(sb.toString(), e);
        }

    }

    private Content getContent() {
        try {
            return contentStore.getContent(spaceId, contentId);

        } catch (ContentStoreException e) {
            StringBuilder sb = new StringBuilder("Error: ");
            sb.append("getting content: ");
            sb.append(spaceId);
            sb.append("/");
            sb.append(contentId);
            sb.append(": ");
            sb.append(e.getMessage());
            log.error(sb.toString());

            throw new DuraCloudRuntimeException(sb.toString(), e);
        }
    }

    private void deleteOldContent() {
        try {
            contentStore.deleteContent(spaceId, contentId);

        } catch (ContentStoreException e) {
            StringBuilder sb = new StringBuilder("Error: ");
            sb.append("deleting content: ");
            sb.append(spaceId);
            sb.append("/");
            sb.append(contentId);
            sb.append(": ");
            sb.append(e.getMessage());
            log.error(sb.toString());
        }
    }

    private void storeContentStream(File file) {
        log.debug("storing content to storage-provider: " + file.getPath());
        try {
            contentStore.addContent(spaceId,
                                    newContentId,
                                    new FileInputStream(file),
                                    file.length(),
                                    null,
                                    null,
                                    null);

        } catch (ContentStoreException e) {
            StringBuilder sb = new StringBuilder("Error adding content: ");
            sb.append(spaceId);
            sb.append("/");
            sb.append(newContentId);
            sb.append(", from: ");
            sb.append(file.getPath());
            log.error(sb.toString());
            throw new DuraCloudRuntimeException(sb.toString(), e);

        } catch (FileNotFoundException e) {
            StringBuilder sb = new StringBuilder("Error finding file from: ");
            sb.append(", from: ");
            sb.append(file.getPath());
            log.error(sb.toString());
            throw new DuraCloudRuntimeException(sb.toString(), e);
        }
    }

}