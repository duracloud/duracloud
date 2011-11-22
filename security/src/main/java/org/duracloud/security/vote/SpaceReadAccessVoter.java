/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import org.duracloud.client.ContentStore;
import org.duracloud.common.model.SystemUserCredential;
import org.duracloud.security.domain.HttpVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import org.springframework.security.userdetails.UserDetailsService;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.duracloud.security.vote.VoterUtil.debugText;

/**
 * This class decides if a caller has READ access to a given resource. If the
 * caller is seeking WRITE access to this resource, this class abstains from
 * casting a vote.
 *
 * @author Andrew Woods
 *         Date: 11/18/11
 */
public class SpaceReadAccessVoter extends SpaceAccessVoter {

    private final Logger log =
        LoggerFactory.getLogger(SpaceReadAccessVoter.class);

    public SpaceReadAccessVoter(ContentStoreUtil contentStoreUtil,
                                UserDetailsService userDetailsService) {
        super(contentStoreUtil, userDetailsService);
    }

    /**
     * This method checks the Access and ACL state of the arg resource
     * (space and provider) and denies access to principals if they are
     * anonymous and the space is CLOSED, or if they do not have a READ ACL for
     * the space.
     *
     * @param auth     principal seeking AuthZ
     * @param resource that is under protection
     * @param config   access-attributes defined on resource
     * @return vote (AccessDecisionVoter.ACCESS_GRANTED, ACCESS_DENIED, ACCESS_ABSTAIN)
     */
    public int vote(Authentication auth,
                    Object resource,
                    ConfigAttributeDefinition config) {
        String label = "SpaceReadAccessVoterImpl";
        if (resource != null && !supports(resource.getClass())) {
            log.debug(debugText(label, auth, config, resource, ACCESS_ABSTAIN));
            return ACCESS_ABSTAIN;
        }

        HttpServletRequest httpRequest = getHttpServletRequest(resource);
        if (null == httpRequest) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        HttpVerb verb = getHttpVerb(httpRequest);
        if (null == verb) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        // This class only handles HTTP read verbs.
        if (!verb.isRead()) {
            log.debug(debugText(label, auth, config, resource, ACCESS_ABSTAIN));
            return ACCESS_ABSTAIN;
        }

        // The Admin always has READ access.
        if (isAdmin(auth.getName())) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }

        // All READs on OPEN spaces are granted.
        ContentStore.AccessType access = getSpaceAccess(httpRequest);
        if (access.equals(ContentStore.AccessType.OPEN)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }

        // Anonymous users can not READ 'closed' spaces.
        if (auth instanceof AnonymousAuthenticationToken) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        Map<String, String> acls = getSpaceACLs(httpRequest);
        if (hasReadAccess(auth.getName(), acls)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }

        List<String> userGroups = getUserGroups(auth);
        if (groupsHaveReadAccess(userGroups, acls)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }

        int grant = ACCESS_DENIED;
        log.debug(debugText(label, auth, config, resource, grant));
        return grant;
    }

}
