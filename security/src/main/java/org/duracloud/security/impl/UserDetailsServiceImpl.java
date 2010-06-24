/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import org.duracloud.common.model.Credential;
import org.duracloud.common.model.RootUserCredential;
import org.duracloud.common.model.SystemUserCredential;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class acts as the repository of username/password/role info for access
 * to this DuraCloud application.
 * The actual info-content is stored in a flat file within DuraCloud itself.
 *
 * @author Andrew Woods
 *         Date: Mar 11, 2010
 */
public class UserDetailsServiceImpl implements DuracloudUserDetailsService {
    private final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private Map<String, User> usersTable = new HashMap<String, User>();

    private static final Credential systemUser = new SystemUserCredential();
    private static final Credential rootUser = new RootUserCredential();

    public UserDetailsServiceImpl() {
        initializeUsers();
    }

    private void initializeUsers() {
        usersTable.clear();

        // Add system user.
        List<String> grants = new ArrayList<String>();
        grants.add("ROLE_ADMIN");
        grants.add("ROLE_USER");
        SecurityUserBean system = new SecurityUserBean(systemUser.getUsername(),
                                                       systemUser.getPassword(),
                                                       grants);

        // Add root user
        grants = new ArrayList<String>();
        grants.add("ROLE_ROOT");
        grants.add("ROLE_ADMIN");
        grants.add("ROLE_USER");
        SecurityUserBean root = new SecurityUserBean(rootUser.getUsername(),
                                                     rootUser.getPassword(),
                                                     grants);
        addUser(system);
        addUser(root);
    }

    /**
     * This method retrieves UserDetails for all users from a flat file in
     * DuraCloud.
     *
     * @param username of principal for whom details are sought
     * @return UserDetails for arg username
     * @throws UsernameNotFoundException if username not found
     * @throws DataAccessException       if system error while retrieving info
     */
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException, DataAccessException {
        UserDetails userDetails = usersTable.get(username);
        if (null == userDetails) {
            throw new UsernameNotFoundException(username);
        }
        return userDetails;
    }

    /**
     * This method re-initializes the existing usersTable, then adds the arg
     * users.
     *
     * @param users to populate into the usersTable
     */
    public void setUsers(List<SecurityUserBean> users) {
        initializeUsers();
        for (SecurityUserBean u : users) {
            addUser(u);
        }
    }

    private void addUser(SecurityUserBean u) {
        List<String> grantBeans = u.getGrantedAuthorities();
        GrantedAuthority[] grants = new GrantedAuthority[grantBeans.size()];
        for (int i = 0; i < grantBeans.size(); ++i) {
            grants[i] = new GrantedAuthorityImpl(grantBeans.get(i));
        }

        User user = new User(u.getUsername(),
                             u.getPassword(),
                             u.isEnabled(),
                             u.isAccountNonExpired(),
                             u.isCredentialsNonExpired(),
                             u.isAccountNonLocked(),
                             grants);

        usersTable.put(u.getUsername(), user);
    }

    /**
     * This method returns all of the non-system-defined users.
     *
     * @return
     */
    public List<SecurityUserBean> getUsers() {
        List<SecurityUserBean> users = new ArrayList<SecurityUserBean>();
        for (User user : this.usersTable.values()) {
            List<String> grants = getGrants(user.getAuthorities());
            SecurityUserBean bean = new SecurityUserBean(user.getUsername(),
                                                         user.getPassword(),
                                                         user.isEnabled(),
                                                         user.isAccountNonExpired(),
                                                         user.isCredentialsNonExpired(),
                                                         user.isAccountNonLocked(),
                                                         grants);
            if (isCustomUser(bean)) {
                users.add(bean);
            }
        }
        return users;
    }

    private boolean isCustomUser(SecurityUserBean user) {
        String username = user.getUsername();
        String rootname = rootUser.getUsername();
        String sysname = systemUser.getUsername();
        return !username.equals(rootname) && !username.equals(sysname);
    }

    private List<String> getGrants(GrantedAuthority[] gAuths) {
        List<String> grants = new ArrayList<String>();
        if (gAuths != null && gAuths.length > 0) {
            for (GrantedAuthority gAuth : gAuths) {
                grants.add(gAuth.getAuthority());
            }
        }
        return grants;
    }

}
