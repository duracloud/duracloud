/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.postprocessing;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReducePostJobWorker;
import org.duracloud.services.amazonmapreduce.util.ContentStoreUtil;
import org.duracloud.services.amazonmapreduce.util.ContentStreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
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

    private ContentStoreUtil storeUtil;
    private ContentStreamUtil streamUtil;
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
        this.storeUtil = new ContentStoreUtil(contentStore);
        this.streamUtil = new ContentStreamUtil();
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
        try {
            return storeUtil.getContentStream(spaceId, contentId);

        } catch (DuraCloudRuntimeException e) {
            String msg = e.getMessage();
            InputStream stream = new ByteArrayInputStream(msg.getBytes());
            return new AutoCloseInputStream(stream);
        }
    }

    private void writeToOutputStream(String text, OutputStream outputStream) {
        streamUtil.writeToOutputStream(text, outputStream);
    }

    private void writeToOutputStream(InputStream inputStream,
                                     OutputStream outputStream) {
        streamUtil.writeToOutputStream(inputStream, outputStream);
    }

    private OutputStream createOutputStream(File file) {
        return streamUtil.createOutputStream(file);
    }

    private void deleteOldContent() {
        storeUtil.deleteOldContent(spaceId, contentId);
    }

    private void storeContentStream(File file) {
        storeUtil.storeContentStream(file, spaceId, newContentId);
    }

}