/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.serialize;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.duracloud.reportdata.storage.StorageReport;
import org.duracloud.reportdata.storage.metrics.Metrics;
import org.duracloud.reportdata.storage.metrics.MimetypeMetrics;
import org.duracloud.reportdata.storage.metrics.SpaceMetrics;
import org.duracloud.reportdata.storage.metrics.StorageMetrics;
import org.duracloud.reportdata.storage.metrics.StorageProviderMetrics;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles moving metrics information into and out of XML format
 *
 * @author: Bill Branan
 * Date: 5/13/11
 */
public class StorageReportSerializer {

    private XStream xstream;

    public StorageReportSerializer() {
        xstream = new XStream(new DomDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.addDefaultImplementation(LinkedList.class, List.class);

        // Class names
        xstream.alias("storage-report", StorageReport.class);
        xstream.alias("storage-metrics", StorageMetrics.class);
        xstream.alias("storage-provider", StorageProviderMetrics.class);
        xstream.alias("space", SpaceMetrics.class);
        xstream.alias("mimetype", MimetypeMetrics.class);

        // Metrics fields
        xstream.aliasField("total-items", Metrics.class, "totalItems");
        xstream.aliasField("total-size", Metrics.class, "totalSize");
        xstream.aliasField("mimetype-metrics", Metrics.class, "mimetypeMetrics");

        // MimetypeMetrics fields
        xstream.aliasField("total-items", MimetypeMetrics.class, "totalItems");
        xstream.aliasField("total-size", MimetypeMetrics.class, "totalSize");
        xstream.useAttributeFor(MimetypeMetrics.class, "mimetype");
        xstream.aliasField("name", MimetypeMetrics.class, "mimetype");

        // SpaceMetrics fields
        xstream.useAttributeFor(SpaceMetrics.class, "spaceName");
        xstream.aliasField("name", SpaceMetrics.class, "spaceName");

        // StorageProviderMetrics fields
        xstream.useAttributeFor(StorageProviderMetrics.class,
                                "storageProviderId");
        xstream.aliasField("id",
                           StorageProviderMetrics.class,
                           "storageProviderId");
        xstream.useAttributeFor(StorageProviderMetrics.class,
                                "storageProviderType");
        xstream.aliasField("type",
                           StorageProviderMetrics.class,
                           "storageProviderType");
        xstream.aliasField("space-metrics",
                           StorageProviderMetrics.class,
                           "spaceMetrics");

        // StorageMetrics fields
        xstream.aliasField("storage-provider-metrics",
                           StorageMetrics.class,
                           "storageProviderMetrics");

        // StorageReport fields
        xstream.aliasField("storage-metrics",
                           StorageReport.class,
                           "storageMetrics");
        xstream.aliasField("report-id",
                           StorageReport.class,
                           "contentId");
        xstream.aliasField("completion-time",
                           StorageReport.class,
                           "completionTime");
        xstream.aliasField("elapsed-time",
                           StorageReport.class,
                           "elapsedTime");
    }

    public String serializeReport(StorageReport storageReport) {
        return xstream.toXML(storageReport);
    }

    public StorageReport deserializeReport(String xml) {
        if(xml == null || xml.equals("")) {
            throw new RuntimeException("Report XML cannot be null or empty");
        } else {
            return (StorageReport)xstream.fromXML(xml);
        }
    }

    public StorageReport deserializeReport(InputStream stream) {
        if(stream == null) {
            throw new RuntimeException("Report stream cannot be null");
        } else {
            return (StorageReport)xstream.fromXML(stream);
        }
    }

}
