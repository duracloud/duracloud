/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author: Bill Branan
 * Date: 4/18/11
 */
public class SecurityUserBeanTest {

    @Test
    public void testGetTopAuthorityDisplay() {
        String user = "username";
        String pass = "password";
        String roleOwner = "ROLE_OWNER";
        String roleAdmin = "ROLE_ADMIN";
        String roleUser = "ROLE_USER";
        List<String> authorities = new ArrayList<String>();

        SecurityUserBean bean = new SecurityUserBean(user, pass, authorities);
        assertEquals("User", bean.getTopAuthorityDisplay());

        authorities.add(roleUser);
        bean = new SecurityUserBean(user, pass, authorities);
        assertEquals("User", bean.getTopAuthorityDisplay());

        authorities.add(roleAdmin);
        bean = new SecurityUserBean(user, pass, authorities);
        assertEquals("Administrator", bean.getTopAuthorityDisplay());

        authorities.add(roleOwner);
        bean = new SecurityUserBean(user, pass, authorities);
        assertEquals("Owner", bean.getTopAuthorityDisplay());
    }

}
