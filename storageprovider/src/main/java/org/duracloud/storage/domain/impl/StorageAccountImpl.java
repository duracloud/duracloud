/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain.impl;

import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;

import java.util.Iterator;
import java.util.Properties;

/**
 * Contains the information necessary to access a storage
 * provider account.
 *
 * @author Bill Branan
 */
public class StorageAccountImpl implements StorageAccount {

    private String id = null;
    private String ownerId = "0";
    private String username = null;
    private String password = null;
    private StorageProviderType type = null;
    private boolean isPrimary;
    private Properties properties = new Properties();

    public StorageAccountImpl(String id,
                              String username,
                              String password,
                              StorageProviderType type) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.type = type;
    }

    /**
     * @return the id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * @return the username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the type
     */
    @Override
    public StorageProviderType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    @Override
    public void setType(StorageProviderType type) {
        this.type = type;
    }

    @Override
    public boolean isPrimary() {
        return isPrimary;
    }

    @Override
    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    @Override
    public String getProperty(String key) {
        // only allow properties defined in PROPS enum.
        StorageAccount.PROPS.valueOf(key);
        return properties.getProperty(key);
    }

    @Override
    public void setProperty(String key, String value) {
        // only allow properties defined in PROPS enum.
        StorageAccount.PROPS.valueOf(key);
        properties.setProperty(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StorageAccountImpl)) {
            return false;
        }

        StorageAccountImpl that = (StorageAccountImpl) o;

        if (isPrimary != that.isPrimary) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (ownerId != null ? !ownerId.equals(that.ownerId) :
            that.ownerId != null) {
            return false;
        }
        if (password != null ? !password.equals(that.password) :
            that.password != null) {
            return false;
        }
        if (properties != null ? !properties.equals(that.properties) :
            that.properties != null) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (username != null ? !username.equals(that.username) :
            that.username != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (ownerId != null ? ownerId.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (isPrimary ? 1 : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("StorageAccount");
        sb.append("[");
        sb.append("id:" + id);
        sb.append(", ownerId:" + ownerId);
        sb.append(", username:" + username);
        sb.append(", password:" + password);
        sb.append(", type:" + type);
        sb.append(", isPrimary:" + isPrimary);
        sb.append(", props:"+properties);
        sb.append("]");
        return sb.toString();
    }
}
