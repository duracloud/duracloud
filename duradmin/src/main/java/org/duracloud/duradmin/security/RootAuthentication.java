/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.security;

import java.util.ArrayList;
import java.util.Collection;

import org.duracloud.common.model.RootUserCredential;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * This class comes in handy when duradmin, in the course of mediating a call from a user with ROLE_USER access,
 * needs to invoke a service that is accessible only to ROLE_ADMIN or ROLE_ROOT.
 * @author Daniel Bernstein
 *
 */
public class RootAuthentication implements Authentication{
    private RootUserCredential rootCredentials; 
    private DuracloudUserDetails user;
    public RootAuthentication(){
        this.rootCredentials = new RootUserCredential();        
        this.user =
            new DuracloudUserDetails(this.rootCredentials.getUsername(),
                                     this.rootCredentials.getPassword(),
                                     null,
                                     null,
                                     true,
                                     true,
                                     true,
                                     true,
                                     new ArrayList<GrantedAuthority>(),
                                     new ArrayList<String>(0));
        
    }
    @Override
    public String getName() {
        return rootCredentials.getUsername();
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public Object getCredentials() {
        return rootCredentials;
    }

    @Override
    public Object getPrincipal() {
        return this.user;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated)
        throws IllegalArgumentException {
        //do nothing.
    }
    @Override
    public Object getDetails() {
        return this.user;
    }

}
