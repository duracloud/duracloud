/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security;

import org.springframework.security.userdetails.UserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;

import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
public interface DuracloudUserDetailsService extends UserDetailsService {

    public void setUsers(List<SecurityUserBean> users);

    public List<SecurityUserBean> getUsers();
}
