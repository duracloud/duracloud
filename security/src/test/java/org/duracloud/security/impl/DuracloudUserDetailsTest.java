/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Andrew Woods
 *         Date: 11/11/11
 */
public class DuracloudUserDetailsTest {

    @Test
    public void testGetGroupsNull() throws Exception {
        List<String> expected = null;
        DuracloudUserDetails userDetails = createDuracloudUserDetails(expected);

        verifyGroups(expected, userDetails.getGroups());
    }

    @Test
    public void testGetGroupsEmpty() throws Exception {
        List<String> expected = new ArrayList<String>();
        DuracloudUserDetails userDetails = createDuracloudUserDetails(expected);

        verifyGroups(expected, userDetails.getGroups());
    }

    @Test
    public void testGetGroups() throws Exception {
        List<String> expected = new ArrayList<String>();
        expected.add("group.0");
        expected.add("group.1");
        expected.add("group.5");
        DuracloudUserDetails userDetails = createDuracloudUserDetails(expected);

        verifyGroups(expected, userDetails.getGroups());
    }

    private void verifyGroups(List<String> expected, List<String> groups) {
        if (null == expected) {
            Assert.assertNull(groups);

        } else {
            Assert.assertEquals(expected.size(), groups.size());
            for (String group : expected) {
                Assert.assertTrue(groups.contains(group));
            }
        }
    }

    private DuracloudUserDetails createDuracloudUserDetails(List<String> groups) {
        return new DuracloudUserDetails("username",
                                        "password",
                                        "email",
                                        true,
                                        true,
                                        true,
                                        true,
                                        new LinkedList<GrantedAuthority>(),
                                        groups);
    }

}
