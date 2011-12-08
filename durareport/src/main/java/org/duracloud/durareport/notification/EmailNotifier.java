/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.notification;

import org.duracloud.appconfig.domain.NotificationConfig;
import org.duracloud.notification.AmazonNotificationFactory;
import org.duracloud.notification.Emailer;
import org.duracloud.notification.NotificationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Notifier which pushes notifications out via email.
 *
 * @author: Bill Branan
 * Date: 12/2/11
 */
public class EmailNotifier implements Notifier {

    private final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    private Emailer emailer;

    @Override
    public void initialize(NotificationConfig notificationConfig) {
        NotificationFactory factory = new AmazonNotificationFactory();
        factory.initialize(notificationConfig.getUsername(),
                           notificationConfig.getPassword());
        emailer = factory.getEmailer(notificationConfig.getOriginator());
    }

    /** Intended for testing only */
    protected void setEmailer(Emailer emailer) {
        this.emailer = emailer;
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
                  "] and message: [" +  message + "]");

        emailer.send(subject, message, emailAddrs);
    }

    private void checkInitialized() {
        if(null == emailer) {
            throw new RuntimeException("The Email Notifier must be " +
                                       "initialized prior to use!");
        }
    }
}
