/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.storage.domain.AuditConfig;
import org.duracloud.storage.domain.DuraStoreInitConfig;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.duracloud.storage.xml.DuraStoreInitDocumentBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds the configuration elements for durastore.
 *
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public class DurastoreConfig extends BaseConfig implements AppConfig {
    private final Logger log = LoggerFactory.getLogger(DurastoreConfig.class);

    public static final String QUALIFIER = "durastore";

    // Audit
    protected static final String auditKey = "audit";
    protected static final String queueKey = "queue";

    // Storage
    protected static final String storageAccountKey = "storage-acct";
    protected static final String ownerIdKey = "owner-id";
    protected static final String isPrimaryKey = "is-primary";
    protected static final String idKey = "id";
    protected static final String providerTypeKey = "provider-type";
    protected static final String usernameKey = "username";
    protected static final String passwordKey = "password";
    // S3
    protected static final String storageClassKey = "storage-class";
    // IRODS
    protected static final String zoneKey = "zone";
    protected static final String portKey = "port";
    protected static final String hostKey = "host";
    protected static final String baseDirectoryKey = "base-directory";
    protected static final String resourceKey = "resource";
    protected static final String tempPathKey = "temp-path";
    // Chronopolis
    protected static final String bridgeHostKey = "bridge-host";
    protected static final String bridgePortKey = "bridge-port";
    protected static final String bridgeUserKey = "bridge-user";
    protected static final String bridgePassKey = "bridge-pass";

    private AuditConfig auditConfig = new AuditConfig();

    private Map<String, StorageAccount> storageAccounts =
        new HashMap<String, StorageAccount>();

    private DuraStoreInitDocumentBinding documentBinding =
        new DuraStoreInitDocumentBinding();

    protected String getQualifier() {
        return QUALIFIER;
    }

    protected void loadProperty(String key, String value) {
        key = key.toLowerCase();
        if (key.startsWith(storageAccountKey)) {
            String suffix = getSuffix(key);
            loadStorageAcct(suffix, value);
        } else if(key.startsWith(auditKey)) {
            String suffix = getSuffix(key);
            loadAudit(suffix, value);
        } else {
            String msg = "unknown key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    private void loadAudit(String key, String value) {
        String suffix = getSuffix(key);
        if (suffix.equalsIgnoreCase(usernameKey)) {
            auditConfig.setAuditUsername(value);
        } else if (suffix.equalsIgnoreCase(passwordKey)) {
            auditConfig.setAuditPassword(value);
        } else if (suffix.equalsIgnoreCase(queueKey)) {
            auditConfig.setAuditQueueName(value);
        }
    }

    private void loadStorageAcct(String key, String value) {
        String id = getPrefix(key);
        StorageAccount acct = storageAccounts.get(id);
        if (null == acct) {
            acct = new StorageAccountImpl(null, null, null, null);
        }

        String suffix = getSuffix(key);
        if (suffix.equalsIgnoreCase(idKey)) {
            acct.setId(value);

        } else if (suffix.equalsIgnoreCase(ownerIdKey)) {
            acct.setOwnerId(value);

        } else if (suffix.equalsIgnoreCase(isPrimaryKey)) {
            acct.setPrimary(Boolean.valueOf(value));

        } else if (suffix.equalsIgnoreCase(providerTypeKey)) {
            acct.setType(StorageProviderType.fromString(value));

        } else if (suffix.equalsIgnoreCase(usernameKey)) {
            acct.setUsername(value);

        } else if (suffix.equalsIgnoreCase(passwordKey)) {
            acct.setPassword(value);

        } else if (suffix.equalsIgnoreCase(storageClassKey)) {
            acct.setOption(StorageAccount.OPTS.STORAGE_CLASS.name(), value);

        } else if (suffix.equalsIgnoreCase(zoneKey)) {
            acct.setOption(StorageAccount.OPTS.ZONE.name(), value);

        } else if (suffix.equalsIgnoreCase(hostKey)) {
            acct.setOption(StorageAccount.OPTS.HOST.name(), value);

        } else if (suffix.equalsIgnoreCase(portKey)) {
            acct.setOption(StorageAccount.OPTS.PORT.name(), value);

        } else if (suffix.equalsIgnoreCase(baseDirectoryKey)) {
            acct.setOption(StorageAccount.OPTS.BASE_DIRECTORY.name(), value);

        } else if (suffix.equalsIgnoreCase(resourceKey)) {
            acct.setOption(StorageAccount.OPTS.RESOURCE.name(), value);

        } else if (suffix.equalsIgnoreCase(tempPathKey)) {
            acct.setOption(StorageAccount.OPTS.TEMP_PATH.name(), value);

        } else if (suffix.equalsIgnoreCase(bridgeHostKey)) {
            acct.setOption(StorageAccount.OPTS.BRIDGE_HOST.name(), value);

        } else if (suffix.equalsIgnoreCase(bridgePortKey)) {
            acct.setOption(StorageAccount.OPTS.BRIDGE_PORT.name(), value);

        } else if (suffix.equalsIgnoreCase(bridgeUserKey)) {
            acct.setOption(StorageAccount.OPTS.BRIDGE_USER.name(), value);

        } else if (suffix.equalsIgnoreCase(bridgePassKey)) {
            acct.setOption(StorageAccount.OPTS.BRIDGE_PASS.name(), value);

        } else {
            String msg = "unknown acct key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }

        storageAccounts.put(id, acct);
    }

    public AuditConfig getAuditConfig() {
        return auditConfig;
    }

    public Collection<StorageAccount> getStorageAccounts() {
        return storageAccounts.values();
    }

    public void setStorageAccounts(Set<StorageAccount> storageAccts) {
        this.storageAccounts = new HashMap<String, StorageAccount>();
        for (StorageAccount storageAcct : storageAccts) {
            this.storageAccounts.put(storageAcct.getId(), storageAcct);
        }
    }

    public String asXml() {
        boolean includeCredentials = true;
        DuraStoreInitConfig initConfig = new DuraStoreInitConfig();
        initConfig.setAuditConfig(getAuditConfig());
        List<StorageAccount> accounts = new ArrayList<>(getStorageAccounts());
        initConfig.setStorageAccounts(accounts);
        return documentBinding.createXmlFrom(initConfig, includeCredentials);
    }
    
    public String getInitResource() {
        return INIT_RESOURCE;
    }
}
