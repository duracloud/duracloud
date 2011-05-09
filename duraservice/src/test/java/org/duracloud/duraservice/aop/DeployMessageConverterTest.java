/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.aop;

import org.easymock.classextension.EasyMock;
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

public class DeployMessageConverterTest {
    private DeployMessageConverter deployMessageConverter;

    @Before
    public void setUp() throws Exception {
        deployMessageConverter = new DeployMessageConverter();
    }

    @Test
    public void testFromConversionException() throws JMSException {
        try{
            Message msg = EasyMock.createMock("Message",
                                              Message.class);;

            deployMessageConverter.fromMessage(msg);
            assertTrue(false);

        } catch(MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testToConversionException() throws JMSException {
        try{
            deployMessageConverter.toMessage((Object) "", null);
            assertTrue(false);

        } catch(MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testFromMessage() throws JMSException {
        String serviceId = "serviceId";
        String serviceHost = "serviceHost";

        MapMessage msg = EasyMock.createMock("MapMessage",
                                             MapMessage.class);

        msg.getStringProperty(DeployMessageConverter.SERVICE_ID);
        EasyMock.expectLastCall().andReturn(serviceId);

        msg.getString(DeployMessageConverter.SERVICE_HOST);
        EasyMock.expectLastCall().andReturn(serviceHost);

        EasyMock.replay(msg);
        Object obj = deployMessageConverter.fromMessage(msg);
        EasyMock.verify(msg);

        assertNotNull(obj);
        assertTrue(obj instanceof DeployMessage);

        DeployMessage deployMessage = (DeployMessage) obj;
        assertEquals(serviceId, deployMessage.getServiceId());
        assertEquals(serviceHost, deployMessage.getServiceHost());
    }

    @Test
    public void testToMessage() throws JMSException {
        String serviceId = "serviceId";
        String serviceHost = "serviceHost";

        MapMessage mapMsg = EasyMock.createMock("MapMessage",
                                                MapMessage.class);
        Session session = EasyMock.createMock("Session",
                                              Session.class);

        session.createMapMessage();
        EasyMock.expectLastCall().andReturn(mapMsg);

        mapMsg.setStringProperty(DeployMessageConverter.SERVICE_ID, serviceId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(DeployMessageConverter.SERVICE_HOST, serviceHost);
        EasyMock.expectLastCall().once();

        DeployMessage deployMessage = new DeployMessage();
        deployMessage.setServiceId(serviceId);
        deployMessage.setServiceHost(serviceHost);

        EasyMock.replay(mapMsg);
        EasyMock.replay(session);
        Message msg = deployMessageConverter.toMessage((Object)deployMessage,
                                                      session);
        EasyMock.verify(mapMsg);
        EasyMock.verify(session);

        assertNotNull(msg);
        assertTrue(msg instanceof MapMessage);
    }
}
