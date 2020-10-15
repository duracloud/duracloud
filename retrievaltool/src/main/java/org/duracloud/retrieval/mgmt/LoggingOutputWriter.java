/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import org.apache.commons.lang3.StringUtils;
import org.duracloud.common.model.ContentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bill Branan
 * Date: 7/23/2015
 */
public class LoggingOutputWriter implements OutputWriter {

    private Logger log = LoggerFactory.getLogger("retrieval-log");

    protected static final String SUCCESS = "RETRIEVED";
    protected static final String FAILURE = "FAILED";
    protected static final String MISSING = "MISSING";

    @Override
    public void writeSuccess(ContentItem contentItem, String localFilePath,
                             int attempts) {
        log.info(format(new Object[] {
            SUCCESS, contentItem.getSpaceId(), contentItem.getContentId(),
            localFilePath}));
    }

    private String format(Object[] objects) {
        return StringUtils.join(objects, "\t");
    }

    @Override
    public void writeFailure(ContentItem contentItem, String error, int attempts) {
        log.info(format(new Object[] {
            FAILURE, contentItem.getSpaceId(), contentItem.getContentId(),
            "attempts:" + attempts, "error:" + error}));
    }

    @Override
    public void writeMissing(ContentItem contentItem, String message, int attempts) {
        log.info(format(new Object[] {
            MISSING, contentItem.getSpaceId(), contentItem.getContentId(),
            "attempts:" + attempts, "message:" + message}));
    }

    @Override
    public void close() {
    }
}
