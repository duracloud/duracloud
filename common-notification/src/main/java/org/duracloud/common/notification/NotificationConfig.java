/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.notification;

import java.util.List;

/**
 * Configuration information necessary to set up notification.
 *
 * @author: Bill Branan
 * Date: 12/6/11
 */
public class NotificationConfig {

    /* The type of notification system to be configured */
    private String type;

    /* Username necessary to authenticate with notification system */
    private String username;

    /* Password necessary to authenticate with notification system */
    private String password;

    /* The source of notifications (from address for email) */
    private String originator;

    /* For administrative notifications (list of addresses for email) */
    private List<String> admins;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }

}
