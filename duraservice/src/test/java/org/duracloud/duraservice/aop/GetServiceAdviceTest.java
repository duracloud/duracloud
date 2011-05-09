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

public class GetServiceAdviceTest {

    private GetServiceAdvice getServiceAdvice;

    @Before
    public void setUp() throws Exception {
        getServiceAdvice = new GetServiceAdvice();
    }

    @Test
    public void testNullParams() throws Throwable {
        try{
            getServiceAdvice.afterReturning(null, null, null, null);
            assertTrue(false);

        } catch(NullPointerException npe) {
            assertTrue(true);
        }
    }

    @Test
    public void testEmptyParam() throws Throwable {
        try{
            getServiceAdvice.afterReturning(null, null, new Object[]{}, null);
            assertTrue(false);

        } catch(ArrayIndexOutOfBoundsException aobe) {
            assertTrue(true);
        }
    }

    @Test
    public void testNullDestination() throws Throwable {
        ServiceMessage msg = new ServiceMessage();
        msg.setServiceId(null);
        msg.setDeploymentId(null);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);
        Destination destination = null;

        jmsTemplate.convertAndSend((Destination)EasyMock.isNull(),
                                   ServiceMessageEquals.eqServiceMessage(msg));
        EasyMock.expectLastCall().once();

        EasyMock.replay(jmsTemplate);

        getServiceAdvice.setJmsTemplate(jmsTemplate);
        getServiceAdvice.setDestination(destination);
        getServiceAdvice.afterReturning(null, null,
                                         new Object[]{null}, null);

        EasyMock.verify(jmsTemplate);
    }

    @Test
    public void testMessage() throws Throwable {
        String id = "1";

        ServiceMessage msg = new ServiceMessage();
        msg.setServiceId(id);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);
        Destination destination = EasyMock.createMock("Destination",
                                                      Destination.class);

        jmsTemplate.convertAndSend((Destination)EasyMock.notNull(),
                                   ServiceMessageEquals.eqServiceMessage(msg));
        EasyMock.expectLastCall().once();

        EasyMock.replay(jmsTemplate);

        getServiceAdvice.setJmsTemplate(jmsTemplate);
        getServiceAdvice.setDestination(destination);
        getServiceAdvice.afterReturning(null, null,
                                         new Object[]{new Integer(id)}, null);

        EasyMock.verify(jmsTemplate);
    }
}
