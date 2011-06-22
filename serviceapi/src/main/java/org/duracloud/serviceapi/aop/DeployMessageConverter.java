/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceapi.aop;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * @author Andrew Woods
 *         Date: June 20, 2011
 */
public class DeployMessageConverter
        implements MessageConverter {

    protected final Logger log = LoggerFactory.getLogger(DeployMessageConverter.class);

    public static final String SERVICE_ID = "serviceId";
    public static final String DEPLOYMENT_ID = "deploymentId";
    public static final String SERVICE_HOST = "serviceHost";

    public Object fromMessage(Message msg) throws JMSException,
            MessageConversionException {
        if (!(msg instanceof MapMessage)) {
            String err = "Arg obj is not an instance of 'MapMessage': ";
            log.error(err + msg);
            throw new MessageConversionException(err);
        }

        MapMessage mapMsg = (MapMessage)msg;
        DeployMessage deployMsg = new DeployMessage();
        deployMsg.setServiceId(mapMsg.getIntProperty(SERVICE_ID));
        deployMsg.setServiceHost(mapMsg.getString(SERVICE_HOST));
        deployMsg.setDeploymentId(mapMsg.getInt(DEPLOYMENT_ID));
        return deployMsg;
    }

    public Message toMessage(Object obj, Session session) throws JMSException,
            MessageConversionException {
        if (!(obj instanceof DeployMessage)) {
            String err = "Arg obj is not an instance of 'DeployMessage': ";
            log.error(err + obj);
            throw new MessageConversionException(err);
        }
        DeployMessage deployMsg = (DeployMessage) obj;

        MapMessage msg = session.createMapMessage();
        msg.setIntProperty(SERVICE_ID, deployMsg.getServiceId());
        msg.setString(SERVICE_HOST, deployMsg.getServiceHost());
        msg.setInt(DEPLOYMENT_ID, deployMsg.getDeploymentId());
        return msg;
    }

}
