/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage;

import org.duracloud.common.xml.SchemaGenerator;
import org.duracloud.reportdata.storage.serialize.StorageReportSerializer;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: 7/13/11
 */
public class SchemaGeneratorTest {

    @Test
    public void testGenerateSchema() throws Exception {
        String fileName = StorageReportBase.SCHEMA_NAME;
        SchemaGenerator generator = new SchemaGenerator(fileName);
        generator.generateSchema(StorageReportInfo.class,
                                 StorageReportList.class,
                                 StorageReport.class);
    }

}
