/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.rackspacestorage;

import org.duracloud.openstackstorage.OpenStackStorageProvider;
import org.duracloud.storage.domain.StorageProviderType;
import org.jclouds.openstack.swift.SwiftClient;

/**
 * Provides content storage backed by Rackspace's Cloud Files service.
 *
 * @author Bill Branan
 */
public class RackspaceStorageProvider extends OpenStackStorageProvider {

    private static final String authUrl= "https://auth.api.rackspacecloud.com/v1.0";

    public RackspaceStorageProvider(String username,
                                    String apiAccessKey,
                                    String authUrl) {
        super(username, apiAccessKey, authUrl);
    }

    public RackspaceStorageProvider(String username, String apiAccessKey) {
        super(username, apiAccessKey, null);
    }

    public RackspaceStorageProvider(SwiftClient swiftClient) {
        super(swiftClient);
    }

    @Override
    public StorageProviderType getStorageProviderType() {
        return StorageProviderType.RACKSPACE;
    }

    @Override
    public String getAuthUrl() {
        return authUrl;
    }

    @Override
    public String getProviderName() {
        return "Rackspace";
    }

}
