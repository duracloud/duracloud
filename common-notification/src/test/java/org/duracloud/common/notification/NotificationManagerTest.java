/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.notification;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 12/7/11
 */
public class NotificationManagerTest {

    private String subject = "subject";
    private String message = "message";
    private String destination = "destination";
    private NotificationConfig config;

    private Notifier notifier1;
    private Notifier notifier2;

    @Before
    public void setup() {
        config = new NotificationConfig();
        config.setType("email");

        notifier1 = EasyMock.createMock(Notifier.class);
        notifier2 = EasyMock.createMock(Notifier.class);
    }

    @After
    public void teardown() {
        EasyMock.verify(notifier1, notifier2);
    }

    @Test
    public void testSendNotifcation() {
        setupMocks();

        NotificationManager manager =
            new NotificationManager(notifier1, notifier2);

        List<NotificationConfig> configList =
            new ArrayList<NotificationConfig>();
        configList.add(config);

        manager.initializeNotifiers(configList);

        manager.sendNotification(NotificationType.EMAIL,
                                 subject,
                                 message,
                                 destination);
    }

    private void setupMocks() {
        EasyMock.expect(notifier1.getNotificationType())
                .andReturn(NotificationType.EMAIL)
                .times(2);
        EasyMock.expect(notifier2.getNotificationType())
                .andReturn(NotificationType.EMAIL)
                .times(2);

        notifier1.initialize(config);
        EasyMock.expectLastCall().once();
        notifier2.initialize(config);
        EasyMock.expectLastCall().once();

        notifier1.notify(subject, message, destination);
        EasyMock.expectLastCall().once();
        notifier2.notify(subject, message, destination);
        EasyMock.expectLastCall().once();

        EasyMock.replay(notifier1, notifier2);
    }

   @Test
    public void testSendAdminNotifcation() {
        setupAdminMocks();

        NotificationManager manager =
            new NotificationManager(notifier1, notifier2);

        List<NotificationConfig> configList =
            new ArrayList<NotificationConfig>();
        configList.add(config);

        manager.initializeNotifiers(configList);

        manager.sendAdminNotification(NotificationType.EMAIL,
                                      subject,
                                      message);
    }

    private void setupAdminMocks() {
        EasyMock.expect(notifier1.getNotificationType())
                .andReturn(NotificationType.EMAIL)
                .times(2);
        EasyMock.expect(notifier2.getNotificationType())
                .andReturn(NotificationType.EMAIL)
                .times(2);

        notifier1.initialize(config);
        EasyMock.expectLastCall().once();
        notifier2.initialize(config);
        EasyMock.expectLastCall().once();

        notifier1.notifyAdmins(subject, message);
        EasyMock.expectLastCall().once();
        notifier2.notifyAdmins(subject, message);
        EasyMock.expectLastCall().once();

        EasyMock.replay(notifier1, notifier2);
    }

}
