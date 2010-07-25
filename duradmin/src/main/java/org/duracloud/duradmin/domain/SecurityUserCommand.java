/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.domain;

import org.duracloud.security.domain.SecurityUserBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Apr 23, 2010
 */
public class SecurityUserCommand {
    private List<SecurityUserBean> users = new ArrayList<SecurityUserBean>();
    private String username = "";
    private String password = "";
    private String verb = "none"; // add or delete or modify

    public SecurityUserCommand() {
    }

    public SecurityUserCommand(List<SecurityUserBean> users) {
        this.users = users;
    }

    public void addUser(SecurityUserBean user) {
        users.add(user);
    }

    public void removeUser(String username) {
        if (username != null) {
        	List<SecurityUserBean> readOnlyUserList = new ArrayList<SecurityUserBean>(users);
            for (SecurityUserBean user : readOnlyUserList) {
                if (username.equalsIgnoreCase(user.getUsername())) {
                    users.remove(user);
                }
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<SecurityUserBean> getUsers() {
        return users;
    }

    public void setUsers(List<SecurityUserBean> users) {
        this.users = users;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }
}
