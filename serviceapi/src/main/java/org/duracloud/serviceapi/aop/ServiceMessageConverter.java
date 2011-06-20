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
public class ServiceMessageConverter
        implements MessageConverter {

    protected final Logger log = LoggerFactory.getLogger(ServiceMessageConverter.class);

    public static final String SERVICE_ID = "serviceId";

    public static final String DEPLOYMENT_ID = "deploymentId";

    public Object fromMessage(Message msg) throws JMSException,
            MessageConversionException {
        if (!(msg instanceof MapMessage)) {
            String err = "Arg obj is not an instance of 'MapMessage': ";
            log.error(err + msg);
            throw new MessageConversionException(err);
        }

        MapMessage mapMsg = (MapMessage)msg;
        ServiceMessage serviceMsg = new ServiceMessage();
        serviceMsg.setServiceId(mapMsg.getStringProperty(SERVICE_ID));
        serviceMsg.setDeploymentId(mapMsg.getString(DEPLOYMENT_ID));
        return serviceMsg;
    }

    public Message toMessage(Object obj, Session session) throws JMSException,
            MessageConversionException {
        if (!(obj instanceof ServiceMessage)) {
            String err = "Arg obj is not an instance of 'ServiceMessage': ";
            log.error(err + obj);
            throw new MessageConversionException(err);
        }
        ServiceMessage serviceMsg = (ServiceMessage) obj;

        MapMessage msg = session.createMapMessage();
        msg.setStringProperty(SERVICE_ID, serviceMsg.getServiceId());
        msg.setString(DEPLOYMENT_ID, serviceMsg.getDeploymentId());
        return msg;
    }

}
