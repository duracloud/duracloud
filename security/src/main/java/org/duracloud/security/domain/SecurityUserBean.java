/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.domain;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Mar 28, 2010
 */
public class SecurityUserBean {
    private String username;
    private String password;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    private boolean accountNonLocked;
    private List<String> grantedAuthorities;

    public static final String SCHEMA_VERSION = "0.2";

    public SecurityUserBean() {
        this("unknown",
             "unknown",
             false,
             false,
             false,
             false,
             new ArrayList<String>());
    }

    public SecurityUserBean(String username,
                            String password,
                            List<String> grantedAuthorities) {
        this(username, password, true, true, true, true, grantedAuthorities);
    }

    public SecurityUserBean(String username,
                            String password,
                            boolean enabled,
                            boolean accountNonExpired,
                            boolean credentialsNonExpired,
                            boolean accountNonLocked,
                            List<String> grantedAuthorities) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.grantedAuthorities = grantedAuthorities;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public List<String> getGrantedAuthorities() {
        return grantedAuthorities;
    }

    public void setUsername(String username) {
        if (!StringUtils.isBlank(username)) {
            this.username = username;
        }
    }

    public void setPassword(String password) {
        if (!StringUtils.isBlank(password)) {
            this.password = password;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void setGrantedAuthorities(List<String> grantedAuthorities) {
        this.grantedAuthorities = grantedAuthorities;
    }

    public void addGrantedAuthority(String grantedAuthority) {
        if (null == this.grantedAuthorities) {
            this.grantedAuthorities = new ArrayList<String>();
        }
        this.grantedAuthorities.add(grantedAuthority);
    }
}
