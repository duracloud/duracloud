/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.bulkimageconversion;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceJobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Performs work after the hadoop job has completed
 *
 * @author: Bill Branan
 * Date: Aug 26, 2010
 */
public class PostJobWorker implements AmazonMapReduceJobWorker {

    private final Logger log = LoggerFactory.getLogger(PostJobWorker.class);

    private AmazonMapReduceJobWorker jobWorker;
    private ContentStore contentStore;
    private String toFormat;
    private String destSpaceId;

    private JobStatus status;
    private String mimetype;

    public PostJobWorker(AmazonMapReduceJobWorker jobWorker,
                         ContentStore contentStore,
                         String toFormat,
                         String destSpaceId) {
        this.jobWorker = jobWorker;
        this.contentStore = contentStore;
        this.toFormat = toFormat;
        this.destSpaceId = destSpaceId;

        status = JobStatus.WAITING;
        Map<String, String> extMimeMap = createExtMimeMap();
        mimetype = extMimeMap.get(toFormat);
        if (mimetype == null) {
            mimetype = extMimeMap.get("default");
        }
    }

    /*
     * Load supported file types: gif, jpg, png, tiff, jp2, bmp, pdf, psd
     */

    private Map<String, String> createExtMimeMap() {
        Map<String, String> extMimeMap = new HashMap<String, String>();
        extMimeMap.put("default", "application/octet-stream");
        extMimeMap.put("gif", "image/gif");
        extMimeMap.put("jpg", "image/jpeg");
        extMimeMap.put("png", "image/png");
        extMimeMap.put("tiff", "image/tiff");
        extMimeMap.put("jp2", "image/jp2");
        extMimeMap.put("bmp", "image/bmp");
        extMimeMap.put("pdf", "application/pdf");
        extMimeMap.put("psd", "image/psd");
        return extMimeMap;
    }

    @Override
    public void run() {
        while (!getJobStatus().isComplete()) {
            if (jobWorker.getJobStatus().isComplete()) {
                status = JobStatus.POST_PROCESSING;
                setContentMimeTypes();
                status = JobStatus.COMPLETE;

            } else {
                log.debug("waiting for job-worker to complete.");
                sleep(120000); // wait 2 min
            }
        }
    }

    private void setContentMimeTypes() {
        log.debug("setting content mime types.");

        Iterator<String> destContents = null;
        try {
            destContents = contentStore.getSpaceContents(destSpaceId);
            if (destContents != null) {
                while (destContents.hasNext()) {
                    String contentId = destContents.next();
                    if (contentId != null) {
                        if (contentId.endsWith(toFormat)) {
                            setContentMimeType(contentId, mimetype);
                        } else if (contentId.endsWith(".csv")) {
                            setContentMimeType(contentId, "text/csv");
                        }
                    }
                }
            } else {
                log.warn("no items found in dest-space: " + destSpaceId);
            }
        } catch (ContentStoreException e) {
            log.error("Could not set destination content mime types due to " +
                e.getMessage());
        }
    }

    private void setContentMimeType(String contentId, String mime) {
        log.debug("setting mime of: " + contentId + ", to: " + mime);

        try {
            Map<String, String> metadata = contentStore.getContentMetadata(
                destSpaceId,
                contentId);
            metadata.put(ContentStore.CONTENT_MIMETYPE, mime);
            contentStore.setContentMetadata(destSpaceId, contentId, metadata);
        } catch (ContentStoreException e) {
            log.error("Could not set mimetype for content item " + contentId +
                " in " + destSpaceId + " due to " + e.getMessage());
        }
    }

    @Override
    public JobStatus getJobStatus() {
        return status;
    }

    @Override
    public Map<String, String> getJobDetailsMap() {
        return new HashMap<String, String>();
    }

    @Override
    public String getJobId() {
        throw new UnsupportedOperationException("getJobId() not supported");
    }

    @Override
    public String getError() {
        return null;
    }

    public void shutdown() {
        status = JobStatus.COMPLETE;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing.
        }
    }

}