/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.notification;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.MessagingException;

import org.apache.commons.validator.routines.EmailValidator;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * @author Shibo Liu
 * Date: 07/03/19
 */
public class SMTPNotificationFactory implements NotificationFactory {

    private static final Logger log = LoggerFactory.getLogger(SMTPNotificationFactory.class);

    private JavaMailSenderImpl emailService;
    private Map<String, Emailer> emailerMap = new HashMap<String, Emailer>();
    private String host;
    private Integer port;

    public SMTPNotificationFactory(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void initialize(String username, String password) {
        String logUserMsg = "";
        emailService = new JavaMailSenderImpl();
        Properties mailProperties = new Properties();
        if (username != null && !username.isEmpty()) {
            mailProperties.put("mail.smtp.auth", "true");
            emailService.setUsername(username.trim());
            emailService.setPassword(password.trim());
            logUserMsg = ", User: " + username;
        }
        mailProperties.put("mail.smtp.starttls.enable", "true");
        mailProperties.put("mail.smtp.starttls.required", "true");
        emailService.setJavaMailProperties(mailProperties);
        emailService.setProtocol("smtp");
        emailService.setHost(host.trim());
        emailService.setPort(port);

        try {
            //Test the connection
            emailService.testConnection();
            log.debug(
                "Email connection test passed: SMTP client connected to {}, Port: {}" +
                logUserMsg, host, port);

        } catch (MessagingException ex) {
            log.error(
                "Email connection test failed when connecting to {}, Port: {}" +
                logUserMsg + ", because {}", host, port, ex.getMessage());
        }
    }

    @Override
    public Emailer getEmailer(String fromAddress) {
        if (null == fromAddress ||
            !EmailValidator.getInstance().isValid(fromAddress)) {
            String msg = "fromAddress " + fromAddress + " is not valid. Email notification service NOT initialized.";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (null == emailService) {
            String msg = "emailService is null. Email notification service NOT initialized.";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }

        Emailer emailer = emailerMap.get(fromAddress);
        if (null == emailer) {
            emailer = new SMTPEmailer(emailService, fromAddress);
            emailerMap.put(fromAddress, emailer);
        }

        return emailer;
    }

}
