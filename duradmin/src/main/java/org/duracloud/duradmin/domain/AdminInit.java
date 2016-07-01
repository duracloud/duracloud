/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.domain;

/**
 * @author: Bill Branan
 * Date: Jan 15, 2010
 */
public class AdminInit {

    private String amaUrl;
    private boolean millDbEnabled;


    public String getAmaUrl() {
        return amaUrl;
    }

    public void setAmaUrl(String amaUrl) {
        this.amaUrl = amaUrl;
    }

    public void setMillDbEnabled(boolean millDbEnabled) {
        this.millDbEnabled = millDbEnabled;
    }
    
    public boolean isMillDbEnabled() {
        return millDbEnabled;
    }
}