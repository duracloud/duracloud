/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestAdvice extends BaseContentStoreAdvice {

    private final Logger log = LoggerFactory.getLogger(IngestAdvice.class);

    protected static final int STORE_ID_INDEX = 1;

    protected static final int SPACE_ID_INDEX = 2;

    protected static final int CONTENT_ID_INDEX = 3;

    protected static final int MIMETYPE_INDEX = 4;

    @Override
    protected Logger log() {
        return log;
    }

    @Override
    protected IngestMessage createMessage(Object[] methodArgs) {
        String storeId = getStoreId(methodArgs);
        String contentId = getContentId(methodArgs);
        String contentMimeType = getMimeType(methodArgs);
        String spaceId = getSpaceId(methodArgs);
        String username = getCurrentUsername();

        IngestMessage msg = new IngestMessage();
        msg.setStoreId(storeId);
        msg.setContentId(contentId);
        msg.setContentMimeType(contentMimeType);
        msg.setSpaceId(spaceId);
        msg.setUsername(username);

        return msg;
    }

    private String getStoreId(Object[] methodArgs) {
        log.debug("Returning 'storeId' at index: " + STORE_ID_INDEX);
        return (String) methodArgs[STORE_ID_INDEX];
    }

    private String getContentId(Object[] methodArgs) {
        log.debug("Returning 'contentId' at index: " + CONTENT_ID_INDEX);
        return (String) methodArgs[CONTENT_ID_INDEX];
    }

    private String getMimeType(Object[] methodArgs) {
        log.debug("Returning 'contentMimeType' at index: " + MIMETYPE_INDEX);
        return (String) methodArgs[MIMETYPE_INDEX];
    }

    private String getSpaceId(Object[] methodArgs) {
        log.debug("Returning 'spaceId' at index: " + SPACE_ID_INDEX);
        return (String) methodArgs[SPACE_ID_INDEX];
    }

}
