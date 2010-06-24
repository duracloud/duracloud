/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.catalog;

import org.apache.commons.io.FilenameUtils;
import org.duracloud.servicesutil.util.error.IllegalBundleNameException;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class keeps track of the bundles that are installed into the OSGi
 * container, and how many services have dependencies on any given bundle.
 *
 * @author Andrew Woods
 *         Date: Dec 11, 2009
 */
public class BundleCatalog {

    private static Map<String, Integer> catalog = new ConcurrentHashMap<String, Integer>();

    /**
     * This method increments the usage count of the arg name in the catalog.
     *
     * @param name of bundle to catalog
     * @return true if this is the first occurrence of arg name in catalog
     */
    public static boolean register(String name) {
        return doRegister(normalizeName(name));
    }

    private static boolean doRegister(String name) {
        boolean firstOccurrence = false;

        Integer count = catalog.get(name);
        if (null == count) {
            firstOccurrence = true;
            count = 0;
        }

        catalog.put(name, count + 1);
        return firstOccurrence;
    }

    /**
     * This method decrements the usage count of the arg name in the catalog.
     *
     * @param name of bundle to catalog
     * @return true if this is the last usage of the arg name in the catalog
     */
    public static boolean unRegister(String name) {
        return doUnRegister(normalizeName(name));
    }

    private static boolean doUnRegister(String name) {
        boolean lastUsage = true;

        Integer count = catalog.remove(name);
        if (null == count) {
            throw new NoSuchElementException("Not found in catalog: " + name);
        }

        if (count > 1) {
            lastUsage = false;
            catalog.put(name, count - 1);
        }

        return lastUsage;
    }

    private static String normalizeName(String name) {
        String ext = FilenameUtils.getExtension(name);
        if (!ext.equals("jar")) {
            throw new IllegalBundleNameException("Must be a jar: " + name);
        }

        return FilenameUtils.getBaseName(name);
    }

    /**
     * This method removes all catalog entries.
     */
    public static void clearCatalog() {
        catalog.clear();
    }
}
