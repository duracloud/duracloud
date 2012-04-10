/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.duracloud.common.model.Credential;
import org.duracloud.security.context.SecurityContextUtil;
import org.duracloud.storage.aop.SpaceMessage;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;

import static junit.framework.Assert.assertTrue;

/**
 * @author Andrew Woods
 *         Date: 4/09/12
 */
public abstract class SpaceAdviceTestBase {

    private BaseContentStoreAdvice spaceAdvice;

    protected abstract BaseContentStoreAdvice getSpaceAdvice();

    @Before
    public void setUp() throws Exception {
        spaceAdvice = getSpaceAdvice();
    }

    @Test
    public void testNullParams() throws Throwable {
        try {
            spaceAdvice.afterReturning(null, null, null, null);
            assertTrue(false);

        } catch (NullPointerException npe) {
            assertTrue(true);
        }
    }

    @Test
    public void testEmptyParam() throws Throwable {
        try {
            spaceAdvice.afterReturning(null, null, new Object[]{}, null);
            assertTrue(false);

        } catch (ArrayIndexOutOfBoundsException aobe) {
            assertTrue(true);
        }
    }

    @Test
    public void testNullDestination() throws Throwable {
        SpaceMessage msg = new SpaceMessage();
        msg.setStoreId(null);
        msg.setSpaceId(null);
        msg.setUsername(null);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);
        Destination destination = null;

        jmsTemplate.convertAndSend((Destination) EasyMock.isNull(),
                                   SpaceMessageEquals.eqSpaceMessage(msg));
        EasyMock.expectLastCall().once();

        SecurityContextUtil securityContextUtil = EasyMock.createMock(
            "SecurityContextUtil",
            SecurityContextUtil.class);
        Credential user = new Credential("username", "password");
        EasyMock.expect(securityContextUtil.getCurrentUser()).andReturn(user);

        EasyMock.replay(jmsTemplate, securityContextUtil);

        spaceAdvice.setJmsTemplate(jmsTemplate);
        spaceAdvice.setDestination(destination);
        spaceAdvice.setSecurityContextUtil(securityContextUtil);
        spaceAdvice.afterReturning(null,
                                   null,
                                   new Object[]{null, null, null},
                                   null);

        EasyMock.verify(jmsTemplate, securityContextUtil);
    }

    @Test
    public void testMessage() throws Throwable {
        String id = "1";

        SpaceMessage msg = new SpaceMessage();
        msg.setStoreId(id);
        msg.setSpaceId(id);
        msg.setUsername(id);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);
        Destination destination = EasyMock.createMock("Destination",
                                                      Destination.class);

        jmsTemplate.convertAndSend((Destination) EasyMock.notNull(),
                                   SpaceMessageEquals.eqSpaceMessage(msg));
        EasyMock.expectLastCall().once();

        SecurityContextUtil securityContextUtil = EasyMock.createMock(
            "SecurityContextUtil",
            SecurityContextUtil.class);
        Credential user = new Credential("username", "password");
        EasyMock.expect(securityContextUtil.getCurrentUser()).andReturn(user);

        EasyMock.replay(jmsTemplate, securityContextUtil);

        spaceAdvice.setJmsTemplate(jmsTemplate);
        spaceAdvice.setDestination(destination);
        spaceAdvice.setSecurityContextUtil(securityContextUtil);
        spaceAdvice.afterReturning(null,
                                   null,
                                   new Object[]{null, id, id},
                                   null);

        EasyMock.verify(jmsTemplate, securityContextUtil);
    }
}
