/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;


import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Andrew Woods
 *         Date: 11/11/11
 */
public class DuracloudUserDetails extends User implements UserDetails {

    private String email;
    private List<String> groups;

    public DuracloudUserDetails(String username,
                                String password,
                                String email,
                                boolean enabled,
                                boolean accountNonExpired,
                                boolean credentialsNonExpired,
                                boolean accountNonLocked,
                                Collection<GrantedAuthority> authorities,
                                List<String> groups)
        throws IllegalArgumentException {
        super(username,
              password,
              enabled,
              accountNonExpired,
              credentialsNonExpired,
              accountNonLocked,
              authorities);
        this.email = email;
        this.groups = groups;
    }

    public String getEmail() {
        return email;
    }
    
    @Override
    public void eraseCredentials() {
        //The credentials are getting erased despite my using the  
        //erase-credentials="false" in security-config.xml
        //overriding prevents the erasure from occurring.
        //--db
        //prevent password from being erased.
    }

    public List<String> getGroups() {
        return groups;
    }
}
