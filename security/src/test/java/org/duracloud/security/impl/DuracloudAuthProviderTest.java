/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Bill Branan
 *         Date: 4/23/2015
 */
public class DuracloudAuthProviderTest {

    @Test
    public void testAdditionalAuthenticationChecks() {
        assertTrue(testIpAuthChecks("1.2.3.4"));
        assertTrue(testIpAuthChecks("1.2.5.7"));

        assertFalse(testIpAuthChecks("1.2.3.5"));
        assertFalse(testIpAuthChecks("1.2.7.8"));
    }

    private boolean testIpAuthChecks(String remoteAddress) {
        DuracloudAuthProvider authProvider =
            new DuracloudAuthProvider(null, new ShaPasswordEncoder(256));

        String username = "user";
        String password = "pass";
        String passwordHash = "d74ff0ee8da3b9806b18c877dbf29bbde50b5bd8e4dad7a3a725000feb82e8f1";
        String ipLimits = "1.2.3.4/32;1.2.5.6/30";

        DuracloudUserDetails userDetails =
            EasyMock.createMock(DuracloudUserDetails.class);
        WebAuthenticationDetails webAuthDetails =
            EasyMock.createMock(WebAuthenticationDetails.class);
        UsernamePasswordAuthenticationToken authToken =
            EasyMock.createMock(UsernamePasswordAuthenticationToken.class);

        // Calls which occur as part of the call to super.additionalAuthenticationChecks()
        EasyMock.expect(authToken.getCredentials())
                .andReturn(password)
                .times(2);
        EasyMock.expect(userDetails.getPassword())
                .andReturn(passwordHash)
                .times(1);

        // Direct calls expected
        EasyMock.expect(userDetails.getIpLimits())
                .andReturn(ipLimits)
                .times(1);
        EasyMock.expect(userDetails.getUsername())
                .andReturn(username)
                .times(1);
        EasyMock.expect(authToken.getDetails())
                .andReturn(webAuthDetails)
                .times(1);
        EasyMock.expect(webAuthDetails.getRemoteAddress())
                .andReturn(remoteAddress)
                .times(1);

        EasyMock.replay(userDetails, webAuthDetails, authToken);

        boolean authAllowed = true;
        try {
            authProvider.additionalAuthenticationChecks(userDetails, authToken);
        } catch(InsufficientAuthenticationException e) {
            authAllowed = false;
        }

        EasyMock.verify(userDetails, webAuthDetails, authToken);
        return authAllowed;
    }

    @Test
    public void testIpInRange() {
        DuracloudAuthProvider authProvider =
            new DuracloudAuthProvider(null, new ShaPasswordEncoder(256));

        assertTrue(authProvider.ipInRange("1.2.3.4", "1.1.1.1/0"));
        assertTrue(authProvider.ipInRange("1.2.3.4", "1.2.3.4/32"));
        assertTrue(authProvider.ipInRange("1.2.3.127", "1.2.3.4/25"));

        assertFalse(authProvider.ipInRange("1.2.2.2", "1.2.3.4/30"));
        assertFalse(authProvider.ipInRange("1.2.3.5", "1.2.3.4/32"));
    }
}
