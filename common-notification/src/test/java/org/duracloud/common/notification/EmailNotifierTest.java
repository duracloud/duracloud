/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.duracloud.notification.Emailer;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: 12/7/11
 */
public class EmailNotifierTest {

    private String subject = "subject";
    private String message = "message";
    private String[] emailAddresses = {"email1", "email2"};

    @Test
    public void testGetNotificationType() {
        EmailNotifier notifier = new EmailNotifier();
        assertEquals(NotificationType.EMAIL, notifier.getNotificationType());
    }

    @Test
    public void testInitialized() {
        // Verify initialization is required
        try {
            new EmailNotifier().notify(subject, message, emailAddresses);
            fail("Notify should fail when notifier is not initialized");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testNotify() {
        // Set up mock emailer
        Emailer emailer = EasyMock.createMock(Emailer.class);

        emailer.send(subject, message, emailAddresses);
        EasyMock.expectLastCall().times(1);

        EasyMock.replay(emailer);

        // Set up notifier
        EmailNotifier notifier = new EmailNotifier();
        notifier.setEmailer(emailer);

        // Send notification
        notifier.notify(subject, message, emailAddresses);

        // Verify mock
        EasyMock.verify(emailer);
    }

    @Test
    public void testNotifyAdmins() {
        String admin1 = "admin1";
        String admin2 = "admin2";
        List<String> adminEmails = new ArrayList<String>();
        adminEmails.add(admin1);
        adminEmails.add(admin2);
        String[] adminEmailArray = {admin1, admin2};

        // Set up mock emailer
        Emailer emailer = EasyMock.createMock(Emailer.class);

        emailer.send(subject, message, adminEmailArray);
        EasyMock.expectLastCall().times(1);

        EasyMock.replay(emailer);

        // Set up notifier
        EmailNotifier notifier = new EmailNotifier();
        notifier.setEmailer(emailer);
        notifier.setAdminEmails(adminEmails);

        // Send notification
        notifier.notifyAdmins(subject, message);

        // Verify mock
        EasyMock.verify(emailer);
    }

}
