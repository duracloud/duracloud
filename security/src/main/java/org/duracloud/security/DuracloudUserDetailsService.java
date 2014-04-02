/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security;

import java.util.List;

import org.duracloud.security.domain.SecurityUserBean;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
public interface DuracloudUserDetailsService extends UserDetailsService {

    public void setUsers(List<SecurityUserBean> users);

    public List<SecurityUserBean> getUsers();

    public SecurityUserBean getUserByUsername(String username);

}
