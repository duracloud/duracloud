/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.serialize;

import org.duracloud.reportdata.storage.ReportTestHelper;
import org.duracloud.reportdata.storage.StorageReportList;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 6/2/11
 */
public class StorageReportListSerializerTest {

    private ReportTestHelper<StorageReportList> testHelper;

    @Before
    public void setup() {
        this.testHelper = new ReportTestHelper<StorageReportList>();
    }

    @Test
    public void testStorageReportListSerializer() {
        StorageReportListSerializer serializer =
            new StorageReportListSerializer();

        StorageReportList reportList = createStorageReportList();

        String xml = serializer.serialize(reportList);
        assertNotNull(xml);

        StorageReportList listDeserialized = serializer.deserialize(xml);
        assertNotNull(listDeserialized);

        assertEquals(reportList, listDeserialized);
        assertEquals(xml, serializer.serialize(listDeserialized));
    }

    @Test
    public void testSchemaVersionCheck() {
        String schemaVersion = "42";
        StorageReportList reportList = createStorageReportList();
        reportList.setSchemaVersion(schemaVersion);
        testHelper.schemaVersionCheck(reportList, schemaVersion,
                                      new StorageReportListSerializer());
    }

    @Test
    public void testValidationCheck() {
        StorageReportList reportList = createStorageReportList();
        testHelper.validationCheck(reportList,
                                   new StorageReportListSerializer());
    }

    private StorageReportList createStorageReportList() {
        String reportId1 = "reportId1";
        String reportId2 = "reportId2";
        String reportId3 = "reportId3";

        List<String> listData = new LinkedList<String>();
        listData.add(reportId1);
        listData.add(reportId2);
        listData.add(reportId3);
        return new StorageReportList(listData);
    }
}
