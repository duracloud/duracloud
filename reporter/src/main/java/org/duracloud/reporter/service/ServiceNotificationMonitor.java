/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reporter.service;

import org.duracloud.common.notification.NotificationManager;
import org.duracloud.common.notification.NotificationType;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.servicemonitor.ServiceMonitorEventHandler;
import org.duracloud.services.ComputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author: Bill Branan
 * Date: 12/2/11
 */
public class ServiceNotificationMonitor implements ServiceMonitorEventHandler {

    private final Logger log =
        LoggerFactory.getLogger(ServiceNotificationMonitor.class);

    private NotificationManager notificationManager;
    private DuracloudUserDetailsService userService;

    public ServiceNotificationMonitor(NotificationManager notificationManager,
                                      DuracloudUserDetailsService userService) {
        this.notificationManager = notificationManager;
        this.userService = userService;
    }

    @Override
    public void handleDeployEvent() {

    }

    @Override
    public void handleUndeployEvent() {

    }

    @Override
    public void handleUpdateConfigEvent() {

    }

    @Override
    public void handleCompletionEvent(ServiceSummary summary) {
        Map<String, String> serviceProps = summary.getProperties();
        String username = serviceProps.get(ComputeService.SVC_LAUNCHING_USER);

        // Retrieve the user (in order to get their email address)
        SecurityUserBean userBean = userService.getUserByUsername(username);
        if(null != userBean) {
            String emailAddress = userBean.getEmail();

            String serviceName = summary.getName();

            String subject = serviceName + " Service Complete";
            StringBuilder msg = new StringBuilder();
            msg.append("The DuraCloud service which you launched has ");
            msg.append("completed. Following are details about the completed ");
            msg.append("service:\n\n    Service name: ").append(serviceName);

            for(String propKey : serviceProps.keySet()) {
                String propValue = serviceProps.get(propKey);
                if(propKey.equals(ComputeService.SVC_LAUNCHING_USER)) {
                    // Leave out
                } else {
                    msg.append("\n    ").append(propKey);
                    msg.append(": ").append(propValue);
                }
            }

            notificationManager.sendNotification(NotificationType.EMAIL,
                                                 subject,
                                                 msg.toString(),
                                                 emailAddress);
        } else {
            log.error("Unable to send service completion notification, " +
                      "as there was no user found with username: " + username);
        }
    }
}
