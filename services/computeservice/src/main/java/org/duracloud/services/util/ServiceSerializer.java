/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.util;

import java.util.List;

import org.duracloud.services.ComputeService;
import org.duracloud.services.beans.ComputeServiceBean;

public interface ServiceSerializer {

    public abstract String serialize(List<ComputeService> services)
            throws Exception;

    public abstract List<ComputeServiceBean> deserializeList(String xml);

    public abstract String serialize(ComputeServiceBean bean) throws Exception;

    public abstract ComputeServiceBean deserializeBean(String xml);

}
