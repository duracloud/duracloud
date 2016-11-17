/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 4/5/12
 */
public class Constants {

    public static final String HEADER_PREFIX = "x-dura-meta-";

    /**
     * An http header sent by the client that indicates the version of the client.
     */
    public static final String CLIENT_VERSION_HEADER = "x-dura-client-version";

    /**
     * Content ID used to define a space snapshot
     */
    public static final String SNAPSHOT_METADATA_SPACE =
        "x-snapshot-metadata";

    /**
     * This structure defines the system managed spaces.
     */
    public static final List<String> SYSTEM_SPACES = Arrays
        .asList("x-duracloud-admin", "x-service-out", "x-service-work", SNAPSHOT_METADATA_SPACE);

    /**
     * Mime types
     */
    public static final String TEXT_TSV = "text/tab-separated-values";

    /**
     * Content ID used to define a space snapshot
     */
    public static final String SNAPSHOT_PROPS_FILENAME =
        ".collection-snapshot.properties";

    /**
     * The property value set on a space to indicate that a snapshot is in
     * process
     */
    public static final String SNAPSHOT_ID_PROP = "snapshot-id";

    /**
     * The property value set on a space to indicate that it is a restored
     * snapshot.
     */
    public static final String RESTORE_ID_PROP = "restore-id";

    
    /**
     * The name of the http request attribute containing the account id (ie subdomain)
     * indicated in the caller's URL.
     */
    public static final String ACCOUNT_ID_ATTRIBUTE = "org.duracloud.account.id";

    public static final String SERVER_HOST = "org.duracloud.request.host";

    public static final String SERVER_PORT = "org.duracloud.request.port";

}
