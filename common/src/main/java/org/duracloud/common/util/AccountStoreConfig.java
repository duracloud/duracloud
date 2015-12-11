/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;
/**
 * This class supplies information related to the account store configuration.
 * 
 * @author Daniel Bernstein
 *
 */
public class AccountStoreConfig {
    
    public static boolean accountStoreIsLocal(){
        String accountStore = System.getProperty("org.duracloud.accountstore", "local");
        return accountStore.trim().toLowerCase().equals("local");
    }
}
