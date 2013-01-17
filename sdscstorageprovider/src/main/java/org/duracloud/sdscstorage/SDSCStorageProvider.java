/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sdscstorage;

import com.rackspacecloud.client.cloudfiles.FilesClient;
import org.duracloud.openstackstorage.OpenStackStorageProvider;
import org.jclouds.openstack.swift.SwiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides content storage backed by SDSC's Cloud Storage service.
 *
 * @author Andrew Woods
 *         Oct 04, 2011
 */
public class SDSCStorageProvider extends OpenStackStorageProvider {

    private final Logger log =
        LoggerFactory.getLogger(SDSCStorageProvider.class);

    private static String authUrl = "https://duracloud.auth.cloud.sdsc.edu/auth/v1.0";

    public SDSCStorageProvider(String username, String apiAccessKey) {
        super(username, apiAccessKey, authUrl);
        log.debug("constructed SDSCStorageProvider: {}, {}", username, authUrl);
    }

    public SDSCStorageProvider(FilesClient filesClient,
                               SwiftClient swiftClient) {
        super(filesClient, swiftClient);
    }

    @Override
    public String getAuthUrl() {
        return authUrl;
    }

    @Override
    public String getProviderName() {
        return "SDSC";
    }

}
