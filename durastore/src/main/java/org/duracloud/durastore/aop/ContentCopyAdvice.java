/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.duracloud.storage.aop.ContentCopyMessage;
import org.duracloud.storage.aop.ContentStoreMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentCopyAdvice extends BaseContentStoreAdvice {

    private final Logger log = LoggerFactory.getLogger(ContentCopyAdvice.class);

    protected static final int STORE_ID_INDEX = 1;
    protected static final int SOURCE_SPACE_ID_INDEX = 2;
    protected static final int SOURCE_CONTENT_ID_INDEX = 3;
    protected static final int DEST_SPACE_ID_INDEX = 4;
    protected static final int DEST_CONTENT_ID_INDEX = 5;
    protected static final int CONTENT_MD5_INDEX = 6;

    @Override
    protected Logger log() {
        return log;
    }

    @Override
    protected ContentStoreMessage createMessage(Object[] methodArgs) {
        String storeId = getStoreId(methodArgs);
        String sourceSpaceId = getSourceSpaceId(methodArgs);
        String sourceContentId = getSourceContentId(methodArgs);
        String destSpaceId = getDestSpaceId(methodArgs);
        String destContentId = getDestContentId(methodArgs);
        String username = getCurrentUsername();
        String contentMd5 = getContentMd5(methodArgs);

        ContentCopyMessage msg = new ContentCopyMessage();
        msg.setStoreId(storeId);
        msg.setSourceSpaceId(sourceSpaceId);
        msg.setSourceContentId(sourceContentId);
        msg.setDestSpaceId(destSpaceId);
        msg.setDestContentId(destContentId);
        msg.setUsername(username);
        msg.setContentMd5(contentMd5);

        return msg;
    }

    private String getStoreId(Object[] methodArgs) {
        log.debug("Returning 'storeId' at index: " + STORE_ID_INDEX);
        return (String) methodArgs[STORE_ID_INDEX];
    }

    private String getSourceSpaceId(Object[] methodArgs) {
        log.debug("Returning source 'spaceId' at index: " +
                  SOURCE_SPACE_ID_INDEX);
        return (String) methodArgs[SOURCE_SPACE_ID_INDEX];
    }

    private String getSourceContentId(Object[] methodArgs) {
        log.debug("Returning source 'contentId' at index: " +
                  SOURCE_CONTENT_ID_INDEX);
        return (String) methodArgs[SOURCE_CONTENT_ID_INDEX];
    }

    private String getDestSpaceId(Object[] methodArgs) {
        log.debug("Returning destination 'spaceId' at index: " +
                  DEST_SPACE_ID_INDEX);
        return (String) methodArgs[DEST_SPACE_ID_INDEX];
    }

    private String getDestContentId(Object[] methodArgs) {
        log.debug("Returning destination 'contentId' at index: " +
                  DEST_CONTENT_ID_INDEX);
        return (String) methodArgs[DEST_CONTENT_ID_INDEX];
    }

    private String getContentMd5(Object[] methodArgs) {
        log.debug("Returning 'contentMd5' at index: " + CONTENT_MD5_INDEX);
        return (String) methodArgs[CONTENT_MD5_INDEX];
    }

}