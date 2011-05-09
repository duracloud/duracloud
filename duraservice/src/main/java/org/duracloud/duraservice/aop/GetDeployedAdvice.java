/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.core.Ordered;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;
import java.lang.reflect.Method;

public class GetDeployedAdvice
        implements AfterReturningAdvice, Ordered {

    private final Logger log = LoggerFactory.getLogger(GetDeployedAdvice.class);

    protected static final int SERVICE_ID_INDEX = 0;

    protected static final int DEPLOYMENT_ID_INDEX = 1;

    private JmsTemplate getDeployedJmsTemplate;

    private Destination destination;

    private int order;

    public void afterReturning(Object returnObj,
                               Method method,
                               Object[] methodArgs,
                               Object targetObj) throws Throwable {

        if (log.isDebugEnabled()) {
            doLogging(returnObj, method, methodArgs, targetObj);
        }

        publishGetDeployedEvent(createServiceMessage(methodArgs));
    }

    private void publishGetDeployedEvent(ServiceMessage serviceMessage) {
        getGetDeployedJmsTemplate().convertAndSend(getDestination(), serviceMessage);
    }

    private ServiceMessage createServiceMessage(Object[] methodArgs) {
        String serviceId = getServiceId(methodArgs);
        String deploymentId = getDeploymentId(methodArgs);

        ServiceMessage msg = new ServiceMessage();
        msg.setServiceId(serviceId);
        msg.setDeploymentId(deploymentId);

        return msg;
    }

    private String getServiceId(Object[] methodArgs) {
        log.debug("Returning 'serviceId' at index: " + SERVICE_ID_INDEX);
        Integer id = ((Integer) methodArgs[SERVICE_ID_INDEX]);
        if(id != null)
            return id.toString();
        return null;
    }

    private String getDeploymentId(Object[] methodArgs) {
        log.debug("Returning 'deploymentId' at index: " + DEPLOYMENT_ID_INDEX);
        Integer id = ((Integer) methodArgs[DEPLOYMENT_ID_INDEX]);
        if(id != null)
            return id.toString();
        return null;
    }

    private void doLogging(Object returnObj,
                           Method method,
                           Object[] methodArgs,
                           Object targetObj) {
        String pre0 = "--------------------------";
        String pre1 = pre0 + "--";
        String pre2 = pre1 + "--";

        log.debug(pre0 + "advice: publish to getDeployed topic");
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

    public JmsTemplate getGetDeployedJmsTemplate() {
        return getDeployedJmsTemplate;
    }

    public void setGetDeployedJmsTemplate(JmsTemplate getDeployedJmsTemplate) {
        this.getDeployedJmsTemplate = getDeployedJmsTemplate;
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
