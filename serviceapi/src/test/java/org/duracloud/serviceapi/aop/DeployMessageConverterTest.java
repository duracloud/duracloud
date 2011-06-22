/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceapi.aop;

import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.support.converter.MessageConversionException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

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
        int serviceId = 7;
        int deploymentId = 9;
        String serviceHost = "serviceHost";

        MapMessage msg = EasyMock.createMock("MapMessage", MapMessage.class);

        EasyMock.expect(msg.getIntProperty(DeployMessageConverter.SERVICE_ID))
            .andReturn(serviceId);

        EasyMock.expect(msg.getString(DeployMessageConverter.SERVICE_HOST))
            .andReturn(serviceHost);

        EasyMock.expect(msg.getInt(DeployMessageConverter.DEPLOYMENT_ID))
            .andReturn(deploymentId);

        EasyMock.replay(msg);
        Object obj = deployMessageConverter.fromMessage(msg);
        EasyMock.verify(msg);

        assertNotNull(obj);
        assertTrue(obj instanceof DeployMessage);

        DeployMessage deployMessage = (DeployMessage) obj;
        Assert.assertEquals(serviceId, deployMessage.getServiceId());
        Assert.assertEquals(serviceHost, deployMessage.getServiceHost());
    }

    @Test
    public void testToMessage() throws JMSException {
        int serviceId = 5;
        int deploymentId = 7;
        String serviceHost = "serviceHost";

        MapMessage mapMsg = EasyMock.createMock("MapMessage",
                                                MapMessage.class);
        Session session = EasyMock.createMock("Session",
                                              Session.class);

        session.createMapMessage();
        EasyMock.expectLastCall().andReturn(mapMsg);

        mapMsg.setIntProperty(DeployMessageConverter.SERVICE_ID, serviceId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(DeployMessageConverter.SERVICE_HOST, serviceHost);
        EasyMock.expectLastCall().once();

        mapMsg.setInt(DeployMessageConverter.DEPLOYMENT_ID, deploymentId);
        EasyMock.expectLastCall().once();

        DeployMessage deployMessage = new DeployMessage();
        deployMessage.setServiceId(serviceId);
        deployMessage.setServiceHost(serviceHost);
        deployMessage.setDeploymentId(deploymentId);

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
