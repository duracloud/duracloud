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
import org.easymock.Capture;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

public class ContentCopyAdviceTest {

    private ContentCopyAdvice contentCopyAdvice;

    @Before
    public void setUp() throws Exception {
        contentCopyAdvice = new ContentCopyAdvice();
    }

    @Test
    public void testNullParams() throws Throwable {
        try{
            contentCopyAdvice.afterReturning(null, null, null, null);
            fail("Exception expected");
        } catch(NullPointerException npe) {
            assertNotNull(npe);
        }
    }

    @Test
    public void testEmptyParam() throws Throwable {
        try{
            contentCopyAdvice.afterReturning(null, null, new Object[]{}, null);
            fail("Exception expected");
        } catch(ArrayIndexOutOfBoundsException aobe) {
            assertNotNull(aobe);
        }
    }

    @Test
    public void testNullDestination() throws Throwable {
        ContentCopyMessage msg = new ContentCopyMessage();
        msg.setStoreId(null);
        msg.setSourceSpaceId(null);
        msg.setSourceContentId(null);
        msg.setDestSpaceId(null);
        msg.setDestContentId(null);
        msg.setUsername(null);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);
        Destination destination = null;

        Capture<ContentCopyMessage> messageCapture =
            new Capture<ContentCopyMessage>();
        jmsTemplate.convertAndSend((Destination)EasyMock.isNull(),
                                   EasyMock.capture(messageCapture));
        EasyMock.expectLastCall().once();

        SecurityContextUtil securityContextUtil = EasyMock.createMock(
            "SecurityContextUtil",
            SecurityContextUtil.class);
        Credential user = new Credential("username","password");
        EasyMock.expect(securityContextUtil.getCurrentUser()).andReturn(user);

        EasyMock.replay(jmsTemplate, securityContextUtil);

        contentCopyAdvice.setJmsTemplate(jmsTemplate);
        contentCopyAdvice.setDestination(destination);
        contentCopyAdvice.setSecurityContextUtil(securityContextUtil);
        Object[] emptyObj = new Object[]{null, null, null, null, null, null};
        contentCopyAdvice.afterReturning(null, null, emptyObj, null);
        ContentCopyMessage capturedMessage = messageCapture.getValue();
        assertEquals(msg, capturedMessage);

        EasyMock.verify(jmsTemplate, securityContextUtil);
    }

    @Test
    public void testMessage() throws Throwable {
        String id = "1";

        ContentCopyMessage msg = new ContentCopyMessage();
        msg.setStoreId(id);
        msg.setSourceSpaceId(id);
        msg.setSourceContentId(id);
        msg.setDestSpaceId(id);
        msg.setDestContentId(id);
        msg.setUsername(id);

        JmsTemplate jmsTemplate = EasyMock.createMock("JmsTemplate",
                                                      JmsTemplate.class);
        Destination destination = EasyMock.createMock("Destination",
                                                      Destination.class);

        Capture<ContentCopyMessage> messageCapture =
            new Capture<ContentCopyMessage>();
        jmsTemplate.convertAndSend((Destination)EasyMock.notNull(),
                                   EasyMock.capture(messageCapture));
        EasyMock.expectLastCall().once();

        SecurityContextUtil securityContextUtil = EasyMock.createMock(
            "SecurityContextUtil",
            SecurityContextUtil.class);
        Credential user = new Credential("username", "password");
        EasyMock.expect(securityContextUtil.getCurrentUser()).andReturn(user);

        EasyMock.replay(jmsTemplate, securityContextUtil);

        contentCopyAdvice.setJmsTemplate(jmsTemplate);
        contentCopyAdvice.setDestination(destination);
        contentCopyAdvice.setSecurityContextUtil(securityContextUtil);
        Object[] idObj = new Object[]{null, id, id, id, id, id};
        contentCopyAdvice.afterReturning(null, null, idObj, null);
        ContentCopyMessage capturedMessage = messageCapture.getValue();
        assertEquals(msg, capturedMessage);

        EasyMock.verify(jmsTemplate, securityContextUtil);
    }
}
