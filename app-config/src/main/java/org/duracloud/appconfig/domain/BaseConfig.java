/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.apache.commons.lang.StringUtils;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.storage.domain.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This class collects the common functionality needed by durastore,
 * duradmin, duraboss, and security configurations.
 *
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public abstract class BaseConfig {
    private final Logger log = LoggerFactory.getLogger(BaseConfig.class);

    /**
     * This method loads this class with the configuration provided in props.
     *
     * @param props
     */
    public void load(Map<String, String> props) {
        if (props != null && props.size() > 0) {
            for (String key : props.keySet()) {
                if (isSupported(key)) {
                    String suffix = getSuffix(key);
                    String value = props.get(key);
                    loadProperty(suffix, value);
                }
            }
        }
    }

    protected boolean isSupported(String key) {
        return (key != null && key.startsWith(getQualifier()));
    }

    /**
     * This method provides the qualifier used to distinquish this config
     * object in a properties file.
     *
     * @return
     */
    protected abstract String getQualifier();

    /**
     * This method handles loading the given key/value into its proper,
     * application-specific field.
     *
     * @param key
     * @param value
     */
    protected abstract void loadProperty(String key, String value);

    protected String getPrefix(String key) {
        String prefix = key;
        int index = key.indexOf(".");
        if (index != -1) {
            prefix = key.substring(0, key.indexOf("."));
        }

        if (StringUtils.isBlank(prefix)) {
            String msg = "prefix not found: " + key;
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
        return prefix;
    }

    protected String getSuffix(String key) {
        String prefix = getPrefix(key);
        String suffix = key;
        if (!suffix.equals(prefix)) {
            suffix = key.substring(getPrefix(key).length() + 1);
        }

        if (StringUtils.isBlank(suffix)) {
            String msg = "suffix not found: " + key;
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
        return suffix;
    }

    protected void loadDbConfig(DatabaseConfig databaseConfig, String key, String value) {
        String suffix = getSuffix(key);
        if (suffix.equalsIgnoreCase("username")) {
            databaseConfig.setUsername(value);
        } else if (suffix.equalsIgnoreCase("password")) {
            databaseConfig.setPassword(value);
        } else if (suffix.equalsIgnoreCase("host")) {
            databaseConfig.setHost(value);
        } else if (suffix.equalsIgnoreCase("port")) {
            databaseConfig.setPort(Integer.parseInt(value));
        } else if (suffix.equalsIgnoreCase("name")) {
            databaseConfig.setName(value);
        }
    }
}
