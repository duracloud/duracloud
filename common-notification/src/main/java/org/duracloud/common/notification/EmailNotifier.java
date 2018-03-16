/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.duracloud.notification.AmazonNotificationFactory;
import org.duracloud.notification.Emailer;
import org.duracloud.notification.NotificationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notifier which pushes notifications out via email.
 *
 * @author: Bill Branan
 * Date: 12/2/11
 */
public class EmailNotifier implements Notifier {

    private final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    private Emailer emailer;
    private List<String> adminEmails;

    @Override
    public void initialize(NotificationConfig notificationConfig) {
        NotificationFactory factory = new AmazonNotificationFactory();
        factory.initialize(notificationConfig.getUsername(),
                           notificationConfig.getPassword());
        emailer = factory.getEmailer(notificationConfig.getOriginator());
        adminEmails = notificationConfig.getAdmins();
        if (null == adminEmails) {
            adminEmails = new ArrayList<String>();
        }
    }

    /**
     * Intended for testing only
     */
    protected void setEmailer(Emailer emailer) {
        this.emailer = emailer;
    }

    /**
     * Intended for testing only
     */
    protected void setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.EMAIL;
    }

    @Override
    public void notify(String subject, String message, String... emailAddrs) {
        checkInitialized();

        log.debug("Sending email to " + Arrays.toString(emailAddrs) +
                  " with subject: [" + subject +
                  "] and message: [" + message + "]");

        emailer.send(subject, message, emailAddrs);
    }

    @Override
    public void notifyAdmins(String subject, String message) {
        checkInitialized();

        log.debug("Sending email to " + adminEmails.size() +
                  " administrators with subject: [" + subject +
                  "] and message: [" + message + "]");

        emailer.send(subject,
                     message,
                     adminEmails.toArray(new String[adminEmails.size()]));
    }

    private void checkInitialized() {
        if (null == emailer) {
            throw new RuntimeException("The Email Notifier must be " +
                                       "initialized prior to use!");
        }
    }
}
