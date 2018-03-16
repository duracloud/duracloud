/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.notification;

/**
 * @author Andrew Woods
 * Date: 3/11/11
 */
public interface Emailer {

    /**
     * This method sends an notification to the listed recipients in text format
     *
     * @param subject    of notification
     * @param body       of notification
     * @param recipients of notification
     */
    public void send(String subject, String body, String... recipients);

    /**
     * This method sends an notification to the listed recipients in HTML format
     *
     * @param subject    of notification
     * @param body       of notification
     * @param recipients of notification
     */
    public void sendAsHtml(String subject, String body, String... recipients);
}
