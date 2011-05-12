/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.rest;

import org.duracloud.client.ServicesManager;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class ServiceReportResource {

    ServicesManager servicesMgr = null;

    public void initialize(ServicesManager servicesMgr) {
        this.servicesMgr = servicesMgr;
    }

    String xml = "<value>NOT YET IMPLEMENTED</value>";

    public String getServiceReport(){
        checkInitialized();
        return xml;
    }

    public void checkInitialized() {
        if(null == servicesMgr) {
            throw new RuntimeException("DuraReport must be initialized.");
        }
    }

}
