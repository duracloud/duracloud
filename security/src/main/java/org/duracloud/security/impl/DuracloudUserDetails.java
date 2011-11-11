/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;

import java.util.List;

/**
 * @author Andrew Woods
 *         Date: 11/11/11
 */
public class DuracloudUserDetails extends User implements UserDetails {

    private List<String> groups;

    public DuracloudUserDetails(String username,
                                String password,
                                boolean enabled,
                                boolean accountNonExpired,
                                boolean credentialsNonExpired,
                                boolean accountNonLocked,
                                GrantedAuthority[] authorities,
                                List<String> groups)
        throws IllegalArgumentException {
        super(username,
              password,
              enabled,
              accountNonExpired,
              credentialsNonExpired,
              accountNonLocked,
              authorities);
        this.groups = groups;
    }

    public List<String> getGroups() {
        return groups;
    }
}
