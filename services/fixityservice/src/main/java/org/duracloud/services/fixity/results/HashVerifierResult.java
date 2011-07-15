/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;

import org.duracloud.services.fixity.domain.ContentLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Andrew Woods
 * Date: Aug 12, 2010
 */
public class HashVerifierResult implements ServiceResult {

    private static final Logger log = LoggerFactory.getLogger(HashVerifierResult.class);

    private boolean success;
    private ContentLocation contentLocationA;
    private ContentLocation contentLocationB;
    private String text;

    public HashVerifierResult(boolean success,
                              ContentLocation contentLocationA,
                              ContentLocation contentLocationB,
                              String text) {
        this.success = success;
        this.contentLocationA = contentLocationA;
        this.contentLocationB = contentLocationB;
        this.text = text;
    }

    @Override
    public String getEntry() {
        return text;
    }

    @Override
    public String getHeader() {
        String locA = contentLocationA.getSpaceId() + "/" +
            contentLocationA.getContentId();
        String locB = contentLocationB.getSpaceId() + "/" +
            contentLocationB.getContentId();
        return "space-id" + DELIM + "content-id" + DELIM + "0:" + locA + DELIM +
            "1:" + locB + DELIM + "status";
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
}
