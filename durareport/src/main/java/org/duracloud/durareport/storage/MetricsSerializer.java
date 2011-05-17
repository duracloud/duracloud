/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.storage;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.duracloud.durareport.storage.metrics.DuraStoreMetrics;
import org.duracloud.durareport.storage.metrics.Metrics;
import org.duracloud.durareport.storage.metrics.MimetypeMetrics;
import org.duracloud.durareport.storage.metrics.SpaceMetrics;
import org.duracloud.durareport.storage.metrics.StorageProviderMetrics;

/**
 * Handles moving metrics information into and out of XML format
 *
 * @author: Bill Branan
 * Date: 5/13/11
 */
public class MetricsSerializer {

    private XStream xstream;

    public MetricsSerializer() {
        xstream = new XStream(new DomDriver());

        // Class names
        xstream.alias("durastore", DuraStoreMetrics.class);
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

        // DuraStoreMetrics fields
        xstream.aliasField("storage-provider-metrics",
                           DuraStoreMetrics.class,
                           "storageProviderMetrics");        
    }

    public String serializeMetrics(DuraStoreMetrics metrics) {
        return xstream.toXML(metrics);
    }

    public DuraStoreMetrics deserializeMetrics(String xml) {
        if(xml == null || xml.equals("")) {
            return new DuraStoreMetrics();
        } else {
            return (DuraStoreMetrics)xstream.fromXML(xml);
        }
    }

}
