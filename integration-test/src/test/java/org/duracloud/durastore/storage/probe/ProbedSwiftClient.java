/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.storage.probe;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Module;
import org.duracloud.common.util.metrics.Metric;
import org.duracloud.common.util.metrics.MetricException;
import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.common.util.metrics.MetricsTable;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.http.options.GetOptions;
import org.jclouds.openstack.swift.SwiftApiMetadata;
import org.jclouds.openstack.swift.SwiftClient;
import org.jclouds.openstack.swift.domain.AccountMetadata;
import org.jclouds.openstack.swift.domain.ContainerMetadata;
import org.jclouds.openstack.swift.domain.MutableObjectInfoWithMetadata;
import org.jclouds.openstack.swift.domain.ObjectInfo;
import org.jclouds.openstack.swift.domain.SwiftObject;
import org.jclouds.openstack.swift.options.CreateContainerOptions;
import org.jclouds.openstack.swift.options.ListContainerOptions;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Erik Paulsson
 *         Date: 8/12/13
 */
public class ProbedSwiftClient implements SwiftClient, MetricsProbed {

    protected MetricsTable metricsTable;
    protected Metric metric = null;

    private SwiftClient swiftClient;

    public ProbedSwiftClient(String username, String apiAccessKey, String endpoint) {
        ListeningExecutorService useExecutor = createThreadPool();
        ListeningExecutorService ioExecutor = createThreadPool();

        Iterable<Module> modules = ImmutableSet.<Module> of(
            new EnterpriseConfigurationModule(useExecutor, ioExecutor));

        Properties properties = new Properties();
        properties.setProperty(Constants.PROPERTY_STRIP_EXPECT_HEADER,
                               "true");

        swiftClient = ContextBuilder.newBuilder(new SwiftApiMetadata())
                        .endpoint(endpoint)
                        .credentials(username, apiAccessKey)
                        .modules(modules)
                        .overrides(properties)
                        .buildApi(SwiftClient.class);
    }

    protected ListeningExecutorService createThreadPool() {
        return MoreExecutors.listeningDecorator(
            new ThreadPoolExecutor(0, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS,
                                   new SynchronousQueue<Runnable>()));
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

    public SwiftObject newSwiftObject() {
        startMetric("newSwiftObject");
        SwiftObject swiftObject = swiftClient.newSwiftObject();
        stopMetric("newSwiftObject");
        return swiftObject;
    }

    public AccountMetadata getAccountStatistics() {
        startMetric("getAccountStatistics");
        AccountMetadata accountMetadata = swiftClient.getAccountStatistics();
        stopMetric("getAccountStatistics");
        return accountMetadata;
    }

    public Set<ContainerMetadata> listContainers(ListContainerOptions... listContainerOptionses) {
        startMetric("listContainers");
        Set<ContainerMetadata> containerMetadatas =
                swiftClient.listContainers(listContainerOptionses);
        stopMetric("listContainers");
        return containerMetadatas;
    }

    public ContainerMetadata getContainerMetadata(String s) {
        startMetric("getContainerMetadata");
        ContainerMetadata containerMetadata = swiftClient.getContainerMetadata(s);
        stopMetric("getContainerMetadata");
        return containerMetadata;
    }

    public boolean setContainerMetadata(String s, Map<String, String> stringStringMap) {
        startMetric("setContainerMetadata");
        boolean success = swiftClient.setContainerMetadata(s, stringStringMap);
        stopMetric("setContainerMetadata");
        return success;
    }

    public boolean deleteContainerMetadata(String s, Iterable<String> strings) {
        startMetric("deleteContainerMetadata");
        boolean success = swiftClient.deleteContainerMetadata(s, strings);
        stopMetric("deleteContainerMetadata");
        return success;
    }

    public boolean createContainer(String s) {
        startMetric("createContainer");
        boolean success = swiftClient.createContainer(s);
        stopMetric("createContainer");
        return success;
    }

    public boolean createContainer(String s, CreateContainerOptions... createContainerOptionses) {
        startMetric("createContainer");
        boolean success = swiftClient.createContainer(s, createContainerOptionses);
        stopMetric("createContainer");
        return success;
    }

    public boolean deleteContainerIfEmpty(String s) {
        startMetric("deleteContainerIfEmpty");
        boolean success = swiftClient.deleteContainerIfEmpty(s);
        stopMetric("deleteContainerIfEmpty");
        return success;
    }

    public boolean containerExists(String s) {
        startMetric("containerExists");
        boolean success = swiftClient.containerExists(s);
        stopMetric("containerExists");
        return success;
    }

    public PageSet<ObjectInfo> listObjects(String s, ListContainerOptions... listContainerOptionses) {
        startMetric("listObjects");
        PageSet<ObjectInfo> objectInfos = swiftClient.listObjects(s, listContainerOptionses);
        stopMetric("listObjects");
        return objectInfos;
    }

    public SwiftObject getObject(String s, String s2, GetOptions... getOptionses) {
        startMetric("getObject");
        SwiftObject swiftObject = swiftClient.getObject(s, s2, getOptionses);
        stopMetric("getObject");
        return swiftObject;
    }

    public boolean setObjectInfo(String s, String s2, Map<String, String> stringStringMap) {
        startMetric("setObjectInfo");
        boolean success = swiftClient.setObjectInfo(s, s2, stringStringMap);
        stopMetric("setObjectInfo");
        return success;
    }

    public MutableObjectInfoWithMetadata getObjectInfo(String s, String s2) {
        startMetric("getObjectInfo");
        MutableObjectInfoWithMetadata mutableObjectInfoWithMetadata =
                swiftClient.getObjectInfo(s, s2);
        stopMetric("getObjectInfo");
        return mutableObjectInfoWithMetadata;
    }

    public String putObject(String s, SwiftObject swiftObject) {
        startMetric("putObject");
        String checksum = swiftClient.putObject(s, swiftObject);
        stopMetric("putObject");
        return checksum;
    }

    public boolean copyObject(String s, String s2, String s3, String s4) {
        startMetric("copyObject");
        boolean success = swiftClient.copyObject(s, s2, s3, s4);
        stopMetric("copyObject");
        return success;
    }

    public void removeObject(String s, String s2) {
        startMetric("removeObject");
        swiftClient.removeObject(s, s2);
        stopMetric("removeObject");
    }

    public boolean objectExists(String s, String s2) {
        startMetric("objectExists");
        boolean success = swiftClient.objectExists(s, s2);
        stopMetric("objectExists");
        return success;
    }

    public String putObjectManifest(String s, String s2) {
        startMetric("putObjectManifest");
        String result = swiftClient.putObjectManifest(s, s2);
        stopMetric("putObjectManifest");
        return result;
    }

    @Override
    public void close() throws IOException {
        swiftClient.close();
    }
}
