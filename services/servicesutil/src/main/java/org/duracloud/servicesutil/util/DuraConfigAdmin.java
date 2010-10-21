/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util;

import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Jan 1, 2010
 */
public interface DuraConfigAdmin {

    @SuppressWarnings("unchecked")
    public abstract Map<String, String> getConfiguration(String configId)
            throws Exception;

    /**
     * This method replaces any existing properties associated with the arg
     * configId with the properties provided in arg props.
     * @param configId
     * @param props
     * @throws Exception
     */
    public abstract void updateConfiguration(String configId,
                                             Map<String, String> props)
        throws Exception;

}