/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.aop;

import org.duracloud.serviceapi.aop.ServiceMessage;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;

/**
 * @author Andrew Woods
 *         Date: 6/29/11
 */
public abstract class AdviceTestBase {

    private ServiceAdvice advice;

    /**
     * This method is overwritten by children tests
     *
     * @return ServiceAdvice
     */
    protected abstract ServiceAdvice getAdvice();

    @Before
    public void setUp() throws Exception {
        advice = getAdvice();
    }

    @Test
    public void testNullParams() throws Throwable {
        try {
            advice.afterReturning(null, null, null, null);
            Assert.fail("exception expected");

        } catch (NullPointerException npe) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testEmptyParam() throws Throwable {
        try {
            advice.afterReturning(null, null, new Object[]{}, null);
            Assert.fail("exception expected");

        } catch (ArrayIndexOutOfBoundsException aobe) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testNullDestination() throws Throwable {
        doTestMessage(null);
    }

    @Test
    public void testMessage() throws Throwable {
        Destination destination = EasyMock.createMock("Destination",
                                                      Destination.class);
        doTestMessage(destination);
    }

    private void doTestMessage(Destination destination) throws Throwable {
        int id = 1;

        ServiceMessage msg = new ServiceMessage();
        msg.setServiceId(id);
        msg.setDeploymentId(id);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);

        if (null == destination) {
            jmsTemplate.convertAndSend((Destination) EasyMock.isNull(),
                                       ServiceMessageEquals.eqServiceMessage(msg));
        } else {
            jmsTemplate.convertAndSend((Destination) EasyMock.notNull(),
                                       ServiceMessageEquals.eqServiceMessage(msg));
        }
        EasyMock.expectLastCall().once();

        EasyMock.replay(jmsTemplate);

        advice.setJmsTemplate(jmsTemplate);
        advice.setDestination(destination);
        advice.afterReturning(null,
                              null,
                              new Object[]{new Integer(id), new Integer(id)},
                              null);

        EasyMock.verify(jmsTemplate);
    }

}
