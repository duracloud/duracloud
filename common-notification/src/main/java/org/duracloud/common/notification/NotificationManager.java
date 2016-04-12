/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.notification;


import java.util.Collection;

/**
 * Manages the set of notifiers which have been configured. Provides a way to
 * both initialize and send notifications to the notifiers based on the
 * notification type.
 *
 * @author: Bill Branan
 * Date: 12/2/11
 */
public class NotificationManager {

    private Notifier[] notifiers;

    public NotificationManager(Notifier... notifiers) {
        this.notifiers = notifiers;
    }

    /**
     * Initializes notifiers using the provided configuration. It is expected
     * that there will be exactly one config for each notifier type.
     * - If there is more than one config for a given type, the last
     *   configuration of that type in the list will win.
     * - If there is a type not represented in the config list, then all
     *   notifiers of that type will remain uninitialized.
     *
     * @param notificationConfigs set of configuration for notifiers
     */
    public void initializeNotifiers(Collection<NotificationConfig> notificationConfigs) {
        for(NotificationConfig config : notificationConfigs) {
            for(Notifier notifier : notifiers) {
                if(notifier.getNotificationType().name().equalsIgnoreCase(
                    config.getType())) {
                    notifier.initialize(config);
                }
            }
        }
    }

    /**
     * Sends a notification through all configured notifiers of a given type.
     *
     * @param type of notification to be sent
     * @param subject of the notification
     * @param message of the notification
     * @param destinations where notification is to be sent
     */
    public void sendNotification(NotificationType type,
                                 String subject,
                                 String message,
                                 String... destinations) {
        for(Notifier notifier : notifiers) {
            if(notifier.getNotificationType().equals(type)) {
                notifier.notify(subject, message, destinations);
            }
        }
    }

    /**
     * Sends a notification to system administrators through all configured
     * notifiers of a given type.
     *
     * @param type of notification to be sent
     * @param subject of the notification
     * @param message of the notification
     */
    public void sendAdminNotification(NotificationType type,
                                      String subject,
                                      String message) {
        for(Notifier notifier : notifiers) {
            if(notifier.getNotificationType().equals(type)) {
                notifier.notifyAdmins(subject, message);
            }
        }
    }

}
