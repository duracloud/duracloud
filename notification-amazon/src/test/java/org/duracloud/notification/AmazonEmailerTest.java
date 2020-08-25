/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.notification;

import java.util.List;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 * Date: 3/11/11
 */
public class AmazonEmailerTest {

    private AmazonEmailer emailer;

    private AmazonSimpleEmailService emailService;

    private String fromAddress = "a@g.com";

    @Before
    public void setUp() throws Exception {
        emailService = EasyMock.createMock("AmazonSimpleEmailService",
                                           AmazonSimpleEmailService.class);
        emailer = new AmazonEmailer(emailService, fromAddress);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(emailService);
    }

    @Test
    public void testSend() throws Exception {
        Capture capturedRequest = createSendMockExpectation();

        String subject = "subject";
        String body = "body text";
        String[] recipients = {"x@y.com"};

        // The call under test.
        emailer.send(subject, body, recipients);

        boolean asHtml = false;
        verifyRequest(capturedRequest, subject, body, recipients, asHtml);
    }

    @Test
    public void testSendAsHtml() throws Exception {
        Capture capturedRequest = createSendMockExpectation();

        String subject = "subject";
        String body = "body text";
        String[] recipients = {"x@y.com", "a@b.org"};

        // The call under test.
        emailer.sendAsHtml(subject, body, recipients);

        boolean asHtml = true;
        verifyRequest(capturedRequest, subject, body, recipients, asHtml);
    }

    private void verifyRequest(Capture capturedRequest,
                               String subject,
                               String body,
                               String[] recipients,
                               boolean asHtml) {
        SendEmailRequest req = (SendEmailRequest) capturedRequest.getValue();
        Assert.assertNotNull(req);

        Message msg = req.getMessage();
        Assert.assertNotNull(msg);

        Assert.assertEquals(subject, msg.getSubject().getData());
        if (asHtml) {
            Assert.assertEquals(body, msg.getBody().getHtml().getData());
        } else {
            Assert.assertEquals(body, msg.getBody().getText().getData());
        }

        List<String> toAddresses = req.getDestination().getToAddresses();
        Assert.assertNotNull(toAddresses);
        Assert.assertEquals(recipients.length, toAddresses.size());
        for (int i = 0; i < recipients.length; ++i) {
            Assert.assertEquals(recipients[i], toAddresses.get(i));
        }
    }

    private Capture createSendMockExpectation() {
        Capture<SendEmailRequest> capturedRequest = Capture.newInstance(CaptureType.FIRST);
        EasyMock.expect(emailService.sendEmail(EasyMock.capture(capturedRequest)))
                .andReturn(null);

        EasyMock.replay(emailService);
        return capturedRequest;
    }
}
