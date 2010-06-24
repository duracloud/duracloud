/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.model;

import java.io.Serializable;

public class Credential
        implements Serializable {

    private static final long serialVersionUID = -7069231739026478165L;

    private int id;

    private String username;

    private String password;

    private Integer isEnabled;

    public boolean hasId() {
        return id > 0;
    }

    public Credential() {
        id = -1;
    }

    public Credential(String username, String password) {
        id = -1;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean equals(Object other) {
        boolean usernameSame = false;
        boolean passwordSame = false;

        if (other == null || !(other instanceof Credential)) {
            return false;
        }
        Credential otherCred = (Credential) other;

        if ((this.username != null && this.username.equals(otherCred
                .getUsername()))
                || this.username == null && otherCred.getUsername() == null) {
            usernameSame = true;
        }

        if ((this.password != null && this.password.equals(otherCred
                .getPassword()))
                || this.password == null && otherCred.getPassword() == null) {
            passwordSame = true;
        }

        return usernameSame && passwordSame;
    }

    @Override
    public int hashCode() {
        return username.hashCode() + password.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Credential [");
        sb.append(username);
        sb.append(":");
        sb.append(password == null ? null : password.replaceAll(".", "*"));
        sb.append("]");
        return sb.toString();
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Default value is '1'
     */
    public Integer getIsEnabled() {
        if (isEnabled == null) {
            isEnabled = 1;
        }
        return isEnabled;
    }

    public void setEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

}
