/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.duracloud.security.domain.SecurityUserBean;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Apr 21, 2010
 */
public class SecurityConfigTest {

    private static final int NUM_USERS = 3;
    private String[] usernames = {"username0", "username1", "username2"};
    private String[] passwords = {"password0", "password1", "password2"};
    private String[] ipLimits = {"1.2.3.4/0", "127.0.0.1/32", ""};
    private String[] enableds = {"true", "false", "true"};
    private String[] acctNonExpireds = {"false", "true", "false"};
    private String[] credNonExpireds = {"false", "false", "true"};
    private String[] acctNonLockeds = {"true", "true", "false"};
    private String[][] grants = {{"grant0a", "grant0b", "grant0c"},
                                 {"grant1a", "grant1b"},
                                 {"grant2a"}};
    private String[][] groups = {{"group0a", "group0b", "group0c"},
                                 {"group1a", "group1b"},
                                 {"group2a"}};

    @Test
    public void testLoad() {
        SecurityConfig config = new SecurityConfig();
        config.load(createProps());
        verifySecurityConfig(config);
    }

    private Map<String, String> createProps() {
        Map<String, String> props = new HashMap<String, String>();
        String dot = ".";
        String prefix =
            SecurityConfig.QUALIFIER + dot + SecurityConfig.userKey + dot;

        for (int i = 0; i < NUM_USERS; ++i) {
            String p = prefix + i + dot;
            props.put(p + SecurityConfig.usernameKey, usernames[i]);
            props.put(p + SecurityConfig.passwordKey, passwords[i]);
            props.put(p + SecurityConfig.ipLimitsKey, ipLimits[i]);
            props.put(p + SecurityConfig.enabledKey, enableds[i]);
            props.put(p + SecurityConfig.acctNonExpiredKey, acctNonExpireds[i]);
            props.put(p + SecurityConfig.credNonExpiredKey, credNonExpireds[i]);
            props.put(p + SecurityConfig.acctNonLockedKey, acctNonLockeds[i]);

            int x = 0;
            for (String grant : grants[i]) {
                props.put(p + SecurityConfig.grantsKey + dot + x++, grant);
            }

            int y = 0;
            for (String group : groups[i]) {
                props.put(p + SecurityConfig.groupsKey + dot + y++, group);
            }
        }
        return props;
    }

    private void verifySecurityConfig(SecurityConfig config) {
        Collection<SecurityUserBean> users = config.getUsers();
        Assert.assertNotNull(users);
        Assert.assertEquals(NUM_USERS, users.size());

        boolean[] verified = {false, false, false};
        for (SecurityUserBean user : users) {
            for (int i = 0; i < NUM_USERS; ++i) {
                if (usernames[i].equals(user.getUsername())) {
                    verifyUser(user, i);
                    verified[i] = true;
                }
            }
        }

        for (boolean v : verified) {
            Assert.assertTrue(v);
        }
    }

    private void verifyUser(SecurityUserBean user, int i) {
        Assert.assertNotNull(user);

        String username = user.getUsername();
        String password = user.getPassword();
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);
        Assert.assertEquals(usernames[i], username);
        Assert.assertEquals(passwords[i], password);

        Assert.assertEquals(ipLimits[i], user.getIpLimits());

        List<String> granteds = user.getGrantedAuthorities();
        Assert.assertNotNull(granteds);
        Assert.assertEquals(grants[i].length, granteds.size());
        for (int j = 0; j < grants[i].length; ++j) {
            Assert.assertTrue(granteds.contains(grants[i][j]));
        }

        List<String> grps = user.getGroups();
        Assert.assertNotNull(grps);
        Assert.assertEquals(groups[i].length, grps.size());
        for (int j = 0; j < groups[i].length; ++j) {
            Assert.assertTrue(grps.contains(groups[i][j]));
        }

        Assert.assertEquals(Boolean.valueOf(enableds[i]), user.isEnabled());
        Assert.assertEquals(Boolean.valueOf(acctNonExpireds[i]),
                            user.isAccountNonExpired());
        Assert.assertEquals(Boolean.valueOf(credNonExpireds[i]),
                            user.isCredentialsNonExpired());
        Assert.assertEquals(Boolean.valueOf(acctNonLockeds[i]),
                            user.isAccountNonLocked());
    }

    @Test
    public void testUpdateSecurity() {
//        SecurityConfig config = new SecurityConfig();
//        config.load(createProps());
//        verifySecurityConfig(config);
//
//        List<String> newGrants = new ArrayList<String>();
//        newGrants.add("g0");
//        SecurityUserBean user = new SecurityUserBean("un", "pw", newGrants);
//        List<SecurityUserBean> newUsers = new ArrayList<SecurityUserBean>();
//        newUsers.add(user);
//
//        config.setSecurityUsers(newUsers);
//
//        Collection<SecurityUserBean> testUsers = config.getUsers();
//        Assert.assertNotNull(testUsers);
//        Assert.assertEquals(newUsers.size(), testUsers.size());
//        for (SecurityUserBean testUser : testUsers) {
//            Assert.assertNotNull(testUser);
//            Assert.assertEquals("un", testUser.getUsername());
//            Assert.assertEquals("pw", testUser.getPassword());
//            Assert.assertTrue(testUser.isAccountNonExpired());
//            Assert.assertTrue(testUser.isAccountNonLocked());
//            Assert.assertTrue(testUser.isCredentialsNonExpired());
//            Assert.assertTrue(testUser.isEnabled());
//
//            List<String> testGrants = testUser.getGrantedAuthorities();
//            Assert.assertNotNull(testGrants);
//            Assert.assertEquals(newGrants.size(), testGrants.size());
//            Assert.assertEquals(1, testGrants.size());
//            Assert.assertEquals(newGrants.get(0), testGrants.get(0));
//        }
    }

}
