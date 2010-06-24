/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * Exception thrown by the ContentStore.
 *
 * @author Bill Branan
 */
public class ContentStoreException extends DuraCloudCheckedException {

    private static final String messageKeyNoContent =
        "duracloud.error.durastore.resource.nocontent";

    private static final String messageKeyContent =
        "duracloud.error.durastore.resource.content";    

    public ContentStoreException (String message) {
        super(message);
    }

    public ContentStoreException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ContentStoreException(Throwable throwable) {
        super(throwable);
    }

    public ContentStoreException(String message,
                                 Throwable t,
                                 String messageKey) {
        super(message, t, messageKey);
    }

    public ContentStoreException(String task, String spaceId, Throwable t) {
        super(buildErrMsg(task, spaceId, t), t, messageKeyNoContent);
        setArgs(task, spaceId, t.getMessage());
    }

    public ContentStoreException(String task,
                                 String spaceId,
                                 String contentId,
                                 Throwable t) {
        super(buildErrMsg(task, spaceId, contentId, t), t, messageKeyContent);
        setArgs(task, spaceId, contentId, t.getMessage());
    }

    private static String buildErrMsg(String task,
                                      String spaceId,
                                      Throwable t) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("Error attempting to ");
        errMsg.append(task);
        errMsg.append(" '");
        errMsg.append(spaceId);
        errMsg.append("' due to: ");
        errMsg.append(t.getMessage());
        return errMsg.toString();
    }

    private static String buildErrMsg(String task,
                                      String spaceId,
                                      String contentId,
                                      Throwable t) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("Error attempting to ");
        errMsg.append(task);
        errMsg.append(" '");
        errMsg.append(contentId);
        errMsg.append("' in '");
        errMsg.append(spaceId);
        errMsg.append("' due to: ");
        errMsg.append(t.getMessage());
        return errMsg.toString();
    }

}
