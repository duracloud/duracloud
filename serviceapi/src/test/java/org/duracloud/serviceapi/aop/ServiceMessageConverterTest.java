/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceapi.aop;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.support.converter.MessageConversionException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class ServiceMessageConverterTest {
    private ServiceMessageConverter serviceMessageConverter;

    @Before
    public void setUp() throws Exception {
        serviceMessageConverter = new ServiceMessageConverter();
    }

    @Test
    public void testFromConversionException() throws JMSException {
        try{
            Message msg = EasyMock.createMock("Message",
                                              Message.class);

            serviceMessageConverter.fromMessage(msg);
            assertTrue(false);

        } catch(MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testToConversionException() throws JMSException {
        try{
            serviceMessageConverter.toMessage((Object) "", null);
            assertTrue(false);

        } catch(MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testFromMessage() throws JMSException {
        int serviceId = 4;
        int deploymentId = 5;

        MapMessage msg = EasyMock.createMock("MapMessage",
                                             MapMessage.class);

        msg.getIntProperty(ServiceMessageConverter.SERVICE_ID);
        EasyMock.expectLastCall().andReturn(serviceId);

        msg.getInt(ServiceMessageConverter.DEPLOYMENT_ID);
        EasyMock.expectLastCall().andReturn(deploymentId);

        EasyMock.replay(msg);
        Object obj = serviceMessageConverter.fromMessage(msg);
        EasyMock.verify(msg);

        assertNotNull(obj);
        assertTrue(obj instanceof ServiceMessage);

        ServiceMessage serviceMessage = (ServiceMessage) obj;
        assertEquals(serviceId, serviceMessage.getServiceId());
        assertEquals(deploymentId, serviceMessage.getDeploymentId());
    }

    @Test
    public void testToMessage() throws JMSException {
        int serviceId = 3;
        int deploymentId = 4;

        MapMessage mapMsg = EasyMock.createMock("MapMessage",
                                                MapMessage.class);
        Session session = EasyMock.createMock("Session",
                                              Session.class);

        session.createMapMessage();
        EasyMock.expectLastCall().andReturn(mapMsg);

        mapMsg.setIntProperty(ServiceMessageConverter.SERVICE_ID, serviceId);
        EasyMock.expectLastCall().once();

        mapMsg.setInt(ServiceMessageConverter.DEPLOYMENT_ID, deploymentId);
        EasyMock.expectLastCall().once();

        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.setServiceId(serviceId);
        serviceMessage.setDeploymentId(deploymentId);

        EasyMock.replay(mapMsg);
        EasyMock.replay(session);
        Message msg = serviceMessageConverter.toMessage((Object) serviceMessage,
                                                      session);
        EasyMock.verify(mapMsg);
        EasyMock.verify(session);

        assertNotNull(msg);
        assertTrue(msg instanceof MapMessage);
    }
}
