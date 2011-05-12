/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.duracloud.appconfig.xml.DurareportInitDocumentBinding;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class DurareportConfig extends DuradminConfig {

    private static final String INIT_RESOURCE = "/reports";
    public static final String QUALIFIER = "durareport";

    @Override
    public String getInitResource() {
        return INIT_RESOURCE;
    }

    @Override
    protected String getQualifier() {
        return QUALIFIER;
    }

    @Override
    protected boolean isSupported(String key) {
        return (key != null &&
                (key.startsWith(getQualifier()) ||
                 key.startsWith(super.getQualifier())));
    }

    @Override
    public String asXml() {
        return DurareportInitDocumentBinding.createDocumentFrom(this);
    }
    
}
