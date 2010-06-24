/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.rackspacestorage;

import com.rackspacecloud.client.cloudfiles.*;
import org.apache.commons.httpclient.HttpException;
import org.duracloud.common.util.metrics.Metric;
import org.duracloud.common.util.metrics.MetricException;
import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.common.util.metrics.MetricsTable;
import org.duracloud.storage.error.StorageException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * This class wraps a Rackspace FilesClient implementation, collecting timing
 * metrics while passing calls down.
 *
 * @author Andrew Woods
 */
public class ProbedFilesClient
        extends FilesClient
        implements MetricsProbed {

    protected MetricsTable metricsTable;

    protected Metric metric = null;

    public ProbedFilesClient(String username, String apiAccessKey)
            throws StorageException {
        super(username, apiAccessKey);
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

    @Override
    public String getAccount() {
        startMetric("getAccount");
        String result = super.getAccount();
        stopMetric("getAccount");
        return result;
    }

    @Override
    public void setAccount(String account) {
        startMetric("setAccount");
        super.setAccount(account);
        stopMetric("setAccount");
    }

    @Override
    public boolean login() throws IOException, HttpException {
        // Not probed because needed in constructor before MetricsTable is available.
        //        startMetric("login");
        boolean result = super.login();
        //        stopMetric("login");
        return result;
    }

    @Override
    public List<FilesContainerInfo> listContainersInfo() throws IOException,
            HttpException, FilesAuthorizationException, FilesException {
        startMetric("listContainersInfo");
        List<FilesContainerInfo> result = super.listContainersInfo();
        stopMetric("listContainersInfo");
        return result;
    }

    @Override
    public List<FilesContainerInfo> listContainersInfo(int limit)
            throws IOException, HttpException, FilesAuthorizationException,
            FilesException {
        startMetric("listContainersInfo");
        List<FilesContainerInfo> result = super.listContainersInfo(limit);
        stopMetric("listContainersInfo");
        return result;
    }

    @Override
    public List<FilesContainerInfo> listContainersInfo(int limit, String marker)
            throws IOException, HttpException, FilesAuthorizationException,
            FilesException {
        startMetric("listContainersInfo");
        List<FilesContainerInfo> result =
                super.listContainersInfo(limit, marker);
        stopMetric("listContainersInfo");
        return result;
    }

    @Override
    public List<FilesContainer> listContainers() throws IOException,
            HttpException, FilesAuthorizationException, FilesException {
        startMetric("listContainers");
        List<FilesContainer> result = super.listContainers();
        stopMetric("listContainers");
        return result;
    }

    @Override
    public List<FilesContainer> listContainers(int limit) throws IOException,
            HttpException, FilesAuthorizationException, FilesException {
        startMetric("listContainers");
        List<FilesContainer> result = super.listContainers(limit);
        stopMetric("listContainers");
        return result;
    }

    @Override
    public List<FilesContainer> listContainers(int limit, String marker)
            throws IOException, HttpException, FilesException {
        startMetric("listContainers");
        List<FilesContainer> result = super.listContainers(limit, marker);
        stopMetric("listContainers");
        return result;
    }

    @Override
    public List<FilesObject> listObjectsStartingWith(String container,
                                                    String startsWith,
                                                    String path,
                                                    int limit,
                                                    String marker)
            throws IOException, HttpException, FilesException {
        startMetric("listObjectsStaringWith");
        List<FilesObject> result =
                super.listObjectsStartingWith(container,
                                              startsWith,
                                              path,
                                              limit,
                                              marker);
        stopMetric("listObjectsStaringWith");
        return result;
    }

    @Override
    public List<FilesObject> listObjects(String container) throws IOException,
            HttpException, FilesAuthorizationException, FilesException {
        startMetric("listObjects");
        List<FilesObject> result = super.listObjects(container);
        stopMetric("listObjects");
        return result;
    }

    @Override
    public List<FilesObject> listObjects(String container, int limit)
            throws IOException, HttpException, FilesAuthorizationException,
            FilesException {
        startMetric("listObjects");
        List<FilesObject> result = super.listObjects(container, limit);
        stopMetric("listObjects");
        return result;
    }

    @Override
    public List<FilesObject> listObjects(String container, String path)
            throws IOException, HttpException, FilesAuthorizationException,
            FilesException {
        startMetric("listObjects");
        List<FilesObject> result = super.listObjects(container, path);
        stopMetric("listObjects");
        return result;
    }

    @Override
    public List<FilesObject> listObjects(String container,
                                         String path,
                                         int limit) throws IOException,
            HttpException, FilesAuthorizationException, FilesException {
        startMetric("listObjects");
        List<FilesObject> result = super.listObjects(container, path, limit);
        stopMetric("listObjects");
        return result;
    }

    @Override
    public List<FilesObject> listObjects(String container,
                                         String path,
                                         int limit,
                                         String marker) throws IOException,
            HttpException, FilesAuthorizationException, FilesException {
        startMetric("listObjects");
        List<FilesObject> result =
                super.listObjects(container, path, limit, marker);
        stopMetric("listObjects");
        return result;
    }

    @Override
    public List<FilesObject> listObjects(String container,
                                         int limit,
                                         String marker) throws IOException,
            HttpException, FilesAuthorizationException, FilesException {
        startMetric("listObjects");
        List<FilesObject> result = super.listObjects(container, limit, marker);
        stopMetric("listObjects");
        return result;
    }

    @Override
    public boolean containerExists(String container) throws IOException,
            HttpException {
        startMetric("containerExists");
        boolean result = super.containerExists(container);
        stopMetric("containerExists");
        return result;
    }

    @Override
    public FilesAccountInfo getAccountInfo() throws IOException, HttpException,
            FilesAuthorizationException, FilesException {
        startMetric("getAccountInfo");
        FilesAccountInfo result = super.getAccountInfo();
        stopMetric("getAccountInfo");
        return result;
    }

    @Override
    public FilesContainerInfo getContainerInfo(String container)
            throws IOException, HttpException, FilesException {
        startMetric("getContainerInfo");
        FilesContainerInfo result = super.getContainerInfo(container);
        stopMetric("getContainerInfo");
        return result;
    }

    @Override
    public void createContainer(String name) throws IOException, HttpException,
            FilesAuthorizationException, FilesException {
        startMetric("createContainer");
        super.createContainer(name);
        stopMetric("createContainer");
    }

    @Override
    public boolean deleteContainer(String name)
        throws IOException, HttpException, FilesAuthorizationException,
               FilesInvalidNameException, FilesNotFoundException,
               FilesContainerNotEmptyException {
        startMetric("deleteContainer");
        boolean result = super.deleteContainer(name);
        stopMetric("deleteContainer");
        return result;
    }

    @Override
    public String cdnEnableContainer(String name) throws IOException,
            HttpException, FilesException {
        startMetric("cdnEnableContainer");
        String result = super.cdnEnableContainer(name);
        stopMetric("cdnEnableContainer");
        return result;
    }

    @Override
    public String cdnUpdateContainer(String name,
                                     int ttl,
                                     boolean enabled,
                                     boolean retainLogs)
            throws IOException, HttpException, FilesException {
        startMetric("cdnUpdateContainer");
        String result = super.cdnUpdateContainer(name, ttl, enabled, retainLogs);
        stopMetric("cdnUpdateContainer");
        return result;
    }

    @Override
    public FilesCDNContainer getCDNContainerInfo(String container)
            throws IOException, HttpException, FilesException {
        startMetric("getCDNContainerInfo");
        FilesCDNContainer result = super.getCDNContainerInfo(container);
        stopMetric("getCDNContainerInfo");
        return result;
    }

    @Override
    public void createPath(String container, String path) throws HttpException,
            IOException, FilesException {
        startMetric("createPath");
        super.createPath(container, path);
        stopMetric("createPath");
    }

    @Override
    public void createFullPath(String container, String path)
            throws HttpException, IOException, FilesException {
        startMetric("createFullPath");
        super.createFullPath(container, path);
        stopMetric("createFullPath");
    }

    @Override
    public List<String> listCdnContainers(int limit) throws IOException,
            HttpException, FilesException {
        startMetric("listCdnContainers");
        List<String> result = super.listCdnContainers(limit);
        stopMetric("listCdnContainers");
        return result;
    }

    @Override
    public List<String> listCdnContainers() throws IOException, HttpException,
            FilesException {
        startMetric("listCdnContainers");
        List<String> result = super.listCdnContainers();
        stopMetric("listCdnContainers");
        return result;
    }

    @Override
    public List<String> listCdnContainers(int limit, String marker)
            throws IOException, HttpException, FilesException {
        startMetric("listCdnContainers");
        List<String> result = super.listCdnContainers(limit, marker);
        stopMetric("listCdnContainers");
        return result;
    }

    @Override
    public List<FilesCDNContainer> listCdnContainerInfo() throws IOException,
            HttpException, FilesException {
        startMetric("listCdnContainerInfo");
        List<FilesCDNContainer> result = super.listCdnContainerInfo();
        stopMetric("listCdnContainerInfo");
        return result;
    }

    @Override
    public List<FilesCDNContainer> listCdnContainerInfo(int limit)
            throws IOException, HttpException, FilesException {
        startMetric("listCdnContainerInfo");
        List<FilesCDNContainer> result = super.listCdnContainerInfo(limit);
        stopMetric("listCdnContainerInfo");
        return result;
    }

    @Override
    public List<FilesCDNContainer> listCdnContainerInfo(int limit, String marker)
            throws IOException, HttpException, FilesException {
        startMetric("listCdnContainerInfo");
        List<FilesCDNContainer> result =
                super.listCdnContainerInfo(limit, marker);
        stopMetric("listCdnContainerInfo");
        return result;
    }

    @Override
    public boolean storeObjectAs(String container,
                                 File obj,
                                 String contentType,
                                 String name) throws IOException,
            HttpException, FilesException {
        startMetric("storeObjectAs");
        boolean result = super.storeObjectAs(container, obj, contentType, name);
        stopMetric("storeObjectAs");
        return result;
    }

    @Override
    public boolean storeObjectAs(String container,
                                 File obj,
                                 String contentType,
                                 String name,
                                 IFilesTransferCallback callback)
            throws IOException, HttpException, FilesException {
        startMetric("storeObjectAs");
        boolean result =
                super
                        .storeObjectAs(container,
                                       obj,
                                       contentType,
                                       name,
                                       callback);
        stopMetric("storeObjectAs");
        return result;
    }

    @Override
    public boolean storeObjectAs(String container,
                                 File obj,
                                 String contentType,
                                 String name,
                                 Map<String, String> metadata)
            throws IOException, HttpException, FilesException {
        startMetric("storeObjectAs");
        boolean result =
                super
                        .storeObjectAs(container,
                                       obj,
                                       contentType,
                                       name,
                                       metadata);
        stopMetric("storeObjectAs");
        return result;
    }

    @Override
    public boolean storeObjectAs(String container,
                                 File obj,
                                 String contentType,
                                 String name,
                                 Map<String, String> metadata,
                                 IFilesTransferCallback callback)
            throws IOException, HttpException, FilesException {
        startMetric("storeObjectAs");
        boolean result =
                super.storeObjectAs(container,
                                    obj,
                                    contentType,
                                    name,
                                    metadata,
                                    callback);
        stopMetric("storeObjectAs");
        return result;
    }

    @Override
    public boolean storeObject(String container, File obj, String contentType)
            throws IOException, HttpException, FilesException {
        startMetric("storeObject");
        boolean result = super.storeObject(container, obj, contentType);
        stopMetric("storeObject");
        return result;
    }

    @Override
    public boolean storeObject(String container,
                               byte obj[],
                               String contentType,
                               String name,
                               Map<String, String> metadata)
            throws IOException, HttpException, FilesException {
        startMetric("storeObject");
        boolean result =
                super.storeObject(container, obj, contentType, name, metadata);
        stopMetric("storeObject");
        return result;
    }

    @Override
    public boolean storeObject(String container,
                               byte obj[],
                               String contentType,
                               String name,
                               Map<String, String> metadata,
                               IFilesTransferCallback callback)
            throws IOException, HttpException, FilesException {
        startMetric("storeObject");
        boolean result =
                super.storeObject(container,
                                  obj,
                                  contentType,
                                  name,
                                  metadata,
                                  callback);
        stopMetric("storeObject");
        return result;
    }

    @Override
    public boolean storeStreamedObject(String container,
                                       InputStream data,
                                       String contentType,
                                       String name,
                                       Map<String, String> metadata)
            throws IOException, HttpException, FilesException {
        startMetric("storeStreamedObject");
        boolean result =
                super.storeStreamedObject(container,
                                          data,
                                          contentType,
                                          name,
                                          metadata);
        stopMetric("storeStreamedObject");
        return result;
    }

    @Override
    public void deleteObject(String container, String objName)
            throws IOException, HttpException, FilesAuthorizationException,
            FilesInvalidNameException {
        startMetric("deleteObject");
        super.deleteObject(container, objName);
        stopMetric("deleteObject");
    }

    @Override
    public FilesObjectMetaData getObjectMetaData(String container,
                                                 String objName)
            throws IOException, HttpException, FilesAuthorizationException,
            FilesInvalidNameException {
        startMetric("getObjectMetaData");
        FilesObjectMetaData result =
                super.getObjectMetaData(container, objName);
        stopMetric("getObjectMetaData");
        return result;
    }

    @Override
    public byte[] getObject(String container, String objName)
            throws IOException, HttpException, FilesAuthorizationException,
            FilesInvalidNameException, FilesNotFoundException {
        startMetric("getObject");
        byte[] result = super.getObject(container, objName);
        stopMetric("getObject");
        return result;
    }

    @Override
    public InputStream getObjectAsStream(String container, String objName)
            throws IOException, HttpException, FilesAuthorizationException,
            FilesInvalidNameException {
        startMetric("getObjectAsStream");
        InputStream result = super.getObjectAsStream(container, objName);
        stopMetric("getObjectAsStream");
        return result;
    }

    @Override
    public int getConnectionTimeOut() {
        startMetric("getConnectionTimeOut");
        int result = super.getConnectionTimeOut();
        stopMetric("getConnectionTimeOut");
        return result;
    }

    @Override
    public void setConnectionTimeOut(int connectionTimeOut) {
        startMetric("setConnectionTimeOut");
        super.setConnectionTimeOut(connectionTimeOut);
        stopMetric("setConnectionTimeOut");
    }

    @Override
    public String getStorageURL() {
        startMetric("getStorageURL");
        String result = super.getStorageURL();
        stopMetric("getStorageURL");
        return result;
    }

    @Override
    public String getStorageToken() {
        startMetric("getStorageToken");
        String result = super.getStorageToken();
        stopMetric("getStorageToken");
        return result;
    }

    @Override
    public boolean isLoggedin() {
        startMetric("isLoggedin");
        boolean result = super.isLoggedin();
        stopMetric("isLoggedin");
        return result;
    }

    @Override
    public String getUserName() {
        startMetric("getUserName");
        String result = super.getUserName();
        stopMetric("getUserName");
        return result;
    }

    @Override
    public void setUserName(String userName) {
        startMetric("setUserName");
        super.setUserName(userName);
        stopMetric("setUserName");
    }

    @Override
    public String getPassword() {
        startMetric("getPassword");
        String result = super.getPassword();
        stopMetric("getPassword");
        return result;
    }

    @Override
    public void setPassword(String password) {
        startMetric("setPassword");
        super.setPassword(password);
        stopMetric("setPassword");
    }

    @Override
    public String getAuthenticationURL() {
        startMetric("getAuthenticationURL");
        String result = super.getAuthenticationURL();
        stopMetric("getAuthenticationURL");
        return result;
    }

    @Override
    public void setAuthenticationURL(String authenticationURL) {
        startMetric("setAuthenticationURL");
        super.setAuthenticationURL(authenticationURL);
        stopMetric("setAuthenticationURL");
    }

    @Override
    public boolean getUseETag() {
        startMetric("getUseETag");
        boolean result = super.getUseETag();
        stopMetric("getUseETag");
        return result;
    }

    @Override
    public void setUseETag(boolean useETag) {
        startMetric("setUseETag");
        super.setUseETag(useETag);
        stopMetric("setUseETag");
    }

    @Override
    public void setUserAgent(String userAgent) {
        // Not probed because needed in constructor before MetricsTable is available.
        //        startMetric("setUserAgent");
        super.setUserAgent(userAgent);
        //        stopMetric("setUserAgent");
    }

    @Override
    public String getUserAgent() {
        startMetric("getUserAgent");
        String result = super.getUserAgent();
        stopMetric("getUserAgent");
        return result;
    }

}
