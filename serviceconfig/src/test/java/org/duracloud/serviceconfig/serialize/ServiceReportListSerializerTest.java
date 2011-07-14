/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.serialize;

import org.duracloud.serviceconfig.ServiceReportList;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 7/14/11
 */
public class ServiceReportListSerializerTest {

    @Test
    public void testServiceReportListSerializer() {
        ServiceReportListSerializer serializer =
            new ServiceReportListSerializer();

        ServiceReportList reportList = createServiceReportList();

        String xml = serializer.serialize(reportList);
        assertNotNull(xml);

        ServiceReportList listDeserialized = serializer.deserialize(xml);
        assertNotNull(listDeserialized);

        assertEquals(reportList, listDeserialized);
        assertEquals(xml, serializer.serialize(listDeserialized));
    }

    private ServiceReportList createServiceReportList() {
        String reportId1 = "reportId1";
        String reportId2 = "reportId2";
        String reportId3 = "reportId3";

        List<String> listData = new LinkedList<String>();
        listData.add(reportId1);
        listData.add(reportId2);
        listData.add(reportId3);
        return new ServiceReportList(listData);
    }

}
