/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.security;

import java.util.Arrays;
import java.util.Collection;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.model.Credential;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


/**
 * A custom authentication provider that grants ROLE_USER authority to 
 * a client who successfully logs into the content store manager.
 *
 * @author Daniel Bernstein
 * @version $Id$
 */
public class ContentStoreAuthenticationProvider implements AuthenticationProvider {
    private ContentStoreManager contentStoreManager;
    
    private static Collection<GrantedAuthority> USER_AUTHORITY = 
                    Arrays.asList(new GrantedAuthority[]{new SimpleGrantedAuthority("ROLE_USER")});

    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        Authentication auth = 
            new UsernamePasswordAuthenticationToken(
                           authentication.getPrincipal(), 
                           authentication.getCredentials(), 
                           USER_AUTHORITY);

        try{
            contentStoreManager.login(new Credential(username,password));
        }catch(Exception ex){
            auth.setAuthenticated(false);
        }
        
        return auth;
        
        
    }

    public boolean supports(Class authentication) {
        // TODO Auto-generated method stub
        return true;
    }

    
    public ContentStoreManager getContentStoreManager() {
        return contentStoreManager;
    }

    
    public void setContentStoreManager(ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
    }

}
