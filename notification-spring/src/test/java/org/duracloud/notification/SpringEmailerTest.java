/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.notification;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 *
 * @author Andy Foster
 * Date: 2020.02.12
 */
public class SpringEmailerTest extends EasyMockSupport {

    private String fromAddressString = "from@address.com";
    private Address fromAddress;
    private String toAddressString = "x@y.com";
    private Address toAddress;
    private MimeMessage mimeMessage;
    private JavaMailSenderImpl mailSender;

    @Before
    public void setUp() throws Exception {
        mailSender = mock(JavaMailSenderImpl.class);
        mimeMessage = mock(MimeMessage.class);
    }

    @After
    public void tearDown() throws Exception {
        verifyAll();
    }

    private void setupMailer() throws MessagingException {
        fromAddress = new InternetAddress(fromAddressString);
        toAddress = new InternetAddress(toAddressString);
        mimeMessage.setFrom(fromAddress);
        mimeMessage.setSubject("subject");
        Address[] toAddresses = {toAddress};
        mimeMessage.setRecipients(EasyMock.eq(MimeMessage.RecipientType.TO), EasyMock.aryEq(toAddresses));

        EasyMock.expect(mailSender.createMimeMessage()).andReturn(mimeMessage);

        mailSender.send(mimeMessage);
    }

    @Test
    public void testSend() throws MessagingException {
        setupMailer();
        mimeMessage.setText("body text");
        replayAll();

        SpringEmailer emailer = new SpringEmailer(mailSender, fromAddressString);

        String subject = "subject";
        String body = "body text";
        String[] recipients = {toAddressString};

        // The call under test.
        emailer.send(subject, body, recipients);
    }

    @Test
    public void testSendAsHtml() throws MessagingException {
        setupMailer();
        mimeMessage.setContent("body text", "text/html");
        replayAll();

        SpringEmailer emailer = new SpringEmailer(mailSender, fromAddressString);

        String subject = "subject";
        String body = "body text";
        String[] recipients = {toAddressString};

        // The call under test.
        emailer.sendAsHtml(subject, body, recipients);
    }
}
