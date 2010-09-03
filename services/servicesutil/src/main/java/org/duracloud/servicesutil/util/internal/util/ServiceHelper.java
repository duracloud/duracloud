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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Dec 18, 2009
 */
public class ServiceHelper {
    private final Logger log = LoggerFactory.getLogger(ServiceHelper.class);

    public ComputeService findService(String serviceId,
                                      List<ComputeService> duraServices) {
        log.debug("finding: " + serviceId + ", size: " + duraServices.size());

        ComputeService target = null;
        String normalizedId = FilenameUtils.getBaseName(serviceId).trim();
        for (ComputeService service : duraServices) {
            String id = service.getServiceId().trim();
            log.debug("service in container: " + id);

            if (normalizedId.equalsIgnoreCase(id)) {
                target = service;
                break;
            }
        }

        if (null == target) {
            throw new ServiceRuntimeException("Service not found:" + serviceId);
        }

        log.debug("found: " + target.getServiceId() + ", for: " + serviceId);
        return target;

    }

}
