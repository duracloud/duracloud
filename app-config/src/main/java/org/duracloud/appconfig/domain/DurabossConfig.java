/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.duracloud.appconfig.xml.DurabossInitDocumentBinding;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class DurabossConfig extends DuradminConfig {

    public static final String QUALIFIER = "duraboss";
    public static final String notificationKey = "notification";
    public static final String notificationTypeKey = "type";
    public static final String notificationUsernameKey = "username";
    public static final String notificationPasswordKey = "password";
    public static final String notificationOriginatorKey = "originator";

    private Map<String, NotificationConfig> notificationConfigs =
        new HashMap<String, NotificationConfig>();

    @Override
    protected boolean subclassLoadProperty(String key, String value) {
        String prefix = getPrefix(key);
        if (prefix.equalsIgnoreCase(notificationKey)) {
            String suffix = getSuffix(key);
            loadNotification(suffix, value);
            return true;
        } else {
            return false;
        }
    }

    private void loadNotification(String key, String value) {
        String id = getPrefix(key);
        String suffix = getSuffix(key);

        NotificationConfig config = notificationConfigs.get(id);
        if(null == config) {
            config = new NotificationConfig();
        }

        if(suffix.equalsIgnoreCase(notificationTypeKey)) {
            config.setType(value);
        } else if (suffix.equalsIgnoreCase(notificationUsernameKey)) {
            config.setUsername(value);
        } else if (suffix.equalsIgnoreCase(notificationPasswordKey)) {
            config.setPassword(value);
        } else if (suffix.equalsIgnoreCase(notificationOriginatorKey)) {
            config.setOriginator(value);
        }

        notificationConfigs.put(id, config);
    }

    @Override
    public String getInitResource() {
        return INIT_RESOURCE;
    }

    @Override
    protected String getQualifier() {
        return QUALIFIER;
    }

    @Override
    protected boolean isSupported(String key) {
        return (key != null &&
                (key.startsWith(getQualifier()) ||
                 key.startsWith(super.getQualifier())));
    }

    @Override
    public String asXml() {
        return DurabossInitDocumentBinding.createDocumentFrom(this);
    }

    /**
     * Set the notification configurations.
     * Only values stored in the map are relevant, keys can be anything.
     * @param notificationConfigs
     */
    public void setNotificationConfigs (
        Map<String, NotificationConfig> notificationConfigs) {
        this.notificationConfigs = notificationConfigs;
    }

    public Collection<NotificationConfig> getNotificationConfigs() {
        return notificationConfigs.values();
    }

}
