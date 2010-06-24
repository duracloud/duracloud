/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.DuraCloudUserType;
import org.duracloud.duraservice.config.DuraServiceConfig;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.Assert;

/**
 * @author Bill Branan
 */
public class RestTestHelper {

    private static String PROJECT_VERSION_PROP = "PROJECT_VERSION";

    private static String configFileName = "test-duraservice.properties";

    private static RestHttpHelper restHelper = getAuthorizedRestHelper();

    private static String baseServiceUrl;
    private static String baseStoreUrl;

    private static String host = "http://localhost";

    private static String port;
    private static final String defaultPort = "8080";

    private static String serviceContext = "duraservice";
    private static String storeContext = "durastore";

    private static String initXml = null;

    public static final String SPACE_ACCESS = "OPEN";

    public static HttpResponse initialize() throws Exception {
        String url = getBaseServiceUrl() + "/services";
        if(initXml == null) {
            initXml = buildTestInitXml();
        }
        return restHelper.post(url, initXml, null);
    }

    public static String getBaseServiceUrl() throws Exception {
        if (baseServiceUrl == null) {
            baseServiceUrl = host + ":" + getPort() + "/" + serviceContext;
        }
        return baseServiceUrl;
    }

    public static String getBaseStoreUrl() throws Exception {
        if (baseStoreUrl == null) {
            baseStoreUrl = host + ":" + getPort() + "/" + storeContext;
        }
        return baseStoreUrl;
    }

    private static String getPort() throws Exception {
        DuraServiceConfig config = new DuraServiceConfig();
        config.setConfigFileName(configFileName);

        if (port == null) {
            port = config.getPort();
        }

        try { // Ensure the port is a valid port value
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            port = defaultPort;
        }

        return port;
    }

    public static String buildTestInitXml() throws Exception {
        String primaryHost = "localhost";
        String primaryServicesAdminPort = "8089";
        String primaryServicesAdminContext =
            "org.duracloud.services.admin_" + getVersion().replace("-", ".");

        String userStorageHost = "localhost";
        String userStoragePort = getPort();
        String userStorageContext = "durastore";
        String userMsgBrokerURL = "tcp://localhost:61617";

        String serviceStorageHost = "localhost";
        String serviceStoragePort = getPort();
        String serviceStorageContext = "durastore";
        String serviceStorageSpaceId =
            "duracloud-" + getVersion().toLowerCase() + "-service-repo";

        String serviceComputeMgrType = "AMAZON_EC2";
        String serviceComputeMgrImage = "1234";
        String serviceComputeMgrUser = getRootCredential().getUsername();
        String serviceComputeMgrPass = getRootCredential().getPassword();

        StringBuilder xml = new StringBuilder();
        xml.append("<servicesConfig>");
          xml.append("<primaryServiceInstance>");
          xml.append("<host>"+primaryHost+"</host>");
          xml.append("<servicesAdminPort>"+primaryServicesAdminPort +
              "</servicesAdminPort>");
          xml.append("<servicesAdminContext>"+primaryServicesAdminContext +
              "</servicesAdminContext>");
          xml.append("</primaryServiceInstance>");
          xml.append("<userStorage>");
            xml.append("<host>"+userStorageHost+"</host>");
            xml.append("<port>"+userStoragePort+"</port>");
            xml.append("<context>"+userStorageContext+"</context>");
            xml.append("<msgBrokerUrl>"+userMsgBrokerURL+"</msgBrokerUrl>");
          xml.append("</userStorage>");
          xml.append("<serviceStorage>");
            xml.append("<host>"+serviceStorageHost+"</host>");
            xml.append("<port>"+serviceStoragePort+"</port>");
            xml.append("<context>"+serviceStorageContext+"</context>");
            xml.append("<spaceId>"+serviceStorageSpaceId+"</spaceId>");
          xml.append("</serviceStorage>");
          xml.append("<serviceCompute>");
            xml.append("<type>"+serviceComputeMgrType+"</type>");
            xml.append("<imageId>"+serviceComputeMgrImage+"</imageId>");
            xml.append("<computeProviderCredential>");
              xml.append("<username>"+serviceComputeMgrUser+"</username>");
              xml.append("<password>"+serviceComputeMgrPass+"</password>");
            xml.append("</computeProviderCredential>");
          xml.append("</serviceCompute>");
        xml.append("</servicesConfig>");
        return xml.toString();
    }

    private static String getVersion() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return version;
    }

    public static RestHttpHelper getAuthorizedRestHelper() {
        return new RestHttpHelper(getRootCredential());
    }

    private static Credential getRootCredential() {
        UnitTestDatabaseUtil dbUtil = null;
        try {
            dbUtil = new UnitTestDatabaseUtil();
        } catch (Exception e) {
            System.err.println("ERROR from unitTestDB: " + e.getMessage());
        }

        Credential rootCredential = null;
        try {
            ResourceType rootUser = ResourceType.fromDuraCloudUserType(
                DuraCloudUserType.ROOT);
            rootCredential = dbUtil.findCredentialForResource(rootUser);
        } catch (Exception e) {
            System.err.println("ERROR getting credential: " + e.getMessage());

        }
        return rootCredential;
    }

}
