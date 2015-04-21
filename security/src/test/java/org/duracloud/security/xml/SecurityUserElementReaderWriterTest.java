/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.xml;

import org.duracloud.security.domain.SecurityUserBean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
public class SecurityUserElementReaderWriterTest {

    private static final int NUM_ENTRIES = 5;

    private InputStream stream;

    private final String usernamePrefix = "username-";
    private final String passwordPrefix = "password-";
    private final String emailPrefix = "email-";
    private final String ipLimitsPrefix = "ipLimits-";
    private final String grantPrefix = "ROLE-";
    private final String groupPrefix = "group.";

    @After
    public void tearDown() throws IOException {
        if (stream != null) {
            stream.close();
        }
        stream = null;
    }

    @Test
    public void testReadWrite() {
        boolean fullBeans = true;
        List<SecurityUserBean> users = createUsers(fullBeans);
        doTest(users, fullBeans);
    }

    private void doTest(List<SecurityUserBean> users, boolean fullBeans) {
        String xml = SecurityUsersDocumentBinding.createDocumentFrom(users);
        Assert.assertNotNull(xml);

        stream = new ByteArrayInputStream(xml.getBytes());
        List<SecurityUserBean> beans = SecurityUsersDocumentBinding.createSecurityUsersFrom(
            stream);
        Assert.assertNotNull(beans);

        verifyUsers(beans, fullBeans);
    }

    private List<SecurityUserBean> createUsers(boolean fullBeans) {
        List<SecurityUserBean> users = new ArrayList<SecurityUserBean>();

        SecurityUserBean user;
        List<String> grantedAuthorties;
        List<String> groups;
        for (int i = 0; i < NUM_ENTRIES; ++i) {
            boolean flag = (i % 2 == 0);

            grantedAuthorties = new ArrayList<String>();
            for (int j = 0; j < i + 1; ++j) {
                grantedAuthorties.add(grantPrefix + j);
            }

            groups = new ArrayList<String>();
            for (int j = 0; j < i + 1; ++j) {
                groups.add(groupPrefix + j);
            }

            if (fullBeans) {
                user = new SecurityUserBean(usernamePrefix + i,
                                            passwordPrefix + i,
                                            emailPrefix + i,
                                            ipLimitsPrefix + i,
                                            flag,
                                            flag,
                                            flag,
                                            flag,
                                            grantedAuthorties,
                                            groups);
            } else {
                user = new SecurityUserBean(usernamePrefix + i,
                                            passwordPrefix + i,
                                            grantedAuthorties);
            }
            users.add(user);
        }
        return users;
    }

    private void verifyUsers(List<SecurityUserBean> b, boolean fullBeans) {
        Assert.assertNotNull(b);
        Assert.assertEquals(NUM_ENTRIES, b.size());

        for (SecurityUserBean user : b) {
            int index = getIndex(user);

            String username = user.getUsername();
            String password = user.getPassword();
            String email = user.getEmail();
            String ipLimits = user.getIpLimits();
            boolean enabled = user.isEnabled();
            boolean credentialNonExpired = user.isCredentialsNonExpired();
            boolean accountNonExpired = user.isAccountNonExpired();
            boolean accountNonLocked = user.isAccountNonLocked();

            Assert.assertNotNull(username);
            Assert.assertNotNull(password);
            Assert.assertNotNull(email);

            Assert.assertEquals(usernamePrefix + index, username);
            Assert.assertEquals(passwordPrefix + index, password);
            Assert.assertEquals(emailPrefix + index, email);
            Assert.assertEquals(ipLimitsPrefix + index, ipLimits);

            if (fullBeans) {
                Assert.assertEquals(index % 2 == 0, enabled);
            } else {
                Assert.assertEquals(true, enabled);
            }
            boolean same = (enabled == credentialNonExpired) &&
                (enabled == accountNonExpired) && (enabled == accountNonLocked);
            Assert.assertTrue(same);

            List<String> grants = user.getGrantedAuthorities();
            Assert.assertNotNull(grants);
            Assert.assertEquals(index, grants.size() - 1);
            for (int i = 0; i < index; ++i) {
                Assert.assertTrue(grants.contains(grantPrefix + i));
            }

            List<String> groups = user.getGroups();
            Assert.assertNotNull(groups);
            Assert.assertEquals(index, groups.size() - 1);
            for (int i = 0; i < index; ++i) {
                Assert.assertTrue(groups.contains(groupPrefix + i));
            }
        }

    }

    private int getIndex(SecurityUserBean user) {
        String username = user.getUsername();
        Assert.assertNotNull(username);
        return Integer.parseInt(username.substring(usernamePrefix.length()));
    }

}