/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.service;

import org.duracloud.durareport.notification.NotificationManager;
import org.duracloud.durareport.notification.NotificationType;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.services.ComputeService;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: 12/7/11
 */
public class ServiceNotificationMonitorTest {

    private static final String USERNAME = "user";

    private NotificationManager notificationManager;
    private DuracloudUserDetailsService userService;

    @After
    public void teardown() {
        EasyMock.verify(notificationManager, userService);
    }

    @Test
    public void testHandleCompletionEvent() {
        setUpMocks();

        ServiceNotificationMonitor monitor =
            new ServiceNotificationMonitor(notificationManager, userService);

        Map<String, String> summaryProps = new HashMap<String, String>();
        summaryProps.put(ComputeService.SVC_LAUNCHING_USER, USERNAME);

        ServiceSummary summary = new ServiceSummary();
        summary.setProperties(summaryProps);
        monitor.handleCompletionEvent(summary);
    }

    private void setUpMocks() {
        notificationManager = EasyMock.createMock(NotificationManager.class);
        userService = EasyMock.createMock(DuracloudUserDetailsService.class);

        SecurityUserBean userBean = new SecurityUserBean();
        String emailAddress = "email";
        userBean.setEmail(emailAddress);

        EasyMock.expect(userService.getUserByUsername(USERNAME))
                .andReturn(userBean)
                .times(1);

        notificationManager.sendNotification(EasyMock.eq(NotificationType.EMAIL),
                                             EasyMock.isA(String.class),
                                             EasyMock.isA(String.class),
                                             EasyMock.eq(emailAddress));
        EasyMock.expectLastCall()
                .times(1);

        EasyMock.replay(notificationManager, userService);
    }

}
