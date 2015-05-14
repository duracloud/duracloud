/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import org.duracloud.security.impl.DuracloudUserDetails;
import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_ABSTAIN;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_DENIED;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_GRANTED;

/**
 * @author Bill Branan
 *         Date: 4/24/2015
 */
public class UserIpLimitsAccessVoterTest {

    /**
     * Test to verify that the voter will abstain from voting if the resource
     * type is not supported.
     */
    @Test
    public void testVoteResourceType() {
        UserIpLimitsAccessVoter voter = new UserIpLimitsAccessVoter();

        Authentication auth = EasyMock.createMock(Authentication.class);
        EasyMock.expect(auth.getName()).andReturn("auth-name");

        Collection<ConfigAttribute> config = Collections.emptyList();

        EasyMock.replay(auth);

        int voteResult = voter.vote(auth, "invalid-resource", config);
        assertEquals(ACCESS_ABSTAIN, voteResult);

        EasyMock.verify(auth);
    }

    /**
     * Test to verify that a matching IP address and range results in an
     * access granted vote.
     */
    @Test
    public void testVoteIPRange() {
        // No specified IP limts (expect abstain)
        String ipLimits = "";
        String userIp = "1.2.3.4";
        doTestVote(ipLimits, userIp, ACCESS_ABSTAIN);

        // User IP in specific IP range (expect grant)
        ipLimits = "1.2.3.4/32;1.2.5.6/30";
        doTestVote(ipLimits, userIp, ACCESS_GRANTED);

        // User IP in broad IP range (expect grant)
        userIp = "1.2.5.7";
        doTestVote(ipLimits, userIp, ACCESS_GRANTED);

        // User IP outside of defined limit (expect deny)
        userIp = "5.5.5.5";
        doTestVote(ipLimits, userIp, ACCESS_DENIED);
    }

    private void doTestVote(String ipLimits, String userIp, int expectedVote) {
        UserIpLimitsAccessVoter voter = new UserIpLimitsAccessVoter();

        Authentication auth = EasyMock.createMock(Authentication.class);
        DuracloudUserDetails userDetails = EasyMock.createMock(DuracloudUserDetails.class);
        FilterInvocation resource = EasyMock.createMock(FilterInvocation.class);
        HttpServletRequest httpRequest = EasyMock.createMock(HttpServletRequest.class);

        EasyMock.expect(auth.getName()).andReturn("auth-name");
        EasyMock.expect(auth.getPrincipal()).andReturn(userDetails);
        EasyMock.expect(userDetails.getIpLimits()).andReturn(ipLimits);
        EasyMock.expect(resource.getHttpRequest()).andReturn(httpRequest);
        EasyMock.expect(httpRequest.getRemoteAddr()).andReturn(userIp).anyTimes();

        Collection<ConfigAttribute> config = Collections.emptyList();

        EasyMock.replay(auth, userDetails, resource, httpRequest);

        int voteResult = voter.vote(auth, resource, config);
        assertEquals(expectedVote, voteResult);

        EasyMock.verify(auth, userDetails, resource, httpRequest);
    }

    /**
     * Test to verify that a call without an authenticated user (anonymous)
     * will result in an ABSTAIN response
     */
    @Test
    public void testAnonymousUser() {
        UserIpLimitsAccessVoter voter = new UserIpLimitsAccessVoter();

        Authentication auth = EasyMock.createMock(Authentication.class);
        FilterInvocation resource = EasyMock.createMock(FilterInvocation.class);
        HttpServletRequest httpRequest = EasyMock.createMock(HttpServletRequest.class);

        EasyMock.expect(auth.getName()).andReturn("auth-name");
        EasyMock.expect(auth.getPrincipal()).andReturn("anonymous-auth");
        EasyMock.expect(resource.getHttpRequest()).andReturn(httpRequest);

        Collection<ConfigAttribute> config = Collections.emptyList();

        EasyMock.replay(auth, resource, httpRequest);

        int voteResult = voter.vote(auth, resource, config);
        assertEquals(ACCESS_ABSTAIN, voteResult);

        EasyMock.verify(auth, resource, httpRequest);
    }

}
