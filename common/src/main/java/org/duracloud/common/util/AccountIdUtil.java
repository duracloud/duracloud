/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class AccountIdUtil {
    public static String extractAccountIdFromHost(String host){
        return host.split("[.]")[0];
    }
}
