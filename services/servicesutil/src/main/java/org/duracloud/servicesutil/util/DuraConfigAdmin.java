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
     * <pre>
     * The update policy for arg properties is that:
     *   1. All arg props will be appended to existing set of properties.
     *   2. A new configuration will be created if there is no existing properties.
     *   3. If an arg prop has the key of an existing property,
     *          the existing property will be overwritten.
     *   4. If a null or empty arg props is provided,
     *          no updates will occur.
     * </pre>
     *
     * @param configId
     * @param props
     * @throws Exception
     */
    public abstract void updateConfiguration(String configId,
                                             Map<String, String> props)
        throws Exception;

}