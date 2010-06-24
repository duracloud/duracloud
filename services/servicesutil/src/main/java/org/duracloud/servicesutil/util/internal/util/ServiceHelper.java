/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal.util;

import org.apache.commons.io.FilenameUtils;
import org.duracloud.services.ComputeService;
import org.duracloud.services.common.error.ServiceRuntimeException;

import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Dec 18, 2009
 */
public class ServiceHelper {

    public ComputeService findService(String serviceId,
                                      List<ComputeService> duraServices) {
        ComputeService target = null;
        String normalizedId = FilenameUtils.getBaseName(serviceId).trim();
        for (ComputeService service : duraServices) {
            String id = FilenameUtils.getBaseName(service.getServiceId().trim());
            if (normalizedId.equalsIgnoreCase(id) || normalizedId.contains(id)) {
                target = service;
                break;
            }
        }

        if (null == target) {
            throw new ServiceRuntimeException("Service not found:" + serviceId);
        }
        return target;

    }

}
