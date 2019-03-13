/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

import java.util.Map;

/**
 * @author Andrew Woods
 * Date: 5/9/11
 */
public interface StorageAccount {

    /**
     * This enum holds names of attributes that are not in the common
     * getters/setters of this interface.
     */
    public enum OPTS {
        // S3 below
        CF_ACCOUNT_ID,
        CF_KEY_ID,
        CF_KEY_PATH,
        AWS_REGION,
        // Swift
        SWIFT_S3_ENDPOINT,
        SWIFT_S3_SIGNER_TYPE,
        // iRODS below
        ZONE,
        PORT,
        HOST,
        BASE_DIRECTORY,
        RESOURCE,
        TEMP_PATH,
        // Snapshot below
        SNAPSHOT_USER,
        BRIDGE_HOST,
        BRIDGE_PORT,
        BRIDGE_USER,
        BRIDGE_PASS,
        BRIDGE_MEMBER_ID,
        //GENERAL
        WRITABLE(false);

        private boolean hidden;

        OPTS() {
            this(true);
        }

        OPTS(final boolean hidden) {
            this.hidden = hidden;
        }

        /**
         * Returns true if this option should not be exposed in serializations
         * unless explicitly requested. See
         * {@link org.duracloud.storage.xml.StorageAccountProviderBinding#getElementFrom(StorageAccount, boolean, boolean)}
         *
         * @return
         */
        public boolean isHidden() {
            return this.hidden;
        }
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

    public Map<String, String> getOptions();

    public void setOption(String key, String value);
}
