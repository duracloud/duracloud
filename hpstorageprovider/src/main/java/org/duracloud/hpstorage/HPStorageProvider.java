/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.hpstorage;

import org.duracloud.openstackstorage.OpenStackStorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides content storage backed by HP's Cloud Services offering.
 *
 * @author Bill Branan
 *         Jan 30, 2012
 */
public class HPStorageProvider extends OpenStackStorageProvider {

    private final Logger log =
        LoggerFactory.getLogger(HPStorageProvider.class);

    private static String authUrl =
        "https://region-a.geo-1.identity.hpcloudsvc.com:35357/auth/v1.0/";

    public HPStorageProvider(String username, String apiAccessKey) {
        super(username, apiAccessKey, authUrl);
        log.debug("constructed HPStorageProvider: {}, {}", username, authUrl);
    }

    @Override
    public String getAuthUrl() {
        return authUrl;
    }

    @Override
    public String getProviderName() {
        return "HP";
    }

}
