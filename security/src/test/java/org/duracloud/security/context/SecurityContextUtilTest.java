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
import org.duracloud.security.impl.DuracloudUserDetails;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: 11/22/11
 */
public class SecurityContextUtilTest {

    private SecurityContextUtil util;
    private SecurityContext context;

    private String username = "username";
    private String password = "password";
    private String email = "email";
    private GrantedAuthority[] authorities;
    private List<String> groups;

    @Before
    public void setUp() throws Exception {
        util = new SecurityContextUtil();

        context = EasyMock.createMock("SecurityContext", SecurityContext.class);

        authorities = new GrantedAuthority[]{new GrantedAuthorityImpl(
            "ROLE_USER"), new GrantedAuthorityImpl("ROLE_ADMIN")};
        groups = new ArrayList<String>();
        groups.add("group-curators");
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(context);
    }

    private void replayMocks() {
        EasyMock.replay(context);
    }

    @Test
    public void testGetCurrentUsername() throws Exception {
        createCurrentUserMock();

        String user = util.getCurrentUsername();
        Assert.assertNotNull(user);
        Assert.assertEquals(username, user);
    }

    @Test
    public void testGetCurrentUser() throws Exception {
        createCurrentUserMock();

        Credential user = util.getCurrentUser();
        Assert.assertNotNull(user);
        Assert.assertEquals(username, user.getUsername());
    }

    private void createCurrentUserMock() {
        Authentication auth = EasyMock.createMock("Authentication",
                                                  Authentication.class);
        DuracloudUserDetails userDetails = new DuracloudUserDetails(username,
                                                                    password,
                                                                    email,
                                                                    true,
                                                                    true,
                                                                    true,
                                                                    true,
                                                                    authorities,
                                                                    groups);
        EasyMock.expect(auth.getPrincipal()).andReturn(userDetails);
        EasyMock.replay(auth);
        EasyMock.expect(context.getAuthentication()).andReturn(auth);
        replayMocks();

        SecurityContextHolder.setContext(context);
    }

    @Test
    public void testGetCurrentUserError() throws Exception {
        replayMocks();
        SecurityContextHolder.clearContext();

        try {
            util.getCurrentUser();
            Assert.fail("exception expected.");

        } catch (NoUserLoggedInException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetCurrentUserDetails() throws NoUserLoggedInException {
        createCurrentUserMock();

        DuracloudUserDetails userDetails = util.getCurrentUserDetails();
        Assert.assertNotNull(userDetails);

        GrantedAuthority[] auths = userDetails.getAuthorities();
        Assert.assertNotNull(auths);
        List<GrantedAuthority> authsList = Arrays.asList(auths);

        Assert.assertEquals(authorities.length, authsList.size());
        for (GrantedAuthority authority : authorities) {
            Assert.assertTrue(authsList.contains(authority));
        }

        Assert.assertEquals(groups, userDetails.getGroups());
        Assert.assertEquals(username, userDetails.getUsername());
        Assert.assertEquals(email, userDetails.getEmail());
    }

    @Test
    public void testGetCurrentUserDetailsError() throws Exception {
        replayMocks();
        SecurityContextHolder.clearContext();

        try {
            util.getCurrentUserDetails();
            Assert.fail("exception expected.");

        } catch (NoUserLoggedInException e) {
            Assert.assertNotNull(e);
        }
    }

}
