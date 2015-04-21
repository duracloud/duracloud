/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security;

import java.util.ArrayList;
import java.util.List;

import org.duracloud.common.model.Credential;
import org.duracloud.common.model.RootUserCredential;
import org.duracloud.common.model.SystemUserCredential;
import org.duracloud.security.domain.SecurityUserBean;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.security.impl.UserDetailsServiceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author Andrew Woods
 *         Date: Mar 28, 2010
 */
public class UserDetailsServiceImplTest {

    private UserDetailsServiceImpl userDetailsService;

    private final String usernameA = "a-user";
    private final String usernameB = "b-user";
    private final String usernameC = "c-user";

    private final String emailA = "a@email.com";
    private final String emailB = "b@email.com";
    private final String emailC = "c@email.com";

    private final String ipLimitsA = "";
    private final String ipLimitsB = "1.1.1.1/0";
    private final String ipLimitsC = "1.2.3.4/32";

    private List<String> grantsA;
    private List<String> grantsB;
    private List<String> grantsC;

    private List<String> groupsA;
    private List<String> groupsB;
    private List<String> groupsC;

    private List<SecurityUserBean> users;

    @Before
    public void setUp() {
        userDetailsService = new UserDetailsServiceImpl();

        grantsA = new ArrayList<String>();
        grantsB = new ArrayList<String>();
        grantsC = new ArrayList<String>();

        grantsA.add("ROLE_ROOT");
        grantsA.add("ROLE_ADMIN");
        grantsA.add("ROLE_USER");

        grantsB.add("ROLE_ADMIN");
        grantsB.add("ROLE_USER");

        grantsC.add("ROLE_USER");

        groupsA = new ArrayList<String>();
        groupsB = new ArrayList<String>();
        groupsC = new ArrayList<String>();

        groupsA.add("group.b.0");

        groupsB.add("group.b.0");
        groupsB.add("group.b.1");

        groupsC.add("group.c.0");
        groupsC.add("group.c.1");
        groupsC.add("group.c.2");

        SecurityUserBean user0 = new SecurityUserBean(usernameA,
                                                      "apw",
                                                      emailA,
                                                      ipLimitsA,
                                                      true,
                                                      true,
                                                      true,
                                                      true,
                                                      grantsA,
                                                      groupsA);
        SecurityUserBean user1 = new SecurityUserBean(usernameB,
                                                      "apw",
                                                      emailB,
                                                      ipLimitsB,
                                                      true,
                                                      true,
                                                      true,
                                                      true,
                                                      grantsB,
                                                      groupsB);
        SecurityUserBean user2 = new SecurityUserBean(usernameC,
                                                      "upw",
                                                      emailC,
                                                      ipLimitsC,
                                                      true,
                                                      true,
                                                      true,
                                                      true,
                                                      grantsC,
                                                      groupsC);
        users = new ArrayList<SecurityUserBean>();
        users.add(user0);
        users.add(user1);
        users.add(user2);
    }

    @After
    public void tearDown() {
        userDetailsService = null;
        grantsA = null;
        grantsB = null;
        grantsC = null;
        users = null;
    }

    @Test
    public void testLoadUserByUsername() {
        userDetailsService.setUsers(users);
        DuracloudUserDetails udA = (DuracloudUserDetails) userDetailsService.loadUserByUsername(usernameA);
        DuracloudUserDetails udB = (DuracloudUserDetails) userDetailsService.loadUserByUsername(usernameB);
        DuracloudUserDetails udC = (DuracloudUserDetails) userDetailsService.loadUserByUsername(usernameC);

        Assert.assertNotNull(udA);
        Assert.assertNotNull(udB);
        Assert.assertNotNull(udC);

        Assert.assertEquals(usernameA, udA.getUsername());
        Assert.assertEquals(usernameB, udB.getUsername());
        Assert.assertEquals(usernameC, udC.getUsername());

        Assert.assertEquals(emailA, udA.getEmail());
        Assert.assertEquals(emailB, udB.getEmail());
        Assert.assertEquals(emailC, udC.getEmail());

        Assert.assertEquals(ipLimitsA, udA.getIpLimits());
        Assert.assertEquals(ipLimitsB, udB.getIpLimits());
        Assert.assertEquals(ipLimitsC, udC.getIpLimits());

        List<GrantedAuthority> gA = new ArrayList<>(udA.getAuthorities());
        List<GrantedAuthority> gB = new ArrayList<>(udB.getAuthorities());
        List<GrantedAuthority> gC = new ArrayList<>(udC.getAuthorities());

        Assert.assertNotNull(gA);
        Assert.assertNotNull(gB);
        Assert.assertNotNull(gC);

        Assert.assertEquals(grantsA.size(), gA.size());
        Assert.assertEquals(grantsB.size(), gB.size());
        Assert.assertEquals(grantsC.size(), gC.size());

        for (GrantedAuthority auth : gA) {
            Assert.assertTrue(grantsA.contains(auth.getAuthority()));
        }
        for (GrantedAuthority auth : gB) {
            Assert.assertTrue(grantsB.contains(auth.getAuthority()));
        }
        for (GrantedAuthority auth : gC) {
            Assert.assertTrue(grantsC.contains(auth.getAuthority()));
        }

        List<String> grpA = udA.getGroups();
        List<String> grpB = udB.getGroups();
        List<String> grpC = udC.getGroups();

        Assert.assertNotNull(grpA);
        Assert.assertNotNull(grpB);
        Assert.assertNotNull(grpC);

        Assert.assertEquals(groupsA.size(), grpA.size());
        Assert.assertEquals(groupsB.size(), grpB.size());
        Assert.assertEquals(groupsC.size(), grpC.size());

        for (String grp : grpA) {
            Assert.assertTrue(groupsA.contains(grp));
        }
        for (String grp : grpB) {
            Assert.assertTrue(groupsB.contains(grp));
        }
        for (String grp : grpC) {
            Assert.assertTrue(groupsC.contains(grp));
        }

        boolean thrown = false;
        try {
            userDetailsService.loadUserByUsername("junk");
            Assert.fail("exception expected");
        } catch (UsernameNotFoundException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void testGetUsers() {
        // Try in un-initialized state.
        List<SecurityUserBean> beans = userDetailsService.getUsers();
        Assert.assertNotNull(beans);
        Assert.assertEquals(0, beans.size());

        // Now try in a useable state.
        userDetailsService.setUsers(users);
        beans = userDetailsService.getUsers();
        Assert.assertNotNull(beans);
        Assert.assertEquals(3, beans.size());

        List<String> foundUsers = new ArrayList<String>();
        for (SecurityUserBean bean : beans) {
            foundUsers.add(bean.getUsername());
        }
        Assert.assertTrue(foundUsers.contains(usernameA));
        Assert.assertTrue(foundUsers.contains(usernameB));
        Assert.assertTrue(foundUsers.contains(usernameC));
    }

    @Test
    public void testFailures() {
        Credential cred = new RootUserCredential();
        verifyFailure(cred);

        cred = new SystemUserCredential();
        verifyFailure(cred);
    }

    private void verifyFailure(Credential cred) {
        SecurityUserBean user = new SecurityUserBean(cred.getUsername(),
                                                     cred.getPassword(),
                                                     "email",
                                                     "",
                                                     true,
                                                     true,
                                                     true,
                                                     true,
                                                     grantsA,
                                                     groupsA);

        users = new ArrayList<SecurityUserBean>();
        users.add(user);

        boolean thrown = false;
        try {
            userDetailsService.setUsers(users);
            Assert.fail("exception expected");
        } catch (Exception e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

}
