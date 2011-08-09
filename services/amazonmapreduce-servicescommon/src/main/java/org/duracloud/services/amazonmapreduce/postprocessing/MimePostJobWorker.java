/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.postprocessing;

import org.apache.commons.io.FilenameUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReducePostJobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is designed to run after the completion of a hadoop job.
 * It iterates the output space of the completed job updating the content item
 * mimetypes based on the mapping defined in this class.
 *
 * @author Andrew Woods
 *         Date: Oct 1, 2010
 */
public class MimePostJobWorker extends BaseAmazonMapReducePostJobWorker {

    private final Logger log = LoggerFactory.getLogger(MimePostJobWorker.class);

    private ContentStore contentStore;
    private String spaceId;

    private Map<String, String> extToMimeMap;
    private static final String DEFAULT_KEY = "default";

    public MimePostJobWorker(AmazonMapReduceJobWorker predecessor,
                             ContentStore contentStore,
                             String spaceId) {
        super(predecessor);
        this.contentStore = contentStore;
        this.spaceId = spaceId;
        this.extToMimeMap = createExtensionToMimeMap();
    }

    private Map<String, String> createExtensionToMimeMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(DEFAULT_KEY, "application/octet-stream");
        map.put("gif", "image/gif");
        map.put("jpg", "image/jpeg");
        map.put("png", "image/png");
        map.put("tiff", "image/tiff");
        map.put("jp2", "image/jp2");
        map.put("bmp", "image/bmp");
        map.put("pdf", "application/pdf");
        map.put("psd", "image/psd");
        map.put("csv", "text/csv");
        map.put("txt", "text/plain");
        return map;
    }

    @Override
    protected void doWork() {
        log.debug("setting content mime types.");

        Iterator<String> destContents = getSpaceContents();
        while (destContents.hasNext() && !getJobStatus().isComplete()) {
            String contentId = destContents.next();
            if (contentId != null) {
                String mimetype = guessMimeType(contentId);
                setContentMimeType(contentId, mimetype);
            }
        }
    }

    private Iterator<String> getSpaceContents() {
        Iterator<String> destContents;
        try {
            destContents = contentStore.getSpaceContents(spaceId);

        } catch (ContentStoreException e) {
            StringBuilder sb = new StringBuilder("Error getting contents of ");
            sb.append("space: ");
            sb.append(spaceId);
            sb.append(", ");
            sb.append(e.getMessage());
            log.error(sb.toString());

            destContents = new ArrayList<String>().iterator();
        }
        return destContents;
    }

    private String guessMimeType(String contentId) {
        if (null == contentId) {
            return extToMimeMap.get(DEFAULT_KEY);
        }

        String ext = FilenameUtils.getExtension(contentId);
        String mimetype = extToMimeMap.get(ext); // HashMap does not choke on null keys.
        if (null == mimetype) {
            mimetype = extToMimeMap.get(DEFAULT_KEY);
        }
        return mimetype;
    }

    private void setContentMimeType(String contentId, String mime) {
        log.debug("setting mime of: " + contentId + ", to: " + mime);

        Map<String, String> properties;
        try {
            properties = contentStore.getContentProperties(spaceId, contentId);
            properties.put(ContentStore.CONTENT_MIMETYPE, mime);
            contentStore.setContentProperties(spaceId, contentId, properties);

        } catch (ContentStoreException e) {
            StringBuilder sb = new StringBuilder("Error setting mimetype for ");
            sb.append("content item: ");
            sb.append(contentId);
            sb.append(" in space: ");
            sb.append(spaceId);
            sb.append(" due to: ");
            sb.append(e.getMessage());
            log.error(sb.toString());
        }
    }

}
