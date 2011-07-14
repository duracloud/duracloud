/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.serialize;

import org.duracloud.common.xml.XmlSerializer;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.StorageReportBase;

/**
 * Handles moving metrics information into and out of XML format
 *
 * @author: Bill Branan
 * Date: 5/13/11
 */
public class StorageReportSerializer extends XmlSerializer<StorageReport> {

    public StorageReportSerializer() {
        super(StorageReport.class,
              StorageReportBase.SCHEMA_NAME,
              StorageReportBase.SCHEMA_VERSION);
    }

}
