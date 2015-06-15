/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.xml;

import org.duracloud.SecurityUserType;
import org.duracloud.SecurityUsersDocument;
import org.duracloud.SecurityUsersType;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.security.domain.SecurityUserBean;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for binding SecurityUsers xml documents to
 * SecurityUserBean lists.
 *
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
public class SecurityUserElementReader {

    /**
     * This method binds a SecurityUsers xml document to a SecurityUsers list.
     *
     * @param doc SecurityUsers xml document
     * @return SecurityUserBean list
     */
    public static List<SecurityUserBean> createSecurityUsersFrom(
        SecurityUsersDocument doc) {
        List<SecurityUserBean> beans = new ArrayList<SecurityUserBean>();

        SecurityUsersType usersType = doc.getSecurityUsers();
        if (usersType.sizeOfSecurityUserArray() > 0) {
            checkSchemaVersion(usersType.getSchemaVersion());

            for (SecurityUserType userType : usersType.getSecurityUserArray()) {
                beans.add(createSecurityUser(userType));
            }
        }

        return beans;
    }

    private static SecurityUserBean createSecurityUser(SecurityUserType userType) {
        String username = userType.getUsername();
        String password = userType.getPassword();
        String email = userType.getEmail();
        String ipLimits = userType.getIpLimits();
        boolean enabled = userType.getEnabled();
        boolean credentialsNonExpired = userType.getCredentialsNonExpired();
        boolean accountNonExpired = userType.getAccountNonExpired();
        boolean accountNonLocked = userType.getAccountNonLocked();
        List<String> authorities = userType.getGrantedAuthorities();
        List<String> groups = userType.getGroups();

        return new SecurityUserBean(username,
                                    password,
                                    email,
                                    ipLimits,
                                    enabled,
                                    credentialsNonExpired,
                                    accountNonExpired,
                                    accountNonLocked,
                                    authorities,
                                    groups);
    }

    private static void checkSchemaVersion(String schemaVersion) {
        if (!schemaVersion.equals(SecurityUserBean.SCHEMA_VERSION)) {
            throw new DuraCloudRuntimeException(
                "Unsupported schema version: " + schemaVersion);
        }
    }

}