package org.duracloud.durastore.storage.probe;

import org.duracloud.common.util.metrics.Metric;
import org.duracloud.common.util.metrics.MetricException;
import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.common.util.metrics.MetricsTable;
import org.soyatec.windowsazure.blob.BlobStorageClient;
import org.soyatec.windowsazure.blob.DateTime;
import org.soyatec.windowsazure.blob.IBlobContainer;
import org.soyatec.windowsazure.blob.IContainerAccessControl;
import org.soyatec.windowsazure.blob.ISharedAccessUrl;
import org.soyatec.windowsazure.blob.ResourceType;
import org.soyatec.windowsazure.blob.internal.RetryPolicies;
import org.soyatec.windowsazure.internal.util.NameValueCollection;
import org.soyatec.windowsazure.internal.util.TimeSpan;

import java.net.URI;
import java.util.List;

import static org.duracloud.azurestorage.AzureStorageProvider.BLOB_NAMESPACE;

/**
 * This class wraps a Azure BlobClient implementation, collecting timing
 * metrics while passing calls down.
 *
 * @author Kristen Cannava
 */
public class ProbedAzureBlobClient extends BlobStorageClient implements MetricsProbed {
    protected BlobStorageClient blobStorage = null;

    protected MetricsTable metricsTable;

    protected Metric metric = null;

    ProbedAzureBlobClient(String username, String apiAccessKey) {
        super(URI.create(BLOB_NAMESPACE), false, username, apiAccessKey);

        blobStorage = BlobStorageClient.create(URI.create(BLOB_NAMESPACE),
                                               false,
                                               username,
                                               apiAccessKey);
        /*
         * Set retry policy for a time interval of 5 seconds.
         */
        blobStorage.setRetryPolicy(RetryPolicies.retryN(1, TimeSpan.fromSeconds(
            5)));
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
            throw new RuntimeException(new MetricException(
                "Metrics table has not been set."));
        }
        return this.metricsTable;
    }

    @Override
    public List<IBlobContainer> listBlobContainers() {
        startMetric("listBlobContainers");
        List<IBlobContainer> result = blobStorage.listBlobContainers();
        stopMetric("listBlobContainers");
        return result;
    }

    @Override
    public IBlobContainer getBlobContainer(String container) {
        startMetric("getBlobContainer");
        IBlobContainer result = blobStorage.getBlobContainer(container);
        stopMetric("getBlobContainer");
        return result;
    }

    @Override
    public IBlobContainer createContainer(String name,
                                          NameValueCollection properties,
                                          IContainerAccessControl accessControl) {
        startMetric("createContainer");
        IBlobContainer result = blobStorage.createContainer(name,
                                                            properties,
                                                            accessControl);
        stopMetric("createContainer");
        return result;
    }

    @Override
    public IBlobContainer createContainer(String name) {
        startMetric("createContainer");
        IBlobContainer result = blobStorage.createContainer(name);
        stopMetric("createContainer");
        return result;
    }

    @Override
    public String getLastStatus() {
        startMetric("getLastStatus");
        String result = blobStorage.getLastStatus();
        stopMetric("getLastStatus");
        return result;
    }

    @Override
    public boolean isContainerExist(String name) {
        startMetric("isContainerExist");
        boolean result = blobStorage.isContainerExist(name);
        stopMetric("isContainerExist");
        return result;
    }

    @Override
    public boolean deleteContainer(String container) {
        startMetric("deleteContainer");
        boolean result = blobStorage.deleteContainer(container);
        stopMetric("deleteContainer");
        return result;
    }

    @Override
    public ISharedAccessUrl createSharedAccessUrl(String s,
                                                  String s1,
                                                  ResourceType resourceType,
                                                  int i,
                                                  DateTime dateTime,
                                                  DateTime dateTime1,
                                                  String s2) {
        startMetric("createSharedAccessUrl");
        ISharedAccessUrl result = blobStorage.createSharedAccessUrl(s,
                                                                    s1,
                                                                    resourceType,
                                                                    i,
                                                                    dateTime,
                                                                    dateTime1,
                                                                    s2);
        stopMetric("createSharedAccessUrl");
        return result;
    }
}
