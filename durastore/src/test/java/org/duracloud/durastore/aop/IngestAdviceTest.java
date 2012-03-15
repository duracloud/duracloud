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
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;

import static junit.framework.Assert.assertTrue;

public class IngestAdviceTest {

    private IngestAdvice ingestAdvice;

    @Before
    public void setUp() throws Exception {
        ingestAdvice = new IngestAdvice();
    }

    @Test
    public void testNullParams() throws Throwable {
        try{
            ingestAdvice.afterReturning(null, null, null, null);
            assertTrue(false);

        } catch(NullPointerException npe) {
            assertTrue(true);
        }
    }

    @Test
    public void testEmptyParam() throws Throwable {
        try{
            ingestAdvice.afterReturning(null, null, new Object[]{}, null);
            assertTrue(false);

        } catch(ArrayIndexOutOfBoundsException aobe) {
            assertTrue(true);
        }
    }

    @Test
    public void testNullDestination() throws Throwable {
        IngestMessage msg = new IngestMessage();
        msg.setStoreId(null);
        msg.setSpaceId(null);
        msg.setContentId(null);
        msg.setContentMimeType(null);
        msg.setUsername(null);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);
        Destination destination = null;

        jmsTemplate.convertAndSend((Destination)EasyMock.isNull(),
                                   IngestMessageEquals.eqIngestMessage(msg));
        EasyMock.expectLastCall().once();

        SecurityContextUtil securityContextUtil = EasyMock.createMock(
            "SecurityContextUtil",
            SecurityContextUtil.class);
        Credential user = new Credential("username","password");
        EasyMock.expect(securityContextUtil.getCurrentUser()).andReturn(user);

        EasyMock.replay(jmsTemplate, securityContextUtil);

        ingestAdvice.setJmsTemplate(jmsTemplate);
        ingestAdvice.setDestination(destination);
        ingestAdvice.setSecurityContextUtil(securityContextUtil);
        ingestAdvice.afterReturning(null, null,
                                         new Object[]{null,null,null,null,null}, null);

        EasyMock.verify(jmsTemplate, securityContextUtil);
    }

    @Test
    public void testMessage() throws Throwable {
        String id = "1";

        IngestMessage msg = new IngestMessage();
        msg.setStoreId(id);
        msg.setSpaceId(id);
        msg.setContentId(id);
        msg.setContentMimeType(id);
        msg.setUsername(id);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);
        Destination destination = EasyMock.createMock("Destination",
                                                      Destination.class);

        jmsTemplate.convertAndSend((Destination)EasyMock.notNull(),
                                   IngestMessageEquals.eqIngestMessage(msg));
        EasyMock.expectLastCall().once();

        SecurityContextUtil securityContextUtil = EasyMock.createMock(
            "SecurityContextUtil",
            SecurityContextUtil.class);
        Credential user = new Credential("username", "password");
        EasyMock.expect(securityContextUtil.getCurrentUser()).andReturn(user);

        EasyMock.replay(jmsTemplate, securityContextUtil);

        ingestAdvice.setJmsTemplate(jmsTemplate);
        ingestAdvice.setDestination(destination);
        ingestAdvice.setSecurityContextUtil(securityContextUtil);
        ingestAdvice.afterReturning(null, null,
                                         new Object[]{null,id,id,id,id}, null);

        EasyMock.verify(jmsTemplate, securityContextUtil);
    }
}
