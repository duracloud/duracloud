/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.duracloud.common.model.Credential;
import org.duracloud.security.context.SecurityContextUtil;
import org.duracloud.security.error.NoUserLoggedInException;
import org.slf4j.Logger;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.core.Ordered;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;
import java.lang.reflect.Method;

/**
 * This class provides common capabilities for all ContentStore AOP advice.
 *
 * @author Andrew Woods
 *         Date: 3/15/12
 */
public abstract class BaseContentStoreAdvice implements AfterReturningAdvice, Ordered {

    private JmsTemplate jmsTemplate;
    private Destination destination;
    private int order;
    private SecurityContextUtil securityContextUtil;

    @Override
    public void afterReturning(Object returnObj,
                               Method method,
                               Object[] methodArgs,
                               Object targetObj) throws Throwable {

        if (log().isDebugEnabled()) {
            doLogging(returnObj, method, methodArgs, targetObj);
        }

        publishEvent(createMessage(methodArgs));
    }

    /**
     * This method returns the logger from implementing class.
     *
     * @return logger
     */
    protected abstract Logger log();

    /**
     * This method creates an AOP message appropriate to the implementing class.
     *
     * @param methodArgs intercepted from call
     * @return {@link ContentStoreMessage}
     */
    protected abstract ContentStoreMessage createMessage(Object[] methodArgs);

    private void publishEvent(ContentStoreMessage event) {
        getJmsTemplate().convertAndSend(getDestination(), event);
    }

    private void doLogging(Object returnObj,
                           Method method,
                           Object[] methodArgs,
                           Object targetObj) {
        String pre0 = "--------------------------";
        String pre1 = pre0 + "--";
        String pre2 = pre1 + "--";

        log().debug(pre0 + "advice: publish to ingest topic");
        if (targetObj != null && targetObj.getClass() != null) {
            log().debug(pre1 + "object: " + targetObj.getClass().getName());
        }
        if (method != null) {
            log().debug(pre1 + "method: " + method.getName());
        }
        if (methodArgs != null) {
            for (Object obj : methodArgs) {
                String argValue;
                if (obj == null) {
                    argValue = "null";
                } else {
                    argValue = obj.toString();
                }
                log().debug(pre2 + "method-arg: " + argValue);
            }
        }
    }

    protected String getCurrentUsername() {
        String username = null;
        try {
            Credential currentUser = securityContextUtil.getCurrentUser();
            log().debug("Returning 'username': {}", currentUser.getUsername());
            username = currentUser.getUsername();

        } catch (NoUserLoggedInException e) {
            log().warn("Getting current user, error: {}", e);
        }
        return username;
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public SecurityContextUtil getSecurityContextUtil() {
        return securityContextUtil;
    }

    public void setSecurityContextUtil(SecurityContextUtil securityContextUtil) {
        this.securityContextUtil = securityContextUtil;
    }
}
