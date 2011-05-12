/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.rest;

import org.duracloud.client.ContentStoreManager;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class StorageReportResource {

    ContentStoreManager storeMgr = null;

    public void initialize(ContentStoreManager storeMgr) {
        this.storeMgr = storeMgr;
    }

    String xml = "<value>NOT YET IMPLEMENTED</value>";

    public String getStorageReport(){
        checkInitialized();
        return xml;
    }

    public String getStorageReportInfo(){
        checkInitialized();
        return xml;
    }

    public String startStorageReport(){
        checkInitialized();
        return "NOT YET IMPLEMENTED";
    }

    public void checkInitialized() {
        if(null == storeMgr) {
            throw new RuntimeException("DuraReport must be initialized.");
        }
    }

}
