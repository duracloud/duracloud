/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * @author: Bill Branan
 * Date: Jan 8, 2010
 */
public class ResourceException extends DuraCloudCheckedException {

    private static final String messageKeyNoContent =
        "duracloud.error.durastore.resource.nocontent";

    private static final String messageKeyContent =
        "duracloud.error.durastore.resource.content";

    public ResourceException(String message) {
        super(message);
    }

    public ResourceException(String message, Throwable t) {
        super(message, t);
    }

    public ResourceException(String task,
                             String spaceId,
                             Throwable t) {
        super(buildErrMsg(task, spaceId, t), t, messageKeyContent);
        setArgs(task, spaceId, t.getMessage());
    }

    public ResourceException(String task,
                             String spaceId,
                             String contentId,
                             Throwable t) {
        super(buildErrMsg(task, spaceId, contentId, t), t, messageKeyContent);
        setArgs(task, spaceId, contentId, t.getMessage());
    }

    public ResourceException(String task,
                             String srcStoreId,
                             String srcSpaceId,
                             String srcContentId,
                             String destStoreId,
                             String destSpaceId,
                             String destContentId,
                             Throwable t) {
        super(buildErrMsg(task,
                          srcStoreId,
                          srcSpaceId,
                          srcContentId,
                          destStoreId,
                          destSpaceId,
                          destContentId,
                          t), t, messageKeyContent);
        setArgs(task,
                srcSpaceId,
                srcContentId,
                destSpaceId,
                destContentId,
                t.getMessage());
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

    private static String buildErrMsg(String task,
                                      String srcStoreId,
                                      String srcSpaceId,
                                      String srcContentId,
                                      String destStoreId,
                                      String destSpaceId,
                                      String destContentId,
                                      Throwable t) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("Error attempting to ");
        errMsg.append(task);
        errMsg.append(" '");
        errMsg.append(srcStoreId);
        errMsg.append(" / ");
        errMsg.append(srcSpaceId);
        errMsg.append(" / ");
        errMsg.append(srcContentId);
        errMsg.append("' to '");
        errMsg.append(destStoreId);
        errMsg.append(" / ");
        errMsg.append(destSpaceId);
        errMsg.append(" / ");
        errMsg.append(destContentId);
        errMsg.append("' due to: ");
        errMsg.append(t.getMessage());
        return errMsg.toString();
    }

}