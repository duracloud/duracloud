/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import org.duracloud.security.DuracloudUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

/**
 * Authentication provider which allows default authentication behavior
 * of the spring DaoAuthenticationProvider, but adds a check to see if the
 * user's request originated from an IP address which is within the defined
 * valid IP ranges. If a user has no defined valid IP ranges, any IP is accepted.
 *
 * @author Bill Branan
 *         Date: 4/22/2015
 */
public class DuracloudAuthProvider extends DaoAuthenticationProvider {

    private final Logger log =
        LoggerFactory.getLogger(DuracloudAuthProvider.class);

    public DuracloudAuthProvider(DuracloudUserDetailsService userDetailsService,
                                 Object passwordEncoder) {
        super.setUserDetailsService(userDetailsService);
        super.setPasswordEncoder(passwordEncoder);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        super.additionalAuthenticationChecks(userDetails, authentication);

        DuracloudUserDetails dcUserDetails = (DuracloudUserDetails)userDetails;
        String userIpLimits = dcUserDetails.getIpLimits();

        // if user IP limits are set, check request IP
        if(null != userIpLimits && !userIpLimits.equals("")) {
            WebAuthenticationDetails details =
                (WebAuthenticationDetails)authentication.getDetails();
            String requestIp = details.getRemoteAddress();

            String[] ipLimits = userIpLimits.split(";");
            for(String ipLimit : ipLimits) {
                if(ipInRange(requestIp, ipLimit)) {
                    // User's IP is within this range, grant access
                    log.debug("Allowing authentication check to continue for user " +
                              dcUserDetails.getUsername() + " because their IP " +
                              requestIp + " exists in a valid range " + ipLimit);
                    return;
                }
            }

            // There are IP limits, and none of them match the user's IP, deny
            log.debug("Denying authentication request for user " +
                      dcUserDetails.getUsername() + " because their IP " +
                      requestIp + " does not match any valid ranges " + userIpLimits);
            throw new InsufficientAuthenticationException(
                "Originating IP for authentication request" + requestIp +
                " is not in an accepted range.");
        } else { // No user IP limits, which means all IPs are accepted
            log.debug("Allowing authentication check to continue for user " +
                      dcUserDetails.getUsername() + " because no IP limits are defined");
            return;
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
