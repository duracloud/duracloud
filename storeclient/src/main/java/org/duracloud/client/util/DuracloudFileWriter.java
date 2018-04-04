/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;

import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class encapsulates writing a simple text file and saving it as a content
 * item.
 *
 * @author Danny Bernstein
 * Date: Jan 4, 2011
 */
public class DuracloudFileWriter extends Writer {
    private static Logger log = LoggerFactory.getLogger(DuracloudFileWriter.class);
    private String contentId;
    private String spaceId;
    private String storeId;
    private String mimetype;
    private ContentStoreUtil contentStoreUtil;

    private File tempFile;
    private Writer writer;

    /**
     * @param spaceId      of the space in which you would like to persist the text.
     * @param contentId    of the item you would like to store the text in.
     * @param contentStore to which you would like write the stream.
     * @throws DuraCloudRuntimeException
     */
    public DuracloudFileWriter(String spaceId,
                               String contentId,
                               String mimetype,
                               ContentStore contentStore) throws DuraCloudRuntimeException {
        throwIllegalArgumentExceptionIfNull(spaceId, "spaceId");
        throwIllegalArgumentExceptionIfNull(spaceId, "contentId");
        throwIllegalArgumentExceptionIfNull(mimetype, "mimetype");
        throwIllegalArgumentExceptionIfNull(contentStore, "contentStore");
        try {
            this.contentId = contentId;
            this.spaceId = spaceId;
            this.mimetype = mimetype;
            this.tempFile = File.createTempFile("dc-content-item-writer", null);
            this.contentStoreUtil = new ContentStoreUtil(contentStore);
            this.storeId = contentStore.getStoreId();
            this.writer = new FileWriter(tempFile);
        } catch (IOException e) {
            log.error("constructor failed", e);
            throw new DuraCloudRuntimeException(e);
        }

    }

    private void throwIllegalArgumentExceptionIfNull(Object value,
                                                     String varName) {
        if (value == null) {
            throw new IllegalArgumentException(MessageFormat.format("{0} must not be null", new Object[] {varName}));
        }
    }

    /**
     * Writes the tempfile to durastore.
     */
    @Override
    public void flush() throws IOException {
        checkWriter();
        writer.flush();
        try {
            contentStoreUtil.storeContentStream(tempFile,
                                                spaceId,
                                                contentId,
                                                mimetype);
        } catch (DuraCloudRuntimeException ex) {
            throw new IOException("flush failed: " + ex.getMessage(), ex);
        }
    }

    private void checkWriter() throws IOException {
        if (writer == null) {
            throw new IOException("write has been closed");
        }
    }

    @Override
    public void close() throws IOException {
        checkWriter();
        writer.close();
        try {
            contentStoreUtil.storeContentStream(tempFile, spaceId, contentId, mimetype);
        } catch (DuraCloudRuntimeException ex) {
            String msg =
                MessageFormat.format("failed to save file ( contentId={}, spaceId={}, storeId={}, mimetype={} )",
                                     new Object[] {
                                         contentId,
                                         spaceId,
                                         storeId,
                                         mimetype});
            log.error(msg, ex);
            throw new IOException(msg, ex);
        } finally {
            this.tempFile.delete();
            this.tempFile = null;
            this.writer = null;
        }
    }

    /**
     * Same as write but appends a new line to the end of the supplied line.
     *
     * @param line
     * @throws DuraCloudRuntimeException
     */
    public void writeLine(String line) throws IOException {
        write(line + "\n");
    }

    @Override
    public void write(String line) throws IOException {
        if (writer == null) {
            throw new IOException("The writer has already been closed.");
        }

        writer.write(line);
    }

    @Override
    protected void finalize() throws Throwable {
        if (this.tempFile != null) {
            this.tempFile.delete();
        }

        super.finalize();

    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }

}
