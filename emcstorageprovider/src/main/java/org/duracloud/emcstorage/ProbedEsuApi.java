/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.emcstorage;

import com.emc.esu.api.*;
import com.emc.esu.api.rest.EsuRestApi;
import org.duracloud.common.util.metrics.Metric;
import org.duracloud.common.util.metrics.MetricException;
import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.common.util.metrics.MetricsTable;
import static org.duracloud.emcstorage.EMCStorageProvider.ESU_HOST;
import static org.duracloud.emcstorage.EMCStorageProvider.ESU_PORT;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class ProbedEsuApi
        implements EsuApi, MetricsProbed {

    private final EsuApi esuApi;

    protected MetricsTable metricsTable;

    protected Metric metric;

    public ProbedEsuApi(String uid, String sharedSecret) {
        esuApi = new EsuRestApi(ESU_HOST, ESU_PORT, uid, sharedSecret);
    }

    protected void startMetric(String methodName) {
        if (metric == null) {
            metric = new Metric(getClass().getName(), methodName);
            getMetricsTable().addMetric(metric);
        } else {
            metric.addElement(methodName);
        }

        metric.start(methodName);
    }

    protected void stopMetric(String methodName) {
        metric.stop(methodName);
    }

    public void setMetricsTable(MetricsTable metricsTable) {
        this.metricsTable = metricsTable;
        this.metric = null;
    }

    private MetricsTable getMetricsTable() {
        if (this.metricsTable == null) {
            throw new RuntimeException(new MetricException("Metrics table has not been set."));
        }
        return this.metricsTable;
    }

    public ObjectId createObject(Acl arg0,
                                 MetadataList arg1,
                                 byte[] arg2,
                                 String arg3) {
        startMetric("createObject");
        ObjectId result = esuApi.createObject(arg0, arg1, arg2, arg3);
        stopMetric("createObject");
        return result;
    }

    public ObjectId createObjectFromStream(Acl acl, MetadataList metadataList,
                                           InputStream inputStream,
                                           int i,
                                           String s) {
        // Default method body
        return null;
    }

    public ObjectId createObjectFromSegment(Acl arg0,
                                            MetadataList arg1,
                                            BufferSegment arg2,
                                            String arg3) {
        startMetric("createObjectFromSegment");
        ObjectId result =
                esuApi.createObjectFromSegment(arg0, arg1, arg2, arg3);
        stopMetric("createObjectFromSegment");
        return result;
    }

    public ObjectId createObjectFromSegmentOnPath(ObjectPath arg0,
                                                  Acl arg1,
                                                  MetadataList arg2,
                                                  BufferSegment arg3,
                                                  String arg4) {
        startMetric("createObjectFromSegmentOnPath");
        ObjectId result =
                esuApi.createObjectFromSegmentOnPath(arg0,
                                                     arg1,
                                                     arg2,
                                                     arg3,
                                                     arg4);
        stopMetric("createObjectFromSegmentOnPath");
        return result;
    }

    public ObjectId createObjectOnPath(ObjectPath arg0,
                                       Acl arg1,
                                       MetadataList arg2,
                                       byte[] arg3,
                                       String arg4) {
        startMetric("createObjectOnPath");
        ObjectId result =
                esuApi.createObjectOnPath(arg0, arg1, arg2, arg3, arg4);
        stopMetric("createObjectOnPath");
        return result;
    }

    public void deleteObject(Identifier arg0) {
        startMetric("deleteObject");
        esuApi.deleteObject(arg0);
        stopMetric("deleteObject");
    }

    public void deleteUserMetadata(Identifier arg0, MetadataTags arg1) {
        startMetric("deleteUserMetadata");
        esuApi.deleteUserMetadata(arg0, arg1);
        stopMetric("deleteUserMetadata");
    }

    public Acl getAcl(Identifier arg0) {
        startMetric("getAcl");
        Acl result = esuApi.getAcl(arg0);
        stopMetric("getAcl");
        return result;
    }

    public ObjectMetadata getAllMetadata(Identifier arg0) {
        startMetric("getAllMetadata");
        ObjectMetadata result = esuApi.getAllMetadata(arg0);
        stopMetric("getAllMetadata");
        return result;
    }

    public MetadataTags getListableTags(MetadataTag arg0) {
        startMetric("getListableTags");
        MetadataTags result = esuApi.getListableTags(arg0);
        stopMetric("getListableTags");
        return result;
    }

    public MetadataTags getListableTags(String arg0) {
        startMetric("getListableTags");
        MetadataTags result = esuApi.getListableTags(arg0);
        stopMetric("getListableTags");
        return result;
    }

    public URL getShareableUrl(Identifier arg0, Date arg1) {
        startMetric("getShareableUrl");
        URL result = esuApi.getShareableUrl(arg0, arg1);
        stopMetric("getShareableUrl");
        return result;
    }

    public MetadataList getSystemMetadata(Identifier arg0, MetadataTags arg1) {
        startMetric("getSystemMetadata");
        MetadataList result = esuApi.getSystemMetadata(arg0, arg1);
        stopMetric("getSystemMetadata");
        return result;
    }

    public MetadataList getUserMetadata(Identifier arg0, MetadataTags arg1) {
        startMetric("getUserMetadata");
        MetadataList result = esuApi.getUserMetadata(arg0, arg1);
        stopMetric("getUserMetadata");
        return result;
    }

    public List<DirectoryEntry> listDirectory(ObjectPath arg0) {
        startMetric("listDirectory");
        List<DirectoryEntry> result = esuApi.listDirectory(arg0);
        stopMetric("listDirectory");
        return result;
    }

    public List<Identifier> listObjects(MetadataTag arg0) {
        startMetric("listObjects");
        List<Identifier> result = esuApi.listObjects(arg0);
        stopMetric("listObjects");
        return result;
    }

    public List<Identifier> listObjects(String arg0) {
        startMetric("listObjects");
        List<Identifier> result = esuApi.listObjects(arg0);
        stopMetric("listObjects");
        return result;
    }

    public List<ObjectResult> listObjectsWithMetadata(MetadataTag arg0) {
        startMetric("listObjectsWithMetadata");
        List<ObjectResult> result = esuApi.listObjectsWithMetadata(arg0);
        stopMetric("listObjectsWithMetadata");
        return result;
    }

    public List<ObjectResult> listObjectsWithMetadata(String arg0) {
        startMetric("listObjectsWithMetadata");
        List<ObjectResult> result = esuApi.listObjectsWithMetadata(arg0);
        stopMetric("listObjectsWithMetadata");
        return result;
    }

    public MetadataTags listUserMetadataTags(Identifier arg0) {
        startMetric("listUserMetadataTags");
        MetadataTags result = esuApi.listUserMetadataTags(arg0);
        stopMetric("listUserMetadataTags");
        return result;
    }

    public List<Identifier> listVersions(Identifier arg0) {
        startMetric("listVersions");
        List<Identifier> result = esuApi.listVersions(arg0);
        stopMetric("listVersions");
        return result;
    }

    public List<Identifier> queryObjects(String arg0) {
        startMetric("queryObjects");
        List<Identifier> result = esuApi.queryObjects(arg0);
        stopMetric("queryObjects");
        return result;
    }

    public byte[] readObject(Identifier arg0, Extent arg1, byte[] arg2) {
        startMetric("readObject");
        byte[] result = esuApi.readObject(arg0, arg1, arg2);
        stopMetric("readObject");
        return result;
    }

    public InputStream readObjectStream(Identifier identifier, Extent extent) {
        // Default method body
        return null;
    }

    public void setAcl(Identifier arg0, Acl arg1) {
        startMetric("setAcl");
        esuApi.setAcl(arg0, arg1);
        stopMetric("setAcl");
    }

    public void setUserMetadata(Identifier arg0, MetadataList arg1) {
        startMetric("setUserMetadata");
        esuApi.setUserMetadata(arg0, arg1);
        stopMetric("setUserMetadata");
    }

    public void updateObject(Identifier arg0,
                             Acl arg1,
                             MetadataList arg2,
                             Extent arg3,
                             byte[] arg4,
                             String arg5) {
        startMetric("updateObject");
        esuApi.updateObject(arg0, arg1, arg2, arg3, arg4, arg5);
        stopMetric("updateObject");
    }

    public void updateObjectFromStream(Identifier identifier, Acl acl,
                                       MetadataList metadataList, Extent extent,
                                       InputStream inputStream,
                                       int i,
                                       String s) {
        // Default method body

    }

    public void updateObjectFromSegment(Identifier arg0,
                                        Acl arg1,
                                        MetadataList arg2,
                                        Extent arg3,
                                        BufferSegment arg4,
                                        String arg5) {
        startMetric("updateObjectFromSegment");
        esuApi.updateObjectFromSegment(arg0, arg1, arg2, arg3, arg4, arg5);
        stopMetric("updateObjectFromSegment");
    }

    public ObjectId versionObject(Identifier arg0) {
        startMetric("versionObject");
        ObjectId result = esuApi.versionObject(arg0);
        stopMetric("versionObject");
        return result;
    }
}
