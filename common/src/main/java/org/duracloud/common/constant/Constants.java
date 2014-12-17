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

    /**
     * This structure defines the system managed spaces.
     */
    public static final List<String> SYSTEM_SPACES = Arrays
        .asList("x-duracloud-admin", "x-service-out", "x-service-work");

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

}
