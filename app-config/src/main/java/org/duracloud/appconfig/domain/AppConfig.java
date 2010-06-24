/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import java.util.Map;

/**
 * This interface defines the contract of a configuration set used for
 * initializing the duracloud applications: duradmin, durastore, duraservice.
 *
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public interface AppConfig {

    /**
     * This method loads this classes configuration elements from the provided
     * properties.
     *
     * @param props
     */
    public void load(Map<String, String> props);

    /**
     * This method returns an XML serialization of the configuration.
     *
     * @return
     */
    public String asXml();

    /**
     * This method returns the URL path element of the application's
     * initialization resource.
     *
     * @return
     */
    public String getInitResource();
}
