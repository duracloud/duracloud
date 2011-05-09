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
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;

import static junit.framework.Assert.assertTrue;

public class DeployAdviceTest {

    private DeployAdvice deployAdvice;

    @Before
    public void setUp() throws Exception {
        deployAdvice = new DeployAdvice();
    }

    @Test
    public void testNullParams() throws Throwable {
        try{
            deployAdvice.afterReturning(null, null, null, null);
            assertTrue(false);

        } catch(NullPointerException npe) {
            assertTrue(true);
        }
    }

    @Test
    public void testEmptyParam() throws Throwable {
        try{
            deployAdvice.afterReturning(null, null, new Object[]{}, null);
            assertTrue(false);

        } catch(ArrayIndexOutOfBoundsException aobe) {
            assertTrue(true);
        }
    }

    @Test
    public void testNullDestination() throws Throwable {
        DeployMessage msg = new DeployMessage();
        msg.setServiceId(null);
        msg.setServiceHost(null);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);
        Destination destination = null;

        jmsTemplate.convertAndSend((Destination)EasyMock.isNull(),
                                   DeployMessageEquals.eqDeployMessage(msg));
        EasyMock.expectLastCall().once();

        EasyMock.replay(jmsTemplate);

        deployAdvice.setDeployJmsTemplate(jmsTemplate);
        deployAdvice.setDestination(destination);
        deployAdvice.afterReturning(null, null,
                                         new Object[]{null,null,null}, null);

        EasyMock.verify(jmsTemplate);
    }

    @Test
    public void testMessage() throws Throwable {
        String id = "1";

        DeployMessage msg = new DeployMessage();
        msg.setServiceId(id);
        msg.setServiceHost(id);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);
        Destination destination = EasyMock.createMock("Destination",
                                                      Destination.class);

        jmsTemplate.convertAndSend((Destination)EasyMock.notNull(),
                                   DeployMessageEquals.eqDeployMessage(msg));
        EasyMock.expectLastCall().once();

        EasyMock.replay(jmsTemplate);

        deployAdvice.setDeployJmsTemplate(jmsTemplate);
        deployAdvice.setDestination(destination);
        deployAdvice.afterReturning(null, null,
                                         new Object[]{new Integer(id),id,null}, null);

        EasyMock.verify(jmsTemplate);
    }
}
