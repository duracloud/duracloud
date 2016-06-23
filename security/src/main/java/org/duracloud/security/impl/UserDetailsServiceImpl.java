/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * This class acts as the repository of username/password/role info for access
 * to this DuraCloud application.
 *
 * @author Andrew Woods
 *         Date: Mar 11, 2010
 */
public class UserDetailsServiceImpl implements DuracloudUserDetailsService {
    private final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private Map<String, DuracloudUserDetails> usersTable =
        new HashMap<String, DuracloudUserDetails>();


    public UserDetailsServiceImpl() {
    }


    /**
     * This method retrieves UserDetails for all users from a flat file in
     * DuraCloud.
     *
     * @param username of principal for whom details are sought
     * @return UserDetails for arg username
     * @throws UsernameNotFoundException if username not found
     */
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {
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
        for (SecurityUserBean u : users) {
            addUser(u);
        }
    }

    private void addUser(SecurityUserBean u) {
        List<String> grantBeans = u.getGrantedAuthorities();
        GrantedAuthority[] grants = new GrantedAuthority[grantBeans.size()];
        for (int i = 0; i < grantBeans.size(); ++i) {
            grants[i] = new SimpleGrantedAuthority(grantBeans.get(i));
        }

        DuracloudUserDetails user = new DuracloudUserDetails(u.getUsername(),
                                                             u.getPassword(),
                                                             u.getEmail(),
                                                             u.getIpLimits(),
                                                             u.isEnabled(),
                                                             u.isAccountNonExpired(),
                                                             u.isCredentialsNonExpired(),
                                                             u.isAccountNonLocked(),
                                                             Arrays.asList(grants),
                                                             u.getGroups());

        usersTable.put(u.getUsername(), user);
    }

    /**
     * This method returns all of the non-system-defined users.
     *
     * @return
     */
    public List<SecurityUserBean> getUsers() {
        List<SecurityUserBean> users = new ArrayList<SecurityUserBean>();
        for (DuracloudUserDetails user : this.usersTable.values()) {
            SecurityUserBean bean = createUserBean(user);
            users.add(bean);
        }
        return users;
    }

    @Override
    public SecurityUserBean getUserByUsername(String username) {
        for (DuracloudUserDetails user : this.usersTable.values()) {
            SecurityUserBean bean = createUserBean(user);
            if (bean.getUsername().equals(username)) {
                return bean;
            }
        }
        return null;
    }

    private SecurityUserBean createUserBean(DuracloudUserDetails user) {
        List<String> grants = getGrants(user.getAuthorities().toArray(new GrantedAuthority[0]));
        return new SecurityUserBean(user.getUsername(),
                                    user.getPassword(),
                                    user.getEmail(),
                                    user.getIpLimits(),
                                    user.isEnabled(),
                                    user.isAccountNonExpired(),
                                    user.isCredentialsNonExpired(),
                                    user.isAccountNonLocked(),
                                    grants,
                                    user.getGroups());
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
