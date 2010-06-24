/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.computeprovider.mgmt;

import java.util.Map;

import org.duracloud.computeprovider.domain.ComputeProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a factory for creating instances of ComputeProviders.
 * <p>
 * It holds a map of compute-provider-types and their class-names.
 * The mapping between the id & classes is configured in the Spring config files.
 *
 * ComputeProvider instances ARE NOT cached after being created.
 * </p>
 *
 * @author Andrew Woods
 */
public class ComputeProviderFactory {

    protected static final Logger log =
            LoggerFactory.getLogger(ComputeProviderFactory.class);

    private static Map<String, String> typeToClassMap;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("null")
    public static ComputeProvider getComputeProvider(ComputeProviderType providerType)
            throws Exception {
        ComputeProvider provider = null;

        Exception exception = null;

        String className = getClassNameFromId(providerType);
        log.debug("class for id: '" + providerType + "' : '" + className + "'");

        Class<?> clazz = getClass(className, exception);
        if (clazz != null) {
            provider = getInstance(clazz, exception);
        }

        if (provider == null) {
            throw exception;
        }
        return provider;
    }

    private static String getClassNameFromId(ComputeProviderType providerType)
            throws Exception {
        String className = null;
        try {
            className = typeToClassMap.get(providerType.toString());
        } catch (Exception e) {
            log.error("Error retrieving from map", e);
            throw e;
        }
        return className;
    }

    private static Class<?> getClass(String className, Exception exception) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("No class found for classname: '" + className + "'", e);
            exception = e;
        }
        return clazz;
    }

    private static ComputeProvider getInstance(Class<?> clazz,
                                               Exception exception) {
        ComputeProvider provider = null;
        try {
            provider = (ComputeProvider) clazz.newInstance();
        } catch (InstantiationException e) {
            log.error("Error with instantiation", e);
            exception = e;
        } catch (IllegalAccessException e) {
            log.error("Illegal access", e);
            exception = e;
        }
        return provider;
    }

    public Map<String, String> getIdToClassMap() {
        return typeToClassMap;
    }

    public static void setIdToClassMap(Map<String, String> map) {
        typeToClassMap = map;
    }

}
