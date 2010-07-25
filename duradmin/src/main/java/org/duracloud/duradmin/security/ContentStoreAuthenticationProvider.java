/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.security;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.model.Credential;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;


/**
 * A custom authentication provider that grants ROLE_USER authority to 
 * a client who successfully logs into the content store manager.
 *
 * @author Daniel Bernstein
 * @version $Id$
 */
public class ContentStoreAuthenticationProvider implements AuthenticationProvider {
    private ContentStoreManager contentStoreManager;
    
    private static GrantedAuthority[] USER_AUTHORITY = 
                        new GrantedAuthority[]{new GrantedAuthorityImpl("ROLE_USER")};

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
