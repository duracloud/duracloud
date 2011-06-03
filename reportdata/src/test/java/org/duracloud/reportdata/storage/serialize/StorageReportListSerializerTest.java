/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.serialize;

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

    @Test
    public void testStorageReportListSerializer() {
        StorageReportListSerializer serializer =
            new StorageReportListSerializer();

        String reportId1 = "reportId1";
        String reportId2 = "reportId2";
        String reportId3 = "reportId3";

        List<String> storageReportList = new LinkedList<String>();
        storageReportList.add(reportId1);
        storageReportList.add(reportId2);
        storageReportList.add(reportId3);

        String xml = serializer.serializeReportList(storageReportList);
        assertNotNull(xml);

        List<String> desList = serializer.deserializeReportList(xml);
        assertNotNull(desList);
        assertEquals(storageReportList, desList);
    }
}
