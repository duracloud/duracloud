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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Performs work after the hadoop job has completed
 *
 * @author: Bill Branan
 * Date: Aug 26, 2010
 */
public class PostJobWorker implements Runnable {

    private BulkImageConversionService service;
    private ContentStore contentStore;
    private String toFormat;
    private String destSpaceId;

    private boolean workComplete;
    private String mimetype;

    public PostJobWorker(BulkImageConversionService service,
                         ContentStore contentStore,
                         String toFormat,
                         String destSpaceId) {
        this.service = service;
        this.contentStore = contentStore;
        this.toFormat = toFormat;
        this.destSpaceId = destSpaceId;

        workComplete = false;
        Map<String, String> extMimeMap = createExtMimeMap();
        mimetype = extMimeMap.get(toFormat);
        if(mimetype == null) {
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
        while(!workComplete) {
            if(service.jobComplete()) {
                setContentMimeTypes();
                workComplete = true;
            } else {
                sleep(120000); // wait 2 min
            }
        }
    }

    private void setContentMimeTypes() {
        Iterator<String> destContents = null;
        try {
            destContents = contentStore.getSpaceContents(destSpaceId);
            if(destContents != null) {
                while(destContents.hasNext()) {
                    String contentId = destContents.next();
                    if(contentId != null) {
                        if(contentId.endsWith(toFormat)) {
                            setContentMimeType(contentId, mimetype);
                        } else if(contentId.endsWith(".csv")) {
                            setContentMimeType(contentId, "text/csv");
                        }
                    }
                }
            }
        } catch(ContentStoreException e) {
            System.out.println("Could not set destination content mime " +
                               "types due to " + e.getMessage());
        }
    }

    private void setContentMimeType(String contentId, String mime) {
        try {
            Map<String, String> metadata =
                contentStore.getContentMetadata(destSpaceId, contentId);
            metadata.put(ContentStore.CONTENT_MIMETYPE, mime);
            contentStore.setContentMetadata(destSpaceId, contentId, metadata);
        } catch(ContentStoreException e) {
            System.out.println("Could not set mimetype for content item " +
                               contentId + " in " + destSpaceId +
                               " due to " + e.getMessage());
        }
    }

    public boolean isComplete() {
        return workComplete;
    }

    public void shutdown() {
        workComplete = true;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing.
        }
    }

}