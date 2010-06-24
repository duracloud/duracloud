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
 *         Date: Dec 18, 2009
 */
public interface ServicePropsFinder {

    public Map<String, String> getProps(String serviceId);
}
