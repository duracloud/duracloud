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

public class SpaceUpdateAdvice
        implements AfterReturningAdvice, Ordered {

    private final Logger log = LoggerFactory.getLogger(SpaceUpdateAdvice.class);

    protected static final int STORE_ID_INDEX = 1;

    protected static final int SPACE_ID_INDEX = 2;

    private JmsTemplate spaceJmsTemplate;

    private Destination destination;

    private int order;

    public void afterReturning(Object returnObj,
                               Method method,
                               Object[] methodArgs,
                               Object targetObj) throws Throwable {

        if (log.isDebugEnabled()) {
            doLogging(returnObj, method, methodArgs, targetObj);
        }

        publishUpdateEvent(createUpdateMessage(methodArgs));
    }

    private void publishUpdateEvent(SpaceMessage updateEvent) {
        getSpaceJmsTemplate().convertAndSend(getDestination(), updateEvent);
    }

    private SpaceMessage createUpdateMessage(Object[] methodArgs) {
        String storeId = getStoreId(methodArgs);
        String spaceId = getSpaceId(methodArgs);

        SpaceMessage msg = new SpaceMessage();
        msg.setStoreId(storeId);
        msg.setSpaceId(spaceId);

        return msg;
    }

    private String getStoreId(Object[] methodArgs) {
        log.debug("Returning 'storeId' at index: " + STORE_ID_INDEX);
        return (String) methodArgs[STORE_ID_INDEX];
    }

    private String getSpaceId(Object[] methodArgs) {
        log.debug("Returning 'spaceId' at index: " + SPACE_ID_INDEX);
        return (String) methodArgs[SPACE_ID_INDEX];
    }

    private void doLogging(Object returnObj,
                           Method method,
                           Object[] methodArgs,
                           Object targetObj) {
        String pre0 = "--------------------------";
        String pre1 = pre0 + "--";
        String pre2 = pre1 + "--";

        log.debug(pre0 + "advice: publish to space update topic");
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

    public JmsTemplate getSpaceJmsTemplate() {
        return spaceJmsTemplate;
    }

    public void setSpaceJmsTemplate(JmsTemplate spaceJmsTemplate) {
        this.spaceJmsTemplate = spaceJmsTemplate;
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