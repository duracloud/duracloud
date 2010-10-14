/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.store;

import org.apache.commons.io.FilenameUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Oct 12, 2010
 */
public class MimeTypeUtil {

    private static final Map<String, String> extToMimeMap = createExtensionToMimeMap();
    private static final String DEFAULT_KEY = "default";

    private static Map<String, String> createExtensionToMimeMap() {
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

    public String guessMimeType(String contentId) {
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
}
