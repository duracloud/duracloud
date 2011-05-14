/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

/**
 * @author Andrew Woods
 *         Date: 5/9/11
 */
public interface StorageAccount {

    /**
     * This enum holds names of attributes that are not in the common
     * getters/setters of this interface.
     */
    public enum PROPS {
        STORAGE_CLASS;
    }

    public String getId();
    public void setId(String id);

    public String getOwnerId();
    public void setOwnerId(String ownerId);

    public String getUsername();
    public void setUsername(String username);

    public String getPassword();
    public void setPassword(String password);

    public StorageProviderType getType();
    public void setType(StorageProviderType type);

    public boolean isPrimary();
    public void setPrimary(boolean primary);

    /**
     * These methods require that the arg key come from PROPS.X.name()
     *
     * @param key of property
     */
    public String getProperty(String key);
    public void setProperty(String key, String value);
}
