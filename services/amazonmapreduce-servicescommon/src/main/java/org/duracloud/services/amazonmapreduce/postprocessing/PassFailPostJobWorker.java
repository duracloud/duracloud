/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.postprocessing;

import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReducePostJobWorker;
import org.duracloud.services.amazonmapreduce.util.ContentStoreUtil;
import org.duracloud.services.amazonmapreduce.util.ContentStreamUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This PostJobWorker examines the final report of a given service run and
 * pushes any errors found up to the parent service.
 * Implementations of this class need to provide the logic to determine if a
 * given line from the final report contains an error or not.
 *
 * @author Andrew Woods
 *         Date: 6/8/11
 */
public abstract class PassFailPostJobWorker extends BaseAmazonMapReducePostJobWorker {

    private ContentStoreUtil storeUtil;
    private ContentStreamUtil streamUtil;
    private String serviceWorkDir;
    private String spaceId;
    private String contentId;

    public PassFailPostJobWorker(AmazonMapReduceJobWorker predecessor,
                                 ContentStore contentStore,
                                 String serviceWorkDir,
                                 String spaceId,
                                 String contentId) {
        super(predecessor);
        ContentStreamUtil util = new ContentStreamUtil();
        init(contentStore, util, serviceWorkDir, spaceId, contentId);
    }

    public PassFailPostJobWorker(AmazonMapReduceJobWorker predecessor,
                                 ContentStore contentStore,
                                 ContentStreamUtil streamUtil,
                                 String serviceWorkDir,
                                 String spaceId,
                                 String contentId,
                                 long sleepMillis) {
        super(predecessor, sleepMillis);
        init(contentStore, streamUtil, serviceWorkDir, spaceId, contentId);
    }

    private void init(ContentStore contentStore,
                      ContentStreamUtil streamUtil,
                      String serviceWorkDir,
                      String spaceId,
                      String contentId) {
        this.storeUtil = new ContentStoreUtil(contentStore);
        this.streamUtil = streamUtil;

        this.serviceWorkDir = serviceWorkDir;
        this.spaceId = spaceId;
        this.contentId = contentId;
    }

    @Override
    protected void doWork() {
        int errorCount = 0;

        BufferedReader reader = getFileReader(getCachedContent());

        // skip header line
        readLine(reader);

        String line = null;
        while ((line = readLine(reader)) != null) {
            if (isError(line)) {
                errorCount++;
            }
        }

        IOUtils.closeQuietly(reader);

        if (errorCount > 0) {
            super.setError(errorCount + " errors");
        }
    }

    protected abstract boolean isError(String line);

    private BufferedReader getFileReader(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {

        }
        return reader;
    }

    private File getCachedContent() {
        InputStream input = storeUtil.getContentStream(spaceId, contentId);

        File file = new File(serviceWorkDir, contentId);
        OutputStream output = streamUtil.createOutputStream(file);

        streamUtil.writeToOutputStream(input, output);

        IOUtils.closeQuietly(output);
        IOUtils.closeQuietly(input);
        return file;
    }

    private String readLine(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new DuraCloudRuntimeException("Error reading line", e);
        }
    }
}
