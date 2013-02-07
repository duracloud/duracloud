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

import java.util.Collection;

/**
 * @author: Andrew Woods
 * Date: Aug 4, 2010
 */
public class HashFinderResult implements ServiceResult {

    private static final Logger log =
        LoggerFactory.getLogger(HashFinderResult.class);

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
        results.append(DELIM);
        results.append(contentId);
        results.append(DELIM);
        results.append(hash);
        return results.toString();
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("HashFinderResult [");
        sb.append("spaceId=");
        sb.append(spaceId);
        sb.append(DELIM);
        sb.append("contentId=");
        sb.append(contentId);
        sb.append(DELIM);
        sb.append("hash=");
        sb.append(hash);
        sb.append(DELIM);
        sb.append("success=");
        sb.append(success);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public Collection<ServiceResultItem> getItems() {
        return null;
    }
}
