/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Andrew Woods
 * Date: Aug 4, 2010
 */
public class HashFinderResult implements ServiceResult {

    private static final Logger log = LoggerFactory.getLogger(HashFinderResult.class);

    private static String newline = System.getProperty("line.separator");
    public static final String HEADER = "space-id,content-id,MD5";

    private String spaceId;
    private String contentId;
    private String hash;
    private boolean success;

    public HashFinderResult(boolean success,
                        String spaceId,
                        String contentId,
                        String hash) {
        this.success = success;
        this.spaceId = spaceId;
        this.contentId = contentId;
        this.hash = hash;
    }

    @Override
    public String getEntry() {
        StringBuffer results = new StringBuffer();
        results.append(spaceId);
        results.append(",");
        results.append(contentId);
        results.append(",");
        results.append(hash);
        results.append(newline);
        return results.toString();
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
}
