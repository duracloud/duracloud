/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

/**
 * @author: Bill Branan
 * Date: 9/21/11
 */
public class InitUtil {

    private InitUtil() {
        // Ensures no instances are made of this class, as there are only static members.
    }

    private static final String INITITALIZED =
        " is initialized and ready for use. See the REST API documentation " +
        "for further information on the available resources.";

    private static final String NOT_INITIALIZED =
        " is not yet initialized, please check back soon. If this condition " +
        "persists, please contact your DuraCloud administrator.";

    public static String getInitializedText(String appName) {
        return appName + INITITALIZED;
    }

    public static String getNotInitializedText(String appName) {
        return appName + NOT_INITIALIZED;
    }

}
