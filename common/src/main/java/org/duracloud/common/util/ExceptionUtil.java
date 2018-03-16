/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

public class ExceptionUtil {

    private ExceptionUtil() {
        // Ensures no instances are made of this class, as there are only static members.
    }

    public static String getStackTraceAsString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement elem : e.getStackTrace()) {
            sb.append(elem.toString() + "\n");
        }
        return sb.toString();
    }

}
