/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import org.duracloud.security.impl.DuracloudUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static org.duracloud.security.vote.VoterUtil.debugText;

/**
 * Votes on access depending on user IP limits. If IP limits are defined
 * for a user, then any requests made by that user must originate from
 * a valid IP range. If no IP limits are defined, ABSTAIN. If IP limits
 * are defined and the user's request is in a valid range, GRANT. If IP
 * limits are defined and the user's request is not in a defined valid range,
 * DENY.
 *
 * @author Bill Branan
 *         Date: 04/15/15
 */
public class UserIpLimitsAccessVoter implements AccessDecisionVoter {

    private final Logger log =
        LoggerFactory.getLogger(UserIpLimitsAccessVoter.class);

    /**
     * This method always returns true because all configAttributes are able
     * to be handled by this voter.
     *
     * @param configAttribute any att
     * @return true
     */
    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    /**
     * This methods returns true if the arg class is an instance of or
     * subclass of FilterInvocation.
     * No other classes can be handled by this voter.
     *
     * @param aClass to be analyized for an AuthZ vote.
     * @return true if is an instance of or subclass of FilterInvocation
     */
    @Override
    public boolean supports(Class aClass) {
        return FilterInvocation.class.isAssignableFrom(aClass);
    }

    /**
     * This method checks the IP limits of the principal and denys access if
     * those limits exist and the request is coming from outside the specified
     * range.
     *
     * @param auth     principal seeking AuthZ
     * @param resource that is under protection
     * @param config   access-attributes defined on resource
     * @return vote (AccessDecisionVoter.ACCESS_GRANTED, ACCESS_DENIED, ACCESS_ABSTAIN)
     */
    public int vote(Authentication auth,
                    Object resource,
                    Collection config) {
        String label = "UserIpLimitsAccessVoter";
        if (resource != null && !supports(resource.getClass())) {
            log.debug(debugText(label, auth, config, resource, ACCESS_ABSTAIN));
            return ACCESS_ABSTAIN;
        }

        FilterInvocation invocation = (FilterInvocation) resource;
        HttpServletRequest httpRequest = invocation.getHttpRequest();
        if (null == httpRequest) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        String userIpLimits = getUserIpLimits(auth);
        // if user IP limits are set, check request IP
        if(null != userIpLimits && !userIpLimits.equals("")) {
            String requestIp = httpRequest.getRemoteAddr();

            String[] ipLimits = userIpLimits.split(";");
            for(String ipLimit : ipLimits) {
                if(ipInRange(requestIp, ipLimit)) {
                    // User's IP is within this range, grant access
                    log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
                    return ACCESS_GRANTED;
                }
            }

            // There are IP limits, and none of them match the user's IP, deny
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        } else { // No user IP limits, abstain
            log.debug(debugText(label, auth, config, resource, ACCESS_ABSTAIN));
            return ACCESS_ABSTAIN;
        }
    }

    /**
     * Retrieves the ip limits defined for a given user
     *
     * @param auth Authentication where user details can be found
     * @return user ip limits, or null if no limits are set
     */
    protected String getUserIpLimits(Authentication auth) {
        Object principal = auth.getPrincipal();

        if(principal instanceof DuracloudUserDetails) {
            DuracloudUserDetails userDetails = (DuracloudUserDetails) principal;
            return userDetails.getIpLimits();
        } else {
            return null;
        }
    }

    /**
     * Determines if a given IP address is in the given IP range.
     *
     * @param ipAddress single IP address
     * @param range IP address range using CIDR notation
     * @return true if the address is in the range, false otherwise
     */
    protected boolean ipInRange(String ipAddress, String range) {
        IpAddressMatcher addressMatcher = new IpAddressMatcher(range);
        return addressMatcher.matches(ipAddress);
    }

}
