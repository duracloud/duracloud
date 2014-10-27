/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Erik Paulsson
 *         Date: 7/10/13
 */
public enum Role {
    ROLE_INIT("Init"),
    ROLE_ROOT("Root"),
    ROLE_OWNER("Owner"),
    ROLE_ADMIN("Administrator"),
    ROLE_USER("User"),
    ROLE_ANONYMOUS("Anonymous");

    private GrantedAuthority authority;
    private String displayName;

    Role(String displayName) {
        this.authority = new GrantedAuthorityImpl(name());
        this.displayName = displayName;
    }

    public GrantedAuthority authority() {
        return this.authority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<Role> getRoleHierarchy() {
        Set<Role> hierarchy = new HashSet<Role>();
        switch (this) {
            case ROLE_ROOT:
                hierarchy.add(ROLE_ROOT);
            case ROLE_OWNER:
                hierarchy.add(ROLE_OWNER);
            case ROLE_ADMIN:
                hierarchy.add(ROLE_ADMIN);
            case ROLE_USER:
                hierarchy.add(ROLE_USER);
            case ROLE_ANONYMOUS:
                hierarchy.add(ROLE_ANONYMOUS);
                break;
            case ROLE_INIT:
                // not in hierarchy
                hierarchy.add(ROLE_INIT);
        }

        return hierarchy;
    }

    public static Role highestRole(Set<Role> roles) {
        Role highest = null;
        if (roles.contains(Role.ROLE_ROOT)) {
            return Role.ROLE_ROOT;
        } else if (roles.contains(Role.ROLE_OWNER)) {
            return Role.ROLE_OWNER;
        } else if (roles.contains(Role.ROLE_ADMIN)) {
            return Role.ROLE_ADMIN;
        } else if (roles.contains(Role.ROLE_USER)) {
            return Role.ROLE_USER;
        } else if (roles.contains(Role.ROLE_ANONYMOUS)) {
            return Role.ROLE_ANONYMOUS;
        }
        return highest;
    }
}
