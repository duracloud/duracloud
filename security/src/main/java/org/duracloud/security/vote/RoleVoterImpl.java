/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.core.Authentication;

/**
 * This class wraps the Spring-RoleVoter for debug visibility.
 *
 * @author Andrew Woods
 *         Date: Mar 12, 2010
 */
public class RoleVoterImpl extends RoleVoter {
    private final Logger log = LoggerFactory.getLogger(RoleVoterImpl.class);

    /**
     * This method is a pass-through for Spring-RoleVoter.
     *
     * @param authentication principal seeking AuthZ
     * @param resource       that is under protection
     * @param config         access-attributes defined on resource
     * @return vote (AccessDecisionVoter.ACCESS_GRANTED, ACCESS_DENIED, ACCESS_ABSTAIN)
     */
    @Override
    public int vote(Authentication authentication,
                    Object resource,
                    Collection<ConfigAttribute> config) {
        int decision = super.vote(authentication, resource, config);
        log.debug(VoterUtil.debugText("RoleVoterImpl",
                                      authentication,
                                      config,
                                      resource,
                                      decision));
        return decision;
    }

}