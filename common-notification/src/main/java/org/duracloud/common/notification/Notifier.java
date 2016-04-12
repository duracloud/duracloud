/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.notification;


/**
 * A Notifier is responsible for sending notifications to a destination or
 * set of destinations. Each notifier will handle distributing notifications
 * over a particular notification channel. The channel handled by the notifier
 * can be determined by asking for the notification type.
 *
 * @author: Bill Branan
 * Date: 12/2/11
 */
public interface Notifier {

    /**
     * Initializes the Notifier to be able to set up connects to begin
     * performing notification operations.
     *
     * @param notificationConfig
     */
    public void initialize(NotificationConfig notificationConfig);

    /**
     * Indicates the type of notifications sent by this notifier
     *
     * @return the channel type of this notifier
     */
    public NotificationType getNotificationType();

    /**
     * Sends a notification to a destination
     *
     * @param subject the subject of the notification
     * @param message the actual notification message
     * @param destinations where the notification is to go, could be an
     *                     email address, a URL address, etc.
     */
    public void notify(String subject, String message, String... destinations);

    /**
     * Sends a notification to configured admins
     *
     * @param subject the subject of the notification
     * @param message the actual notification message
     */
    public void notifyAdmins(String subject, String message);

}
