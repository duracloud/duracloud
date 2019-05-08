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
public class SpringNotificationFactory implements NotificationFactory {

    private static final Logger log = LoggerFactory.getLogger(
        SpringNotificationFactory.class);

    private JavaMailSenderImpl emailService;
    private Map<String, Emailer> emailerMap = new HashMap<String, Emailer>();
    private String host;
    private Integer port;

    public SpringNotificationFactory(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void initialize(String username, String password) {
        emailService = new JavaMailSenderImpl();
        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", "true");
        mailProperties.put("mail.smtp.starttls.enable", "true");
        mailProperties.put("mail.smtp.starttls.required", "true");
        emailService.setJavaMailProperties(mailProperties);
        emailService.setProtocol("smtp");
        emailService.setHost(host.trim());
        emailService.setPort(port);
        emailService.setUsername(username.trim());
        emailService.setPassword(password.trim());

        try {
            //Test the connection
            emailService.testConnection();
            log.debug(
                "Emial connection test passed: email service with Sprint email client connected to {}, Port: {}, " +
                "User: {}.",
                host, port, username);

        } catch (MessagingException ex) {
            log.error("Email connection test failed when connecting to {}, Port: {}, User: {}, because {}", host, port,
                      username, ex.getMessage());
        }

    }

    @Override
    public Emailer getEmailer(String fromAddress) {
        if (null == fromAddress ||
            !EmailValidator.getInstance().isValid(fromAddress)) {
            String msg = "fromAddress not valid notification: " + fromAddress;
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (null == emailService) {
            String msg = "Emailer service !initialized.";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }

        Emailer emailer = emailerMap.get(fromAddress);
        if (null == emailer) {
            emailer = new SpringEmailer(emailService, fromAddress);
            emailerMap.put(fromAddress, emailer);
        }

        return emailer;
    }

}
