/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.storage.domain;

/**
 * @author Andrew Woods
 *         Date: 5/9/11
 */
public interface StorageAccount {

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
}
