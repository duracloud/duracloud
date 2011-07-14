/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.serialize;

import org.duracloud.common.xml.XmlSerializer;
import org.duracloud.serviceconfig.ServiceReportBase;
import org.duracloud.serviceconfig.ServiceReportList;

/**
 * @author: Bill Branan
 * Date: 7/14/11
 */
public class ServiceReportListSerializer extends XmlSerializer<ServiceReportList> {

    public ServiceReportListSerializer() {
        super(ServiceReportList.class,
              ServiceReportBase.SCHEMA_NAME,
              ServiceReportBase.SCHEMA_VERSION);
    }

}
