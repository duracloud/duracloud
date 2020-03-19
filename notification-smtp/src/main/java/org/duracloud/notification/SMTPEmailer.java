/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.notification;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * @author Shibo Liu
 * Date: 07/03/19
 */
public class SMTPEmailer implements Emailer {

    private static final Logger log = LoggerFactory.getLogger(SMTPEmailer.class);
    private JavaMailSenderImpl emailService;
    private String fromAddress;

    public SMTPEmailer(JavaMailSenderImpl emailService,
                         String fromAddress) {
        this.emailService = emailService;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(String subject, String body, String... recipients) {
        sendEmail(subject, body, false, recipients);
    }

    @Override
    public void sendAsHtml(String subject, String body, String... recipients) {
        sendEmail(subject, body, true, recipients);
    }

    private void sendEmail(String subject, String messageStr, boolean isHtml, String... recipients) {
        MimeMessage message = emailService.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message);

        try {
            messageHelper.setFrom(fromAddress);
            messageHelper.setSubject(subject);
            messageHelper.setTo(recipients);
            messageHelper.setText(messageStr, isHtml);
        } catch (MessagingException ex) {
            log.error("Failed to prepare email message {}", ex.getMessage());
        }

        try {
            emailService.send(message);
        } catch (MailException ex) {
            log.error("Failed to send email because: {}", ex.getMessage());
        }
    }
}
