/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.serialize;

import org.duracloud.reportdata.storage.StorageReportInfo;

/**
 * @author: Bill Branan
 * Date: 6/2/11
 */
public class StorageReportInfoSerializer extends StorageReportSerializerBase<StorageReportInfo> {

    public StorageReportInfoSerializer() {
        super(StorageReportInfo.class);
    }

}
