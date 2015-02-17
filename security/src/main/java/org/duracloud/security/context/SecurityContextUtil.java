/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.context;

import org.duracloud.common.error.NoUserLoggedInException;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.UserUtil;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This class returns the Credential of the user currently logged into the
 * session.
 *
 * @author Andrew Woods
 *         Date: Mar 27, 2010
 */
public class SecurityContextUtil implements UserUtil {

    private final Logger log = LoggerFactory.getLogger(SecurityContextUtil.class);

    static {
        //We set this value here in order to ensure that spawned threads do not 
        //lose their security context. 
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
    
    @Override
    public String getCurrentUsername() throws NoUserLoggedInException {
        DuracloudUserDetails userDetails = getCurrentUserDetails();
        return userDetails.getUsername();
    }

    public Credential getCurrentUser() throws NoUserLoggedInException {
        DuracloudUserDetails userDetails = getCurrentUserDetails();
        Credential credential = new Credential(userDetails.getUsername(),
                                               userDetails.getPassword());

        log.debug("user in context: " + credential.toString());
        return credential;
    }

    public DuracloudUserDetails getCurrentUserDetails()
        throws NoUserLoggedInException {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        if (null == auth) {
            log.debug("no user-auth found.");
            throw new NoUserLoggedInException();
        }

        Object obj = auth.getPrincipal();
        if (obj instanceof DuracloudUserDetails) {
            return (DuracloudUserDetails) obj;

        } else {
            log.debug("no user logged in: {}", obj.getClass().getName());
            throw new NoUserLoggedInException();
        }
    }

}
