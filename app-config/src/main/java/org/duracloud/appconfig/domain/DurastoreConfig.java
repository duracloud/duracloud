/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.storage.domain.AuditConfig;
import org.duracloud.storage.domain.DatabaseConfig;
import org.duracloud.storage.domain.DuraStoreInitConfig;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.duracloud.storage.xml.DuraStoreInitDocumentBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    protected static final String logSpaceIdKey = "log-space-id";
    // Mill
    protected static final String millDbKey = "mill.db";

    // Storage
    protected static final String storageAccountKey = "storage-acct";
    protected static final String ownerIdKey = "owner-id";
    protected static final String isPrimaryKey = "is-primary";
    protected static final String idKey = "id";
    protected static final String providerTypeKey = "provider-type";
    protected static final String usernameKey = "username";
    protected static final String passwordKey = "password";
    // S3
    protected static final String cfAccountId = "cf-account-id";
    protected static final String cfKeyId = "cf-key-id";
    protected static final String cfKeyPath = "cf-key-path";
    // IRODS
    protected static final String zoneKey = "zone";
    protected static final String portKey = "port";
    protected static final String hostKey = "host";
    protected static final String baseDirectoryKey = "base-directory";
    protected static final String resourceKey = "resource";
    // Snapshot
    protected static final String snapshotUserKey = "snapshot-user";
    protected static final String bridgeHostKey = "bridge-host";
    protected static final String bridgePortKey = "bridge-port";
    protected static final String bridgeUserKey = "bridge-user";
    protected static final String bridgePassKey = "bridge-pass";
    protected static final String bridgeMemberIDKey = "bridge-member-id";

    private DatabaseConfig millDbConfig = new DatabaseConfig();

    private AuditConfig auditConfig = new AuditConfig();

    private Map<String, StorageAccount> storageAccounts = new HashMap<>();

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
        }  else if(key.startsWith(millDbKey)) {
            String suffix = getSuffix(key);
            loadDbConfig(millDbConfig, suffix, value);
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
        } else if (suffix.equalsIgnoreCase(logSpaceIdKey)) {
            auditConfig.setAuditLogSpaceId(value);
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

        } else if (suffix.equalsIgnoreCase(cfAccountId)) {
            acct.setOption(StorageAccount.OPTS.CF_ACCOUNT_ID.name(), value);

        } else if (suffix.equalsIgnoreCase(cfKeyId)) {
            acct.setOption(StorageAccount.OPTS.CF_KEY_ID.name(), value);

        } else if (suffix.equalsIgnoreCase(cfKeyPath)) {
            acct.setOption(StorageAccount.OPTS.CF_KEY_PATH.name(), value);

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

        } else if (suffix.equalsIgnoreCase(snapshotUserKey)) {
            acct.setOption(StorageAccount.OPTS.SNAPSHOT_USER.name(), value);

        } else if (suffix.equalsIgnoreCase(bridgeHostKey)) {
            acct.setOption(StorageAccount.OPTS.BRIDGE_HOST.name(), value);

        } else if (suffix.equalsIgnoreCase(bridgePortKey)) {
            acct.setOption(StorageAccount.OPTS.BRIDGE_PORT.name(), value);

        } else if (suffix.equalsIgnoreCase(bridgeUserKey)) {
            acct.setOption(StorageAccount.OPTS.BRIDGE_USER.name(), value);

        } else if (suffix.equalsIgnoreCase(bridgePassKey)) {
            acct.setOption(StorageAccount.OPTS.BRIDGE_PASS.name(), value);

        }else if (suffix.equalsIgnoreCase(bridgeMemberIDKey)) {
            acct.setOption(StorageAccount.OPTS.BRIDGE_MEMBER_ID.name(), value);
        
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

    public void setAuditConfig(AuditConfig auditConfig) {
        this.auditConfig = auditConfig;
    }

    public DatabaseConfig getMillDbConfig() {
        return this.millDbConfig;
    }

    public void setMillDbConfig(DatabaseConfig millDbConfig) {
        this.millDbConfig = millDbConfig;
    }

    public Collection<StorageAccount> getStorageAccounts() {
        return storageAccounts.values();
    }

    public void setStorageAccounts(Set<StorageAccount> storageAccts) {
        this.storageAccounts = new HashMap<>();
        for (StorageAccount storageAcct : storageAccts) {
            this.storageAccounts.put(storageAcct.getId(), storageAcct);
        }
    }

    public String asXml() {
        boolean includeCredentials = true;
        boolean includeOptions = true;
        DuraStoreInitConfig initConfig = new DuraStoreInitConfig();
        initConfig.setAuditConfig(getAuditConfig());
        initConfig.setMillDbConfig(getMillDbConfig());
        List<StorageAccount> accounts = new ArrayList<>(getStorageAccounts());
        initConfig.setStorageAccounts(accounts);
        return documentBinding.createXmlFrom(initConfig,
                                             includeCredentials,
                                             includeOptions);
    }

    public String getInitResource() {
        return INIT_RESOURCE;
    }
}
