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
import org.duracloud.security.domain.SecurityUserBean;
import org.duracloud.security.xml.SecurityUsersDocumentBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the configuration elements for application security.
 *
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public class SecurityConfig extends BaseConfig implements AppConfig {
    private final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final static String INIT_RESOURCE = "/security";

    protected final static String QUALIFIER = "security";
    protected final static String userKey = "user";
    protected final static String usernameKey = "username";
    protected final static String passwordKey = "password";
    protected final static String enabledKey = "enabled";
    protected final static String emailKey = "email";
    protected final static String ipLimitsKey = "iplimits";
    protected final static String acctNonExpiredKey = "acct-non-expired";
    protected final static String credNonExpiredKey = "cred-non-expired";
    protected final static String acctNonLockedKey = "acct-non-locked";
    protected final static String grantsKey = "grants";
    protected final static String groupsKey = "groups";

    private Map<String, SecurityUserBean> users = new HashMap<String, SecurityUserBean>();

    public String asXml() {
        return SecurityUsersDocumentBinding.createDocumentFrom(getUsers());
    }

    public String getInitResource() {
        return INIT_RESOURCE;
    }

    protected String getQualifier() {
        return QUALIFIER;
    }

    protected void loadProperty(String key, String value) {
        key = key.toLowerCase();
        String prefix = getPrefix(key);
        if (prefix.equalsIgnoreCase(userKey)) {
            String suffix = getSuffix(key);
            loadUser(suffix, value);

        } else {
            String msg = "unknown key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    private void loadUser(String key, String value) {
        String id = getPrefix(key);
        SecurityUserBean user = users.get(id);
        if (null == user) {
            user = new SecurityUserBean();
        }

        String suffix = key.substring(id.length() + 1).toLowerCase();
        if (StringUtils.isBlank(suffix)) {
            String msg = "invalid key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }

        if (suffix.equalsIgnoreCase(usernameKey)) {
            user.setUsername(value);

        } else if (suffix.equalsIgnoreCase(passwordKey)) {
            user.setPassword(value);

        } else if (suffix.equalsIgnoreCase(enabledKey)) {
            user.setEnabled(Boolean.valueOf(value));

        } else if (suffix.equalsIgnoreCase(emailKey)) {
            user.setEmail(value);

        } else if (suffix.equalsIgnoreCase(ipLimitsKey)) {
            user.setIpLimits(value);

        } else if (suffix.equalsIgnoreCase(acctNonExpiredKey)) {
            user.setAccountNonExpired(Boolean.valueOf(value));

        } else if (suffix.equalsIgnoreCase(credNonExpiredKey)) {
            user.setCredentialsNonExpired(Boolean.valueOf(value));

        } else if (suffix.equalsIgnoreCase(acctNonLockedKey)) {
            user.setAccountNonLocked(Boolean.valueOf(value));

        } else if (suffix.startsWith(grantsKey)) {
            user.addGrantedAuthority(value);

        } else if (suffix.startsWith(groupsKey)) {
            user.addGroup(value);

        } else {
            String msg = "unknown user key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }

        users.put(id, user);
    }

    public Collection<SecurityUserBean> getUsers() {
        return users.values();
    }

}
