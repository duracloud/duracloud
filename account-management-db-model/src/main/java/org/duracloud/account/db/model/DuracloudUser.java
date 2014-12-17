/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Erik Paulsson
 *         Date: 7/10/13
 */
@Entity
public class DuracloudUser extends BaseEntity implements UserDetails {

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String securityQuestion;
    private String securityAnswer;

    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean credentialsNonExpired = true;
    private boolean accountNonLocked = true;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="user")
    private Set<AccountRights> accountRights;
    private boolean root = false;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public Set<AccountRights> getAccountRights() {
        return accountRights;
    }

    public void setAccountRights(Set<AccountRights> accountRights) {
        this.accountRights = accountRights;
    }

    /**
     * Returns the set of all possible roles a user can play This method is
     * implemented as part of the UserDetails interface (
     * <code>UserDetails</code>).
     *
     * @return
     */
    public Collection<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        for (Role role : Role.ROLE_USER.getRoleHierarchy()) {
            authorities.add(new GrantedAuthorityImpl(role.name()));
        }

        if (accountRights != null) {
            for (AccountRights rights : accountRights) {
                Set<Role> roles = rights.getRoles();
                if (roles != null) {
                    for (Role role : roles) {
                        authorities.add(role.authority());
                    }
                }
            }
        }
        
        if(isRoot()){
            authorities.add(new GrantedAuthorityImpl(Role.ROLE_ROOT.name()));
        }
        return authorities;
    }

    public Set<Role> getRolesByAcct(Long accountId) {
        Set<Role> roles = new HashSet<Role>(0);
        if (accountRights != null) {
            for (AccountRights rights : getAccountRights()) {
                if (rights.getAccount().getId().equals(accountId)) {
                    roles = rights.getRoles();
                }
            }
        }
        return roles;
    }

    public Role getRoleByAcct(Long accountId) {
        if(isRoot()){
            return Role.ROLE_ROOT;
        }
        Set<Role> roles = getRolesByAcct(accountId);
        return Role.highestRole(roles);
    }

    public boolean isOwnerForAcct(Long accountId) {
        return hasRoleForAcct(accountId, Role.ROLE_OWNER);
    }

    public boolean isAdminForAcct(Long accountId) {
        return hasRoleForAcct(accountId, Role.ROLE_ADMIN);
    }

    public boolean hasRoleForAcct(Long accountId, Role role) {
        Set<Role> roles = getRolesByAcct(accountId);
        if (roles != null) {
            return roles.contains(role);
        }

        return false;
    }

    public boolean isRootUser() {
        return root;
    }
    
    public boolean isRoot() {
        return root;
    }
}
