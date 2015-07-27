/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class SecurityUtil {
    
    public static boolean isRoot(Authentication auth) {
        for(GrantedAuthority g : auth.getAuthorities()){
            if(g.getAuthority().equals("ROLE_ROOT")){
                return true;
            }
        }
        return false;
    }
}
