/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage;

import org.duracloud.reportdata.storage.error.SerializationException;
import org.duracloud.reportdata.storage.metrics.MimetypeMetrics;
import org.duracloud.reportdata.storage.metrics.SpaceMetrics;
import org.duracloud.reportdata.storage.metrics.StorageMetrics;
import org.duracloud.reportdata.storage.metrics.StorageProviderMetrics;
import org.duracloud.reportdata.storage.serialize.StorageReportSerializerBase;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author: Bill Branan
 * Date: 6/1/11
 */
public class ReportTestHelper<T> {

    public String mimetype1 = "text/plain";
    public String mimetype2 = "text/xml";
    public String mimetype3 = "application/xml";
    public String spaceName1 = "space1";
    public String spaceName2 = "space2";
    public String providerId1 = "provider1";
    public String providerType1 = "AMAZON";
    public String providerId2 = "provider2";
    public String providerType2 = "RACKSPACE";

    public StorageMetrics createMetrics() {
        List<MimetypeMetrics> spaceMime = new LinkedList<MimetypeMetrics>();
        spaceMime.add(new MimetypeMetrics(mimetype1, 1, 100));
        spaceMime.add(new MimetypeMetrics(mimetype2, 2, 200));
        spaceMime.add(new MimetypeMetrics(mimetype3, 3, 300));

        List<SpaceMetrics> spaceMetrics = new LinkedList<SpaceMetrics>();
        spaceMetrics.add(new SpaceMetrics(spaceName1, 6, 600, spaceMime));
        spaceMetrics.add(new SpaceMetrics(spaceName2, 6, 600, spaceMime));

        List<MimetypeMetrics> providerMime = new LinkedList<MimetypeMetrics>();
        providerMime.add(new MimetypeMetrics(mimetype1, 2, 200));
        providerMime.add(new MimetypeMetrics(mimetype2, 4, 400));
        providerMime.add(new MimetypeMetrics(mimetype3, 6, 600));

        List<StorageProviderMetrics> providerMetrics =
            new LinkedList<StorageProviderMetrics>();
        StorageProviderMetrics provider1Metrics =
            new StorageProviderMetrics(providerId1, providerType1, spaceMetrics,
                                       12, 1200, providerMime);
        StorageProviderMetrics provider2Metrics =
            new StorageProviderMetrics(providerId2, providerType2, spaceMetrics,
                                       12, 1200, providerMime);
        providerMetrics.add(provider1Metrics);
        providerMetrics.add(provider2Metrics);

        List<MimetypeMetrics> storageMime = new LinkedList<MimetypeMetrics>();
        storageMime.add(new MimetypeMetrics(mimetype1, 4, 400));
        storageMime.add(new MimetypeMetrics(mimetype2, 8, 800));
        storageMime.add(new MimetypeMetrics(mimetype3, 12, 1200));

        StorageMetrics storageMetrics =
            new StorageMetrics(providerMetrics, 24, 2400, providerMime);

        return storageMetrics;
    }

    public void schemaVersionCheck(T report,
                                   String schemaVersion,
                                   StorageReportSerializerBase serializer) {
        String xml = serializer.serialize(report);
        assertTrue("Report should include schema version",
                   xml.contains(schemaVersion));
    }

    public void validationCheck(T report,
                                StorageReportSerializerBase serializer) {
        String xml = serializer.serialize(report);
        xml = xml.substring(0, xml.length() - 10);
        try {
            serializer.deserialize(xml);
            fail("Exception expected");
        } catch(SerializationException expected) {
            assertNotNull(expected.getMessage());
            assertTrue("Exception should include expected schema version",
                expected.getMessage().contains(StorageReport.SCHEMA_VERSION));
        }
    }

}
