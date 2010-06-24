/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.context;

import org.duracloud.common.model.Credential;
import org.duracloud.security.error.NoUserLoggedInException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;

/**
 * This class returns the Credential of the user currently logged into the
 * session.
 *
 * @author Andrew Woods
 *         Date: Mar 27, 2010
 */
public class SecurityContextUtil {

    private final Logger log = LoggerFactory.getLogger(SecurityContextUtil.class);

    public Credential getCurrentUser() throws NoUserLoggedInException {
        Credential credential = null;

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        if (null == auth) {
            log.debug("no user-auth found.");
            throw new NoUserLoggedInException();
        }

        Object obj = auth.getPrincipal();
        if (obj instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) obj;
            credential = new Credential(userDetails.getUsername(),
                                        userDetails.getPassword());
        } else {
            log.debug("no user logged in.");
            throw new NoUserLoggedInException();
        }

        log.debug("user in context: " + credential.toString());
        return credential;
    }
}
