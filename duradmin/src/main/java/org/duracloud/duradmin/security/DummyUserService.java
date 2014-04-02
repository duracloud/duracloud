/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.security;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Spring security configuration seems to require a user detail service.  I played with it for a while
 * and couldn't see how to get around this requirement.  This class doesn't seem to be called 
 * by the security framework at all. 
 * @author Daniel Bernstein
 * @version $Id$
 */
public class DummyUserService implements UserDetailsService {

    public UserDetails loadUserByUsername(final String username)
            throws UsernameNotFoundException, DataAccessException {
        
        throw new UsernameNotFoundException(username);
    }

}
