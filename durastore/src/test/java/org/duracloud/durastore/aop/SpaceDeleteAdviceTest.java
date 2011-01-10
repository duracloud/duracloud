/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;

import static junit.framework.Assert.assertTrue;

public class SpaceDeleteAdviceTest {

    private SpaceDeleteAdvice spaceDeleteAdvice;

    @Before
    public void setUp() throws Exception {
        spaceDeleteAdvice = new SpaceDeleteAdvice();
    }

    @Test
    public void testNullParams() throws Throwable {
        try{
            spaceDeleteAdvice.afterReturning(null, null, null, null);
            assertTrue(false);

        } catch(NullPointerException npe) {
            assertTrue(true);
        }
    }

    @Test
    public void testEmptyParam() throws Throwable {
        try{
            spaceDeleteAdvice.afterReturning(null, null, new Object[]{}, null);
            assertTrue(false);

        } catch(ArrayIndexOutOfBoundsException aobe) {
            assertTrue(true);
        }
    }

    @Test
    public void testNullDestination() throws Throwable {
        SpaceMessage msg = new SpaceMessage();
        msg.setStoreId(null);
        msg.setSpaceId(null);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);
        Destination destination = null;

        jmsTemplate.convertAndSend((Destination)EasyMock.isNull(),
                                   SpaceMessageEquals.eqSpaceMessage(msg));
        EasyMock.expectLastCall().once();

        EasyMock.replay(jmsTemplate);

        spaceDeleteAdvice.setSpaceJmsTemplate(jmsTemplate);
        spaceDeleteAdvice.setDestination(destination);
        spaceDeleteAdvice.afterReturning(null, null,
                                         new Object[]{null,null,null}, null);

        EasyMock.verify(jmsTemplate);
    }

    @Test
    public void testMessageCreation() throws Throwable {
        String id = "1";

        SpaceMessage msg = new SpaceMessage();
        msg.setStoreId(id);
        msg.setSpaceId(id);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);
        Destination destination = EasyMock.createMock("Destination",
                                                      Destination.class);

        jmsTemplate.convertAndSend((Destination)EasyMock.notNull(),
                                   SpaceMessageEquals.eqSpaceMessage(msg));
        EasyMock.expectLastCall().once();

        EasyMock.replay(jmsTemplate);

        spaceDeleteAdvice.setSpaceJmsTemplate(jmsTemplate);
        spaceDeleteAdvice.setDestination(destination);
        spaceDeleteAdvice.afterReturning(null, null,
                                         new Object[]{null,id,id}, null);

        EasyMock.verify(jmsTemplate);
    }
}
