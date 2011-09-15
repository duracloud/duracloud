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
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.core.Ordered;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;
import java.lang.reflect.Method;

public class ContentCopyAdvice implements AfterReturningAdvice, Ordered {

    private final Logger log = LoggerFactory.getLogger(ContentCopyAdvice.class);

    protected static final int STORE_ID_INDEX = 1;

    protected static final int SOURCE_SPACE_ID_INDEX = 2;

    protected static final int SOURCE_CONTENT_ID_INDEX = 3;

    protected static final int DEST_SPACE_ID_INDEX = 4;

    protected static final int DEST_CONTENT_ID_INDEX = 5;

    private JmsTemplate contentJmsTemplate;

    private Destination destination;

    private int order;

    @Override
    public void afterReturning(Object returnObj,
                               Method method,
                               Object[] methodArgs,
                               Object targetObj) throws Throwable {

        if (log.isDebugEnabled()) {
            doLogging(returnObj, method, methodArgs, targetObj);
        }

        publishUpdateEvent(createUpdateMessage(methodArgs));
    }

    private void publishUpdateEvent(ContentCopyMessage updateEvent) {
        getContentJmsTemplate().convertAndSend(getDestination(), updateEvent);
    }

    private ContentCopyMessage createUpdateMessage(Object[] methodArgs) {
        String storeId = getStoreId(methodArgs);
        String sourceSpaceId = getSourceSpaceId(methodArgs);
        String sourceContentId = getSourceContentId(methodArgs);
        String destSpaceId = getDestSpaceId(methodArgs);
        String destContentId = getDestContentId(methodArgs);

        ContentCopyMessage msg = new ContentCopyMessage();
        msg.setStoreId(storeId);
        msg.setSourceSpaceId(sourceSpaceId);
        msg.setSourceContentId(sourceContentId);
        msg.setDestSpaceId(destSpaceId);
        msg.setDestContentId(destContentId);

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

    private void doLogging(Object returnObj,
                           Method method,
                           Object[] methodArgs,
                           Object targetObj) {
        String pre0 = "--------------------------";
        String pre1 = pre0 + "--";
        String pre2 = pre1 + "--";

        log.debug(pre0 + "advice: publish to content copy topic");
        if (targetObj != null && targetObj.getClass() != null) {
            log.debug(pre1 + "object: " + targetObj.getClass().getName());
        }
        if (method != null) {
            log.debug(pre1 + "method: " + method.getName());
        }
        if (methodArgs != null) {
            for (Object obj : methodArgs) {
                String argValue;
                if(obj == null) {
                    argValue = "null";
                } else {
                    argValue = obj.toString();
                }
                log.debug(pre2 + "method-arg: " + argValue);
            }
        }
    }

    public JmsTemplate getContentJmsTemplate() {
        return contentJmsTemplate;
    }

    public void setContentJmsTemplate(JmsTemplate contentJmsTemplate) {
        this.contentJmsTemplate = contentJmsTemplate;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}