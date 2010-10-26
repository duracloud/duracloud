/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.duracloud.servicesutil.util.DuraConfigAdmin;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 *         Date: Jan 1, 2010
 */
public class DuraConfigAdminImpl implements DuraConfigAdmin {

    private final Logger log = LoggerFactory.getLogger(DuraConfigAdminImpl.class);

    private ConfigurationAdmin osgiConfigAdmin;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getConfiguration(String configId)
        throws Exception {
        Map<String, String> props = new HashMap<String, String>();

        // A new Configuration is created if one does not already exist.
        Configuration config = getOsgiConfigAdmin().getConfiguration(configId);

        Dictionary dictionary = config.getProperties();
        if (dictionary != null) {
            Enumeration keys = dictionary.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                props.put(key, (String) dictionary.get(key));
            }
        } else {
            log.warn("No config found for config-id: '" + configId + "'");
        }
        return props;
    }

    /**
     * {@inheritDoc}
     */
    public void updateConfiguration(String configId, Map<String, String> props)
        throws Exception {
        doUpdateConfiguration(configId, props);
    }

    private void doUpdateConfiguration(String configId,
                                       Map<String, String> props)
        throws Exception {
        // Exit if no meaningful properties provided.
        if (props == null) {
            log.warn("arg props is null");
            return;
        }
        log.debug("arg props for {}: {}", configId, props.toString());

        Configuration config = getOsgiConfigAdmin().getConfiguration(configId);
        Dictionary<String, String> dictionary = getExistingProperties(config);
        for (String key : props.keySet()) {
            String val = props.get(key);
            dictionary.put(key, val);
        }

        // Push the update into the osgi-container.
        log.debug("1. before config push to container: " + configId);
        config.update(dictionary);
        log.debug("2. after  config push to container: " + configId);
    }

    @SuppressWarnings("unchecked")
    private Dictionary<String, String> getExistingProperties(Configuration config)
        throws Exception {
        Dictionary dictionary = config.getProperties();
        if (dictionary == null) {
            dictionary = new Properties();
        }
        return dictionary;
    }

    public ConfigurationAdmin getOsgiConfigAdmin() {
        return osgiConfigAdmin;
    }

    public void setOsgiConfigAdmin(ConfigurationAdmin osgiConfigAdmin) {
        this.osgiConfigAdmin = osgiConfigAdmin;
    }

}
