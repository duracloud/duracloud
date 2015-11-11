/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.storage.domain.StorageAccount.OPTS.BASE_DIRECTORY;
import static org.duracloud.storage.domain.StorageAccount.OPTS.SNAPSHOT_USER;
import static org.duracloud.storage.domain.StorageAccount.OPTS.BRIDGE_HOST;
import static org.duracloud.storage.domain.StorageAccount.OPTS.BRIDGE_PASS;
import static org.duracloud.storage.domain.StorageAccount.OPTS.BRIDGE_PORT;
import static org.duracloud.storage.domain.StorageAccount.OPTS.BRIDGE_USER;
import static org.duracloud.storage.domain.StorageAccount.OPTS.BRIDGE_MEMBER_ID;
import static org.duracloud.storage.domain.StorageAccount.OPTS.HOST;
import static org.duracloud.storage.domain.StorageAccount.OPTS.PORT;
import static org.duracloud.storage.domain.StorageAccount.OPTS.RESOURCE;
import static org.duracloud.storage.domain.StorageAccount.OPTS.ZONE;

/**
 * @author Andrew Woods
 *         Date: Apr 21, 2010
 */
public class DurastoreConfigTest {

    private static final int NUM_ACCTS = 4;

    private String[] ownerIds = {"ownerId0", "ownerId1", "ownerId2", "ownerId3"};
    private String[] primaries = {"false", "false", "true", "false"};
    private String[] ids = {"id0", "id1", "id2", "id3"};
    private String[] types = {StorageProviderType.AMAZON_S3.toString(),
                              StorageProviderType.RACKSPACE.toString(),
                              StorageProviderType.SNAPSHOT.toString(),
                              StorageProviderType.IRODS.toString()};
    private String[] usernames = {"username0", "username1", "username2", "username3"};
    private String[] passwords = {"password0", "password1", "password2", "password3"};

    private String zone = "zone";
    private String host = "host";
    private String port = "port";
    private String baseDirectory = "baseDirectory";
    private String resource = "resource";
    private String snapshotUser = "snapshotuser";
    private String bridgeHost = "bridgehost";
    private String bridgePort = "bridgeport";
    private String bridgeUser = "bridgeuser";
    private String bridgePass = "bridgepass";
    private String bridgeMemberId = "bridge-member-id";
    @Test
    public void testLoad() {
        DurastoreConfig config = new DurastoreConfig();
        config.load(createProps());
        verifyDurastoreConfig(config);
    }

    private Map<String, String> createProps() {
        Map<String, String> props = new HashMap<String, String>();

        String dot = ".";
        String prefix = DurastoreConfig.QUALIFIER + dot +
            DurastoreConfig.storageAccountKey + dot;

        for (int i = 0; i < NUM_ACCTS; ++i) {
            String p = prefix + i + dot;
            props.put(p + DurastoreConfig.usernameKey, usernames[i]);
            props.put(p + DurastoreConfig.passwordKey, passwords[i]);
            props.put(p + DurastoreConfig.isPrimaryKey, primaries[i]);
            props.put(p + DurastoreConfig.idKey, ids[i]);
            props.put(p + DurastoreConfig.ownerIdKey, ownerIds[i]);
            props.put(p + DurastoreConfig.providerTypeKey, types[i]);

            if (types[i] == StorageProviderType.AMAZON_S3.toString()) {
                // no special options
            } else if (types[i] == StorageProviderType.IRODS.toString()) {
                props.put(p + DurastoreConfig.zoneKey, zone);
                props.put(p + DurastoreConfig.hostKey, host);
                props.put(p + DurastoreConfig.portKey, port);
                props.put(p + DurastoreConfig.baseDirectoryKey, baseDirectory);
                props.put(p + DurastoreConfig.resourceKey, resource);
            } else if (types[i] == StorageProviderType.SNAPSHOT.toString()) {
                props.put(p + DurastoreConfig.snapshotUserKey, snapshotUser);
                props.put(p + DurastoreConfig.bridgeHostKey, bridgeHost);
                props.put(p + DurastoreConfig.bridgePortKey, bridgePort);
                props.put(p + DurastoreConfig.bridgeUserKey, bridgeUser);
                props.put(p + DurastoreConfig.bridgePassKey, bridgePass);
                props.put(p + DurastoreConfig.bridgeMemberIDKey, bridgeMemberId);

            }
        }
        return props;
    }

    private void verifyDurastoreConfig(DurastoreConfig config) {

        Collection<StorageAccount> accts = config.getStorageAccounts();
        Assert.assertNotNull(accts);
        Assert.assertEquals(NUM_ACCTS, accts.size());

        boolean[] verified = {false, false, false, false};
        for (StorageAccount acct : accts) {
            for (int i = 0; i < NUM_ACCTS; ++i) {
                if (usernames[i].equals(acct.getUsername())) {
                    verifyAcct(acct, i);
                    verified[i] = true;
                }
            }
        }

        for (boolean v : verified) {
            Assert.assertTrue(v);
        }
    }

    private void verifyAcct(StorageAccount acct, int i) {
        Assert.assertNotNull(acct);

        String username = acct.getUsername();
        String password = acct.getPassword();
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);
        Assert.assertEquals(usernames[i], username);
        Assert.assertEquals(passwords[i], password);

        Assert.assertEquals(Boolean.valueOf(primaries[i]), acct.isPrimary());
        Assert.assertEquals(ids[i], acct.getId());
        Assert.assertEquals(ownerIds[i], acct.getOwnerId());

        StorageProviderType type = StorageProviderType.fromString(types[i]);
        Assert.assertEquals(type, acct.getType());

        Map<String, String> options = acct.getOptions();
        Assert.assertNotNull(options);

        if (type == StorageProviderType.AMAZON_S3) {
            // No S3 options

        } else if (type == StorageProviderType.IRODS) {
            Assert.assertEquals(zone, options.get(ZONE.name()));
            Assert.assertEquals(host, options.get(HOST.name()));
            Assert.assertEquals(port, options.get(PORT.name()));
            Assert.assertEquals(baseDirectory,
                                options.get(BASE_DIRECTORY.name()));
            Assert.assertEquals(resource, options.get(RESOURCE.name()));

        } else if (type == StorageProviderType.SNAPSHOT) {
            Assert.assertEquals(snapshotUser, options.get(SNAPSHOT_USER.name()));
            Assert.assertEquals(bridgeHost, options.get(BRIDGE_HOST.name()));
            Assert.assertEquals(bridgePort, options.get(BRIDGE_PORT.name()));
            Assert.assertEquals(bridgeUser, options.get(BRIDGE_USER.name()));
            Assert.assertEquals(bridgePass, options.get(BRIDGE_PASS.name()));
            Assert.assertEquals(bridgeMemberId, options.get(BRIDGE_MEMBER_ID.name()));

        } else {
            Assert.assertEquals(0, options.size());
        }
    }

}
