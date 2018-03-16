/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.notification;

import java.util.Arrays;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

/**
 * @author Andrew Woods
 * Date: 3/11/11
 */
public class AmazonEmailer implements Emailer {

    private AmazonSimpleEmailService emailService;
    private String fromAddress;

    public AmazonEmailer(AmazonSimpleEmailService emailService,
                         String fromAddress) {
        this.emailService = emailService;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(String subject, String body, String... recipients) {
        Body requestBody = new Body().withText(new Content(body));
        sendEmail(subject, requestBody, recipients);
    }

    @Override
    public void sendAsHtml(String subject, String body, String... recipients) {
        Body requestBody = new Body().withHtml(new Content(body));
        sendEmail(subject, requestBody, recipients);
    }

    private void sendEmail(String subject, Body body, String... recipients) {
        Destination destination = new Destination(Arrays.asList(recipients));
        Message msg = new Message().withBody(body).withSubject(new Content(subject));

        SendEmailRequest request = new SendEmailRequest().withSource(fromAddress)
                                                         .withDestination(destination)
                                                         .withMessage(msg);
        emailService.sendEmail(request);
    }
}
