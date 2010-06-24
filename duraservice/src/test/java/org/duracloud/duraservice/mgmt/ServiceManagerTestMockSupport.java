/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.mgmt;

import org.duracloud.common.model.Credential;
import org.duracloud.domain.Content;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServicesConfigDocument;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStore;
import org.duracloud.duraservice.domain.Store;
import org.duracloud.duraservice.domain.UserStore;
import org.duracloud.duraservice.domain.ServiceComputeInstance;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.servicesadminclient.ServicesAdminClient;
import org.easymock.classextension.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


/**
 * Mock support for ServiceManagerTest
 *
 * @author Bill Branan
 */
public class ServiceManagerTestMockSupport {

    private static final String PROJECT_VERSION_PROP = "PROJECT_VERSION";

    protected static final String USER_SPACE_1 = "space1";
    protected static final String USER_SPACE_2 = "space2";
    protected static final String USER_CONTENT_STORE = "content-store";

    private final String configFileName = getServiceSpaceId() + ".xml";
    private final String serviceId1 = "service1.zip";
    private final String serviceId2 = "service2.zip";
    private final String config1Name = "config1";
    private final String config2Name = "config2";
    private final String config3Name = "config3";
    private final String config1Value = "Config Value";

    private String getServiceSpaceId() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return "duracloud-" + version.toLowerCase() + "-service-repo";
    }

    public Content getServiceInfoFile() throws Exception {
        List<ServiceInfo> services = new ArrayList<ServiceInfo>();
        services.add(getServiceInfo(1));
        services.add(getServiceInfo(2));
        services.add(getServiceInfo(3));

        String xml = ServicesConfigDocument.getServiceListAsXML(services);

        Content content = new Content();
        content.setStream(new ByteArrayInputStream(xml.getBytes()));
        return content;
    }

    public ServiceInfo getServiceInfo(int id) {
        if (id == 1) {
            return buildService1();
        } else if (id == 2) {
            return buildService2();
        } else if (id == 3) {
            return buildService3();
        } else {
            throw new RuntimeException("Unexpected id: " + id);
        }
    }

    private ServiceInfo buildService1() {
        ServiceInfo service1 = new ServiceInfo();
        service1.setId(1);
        service1.setContentId("service1.zip");
        service1.setDescription("Service 1 Description");
        service1.setDisplayName("Service 1");
        service1.setUserConfigVersion("1.0");
        service1.setMaxDeploymentsAllowed(-1);

        List<UserConfig> service1UserConfig = new ArrayList<UserConfig>();
        TextUserConfig config1 = new TextUserConfig(config1Name,
                                                    "Config 1",
                                                    config1Value);

        List<Option> config2ops = new ArrayList<Option>();
        Option config2op1 = new Option("Config 2 Option 1",
                                       "config2op1",
                                       false);
        Option config2op2 = new Option("Config 2 Option 2",
                                       "config2op2",
                                       false);
        config2ops.add(config2op1);
        config2ops.add(config2op2);
        SingleSelectUserConfig config2 = new SingleSelectUserConfig(config2Name,
                                                                    "Config 2",
                                                                    config2ops);

        List<Option> config3ops = new ArrayList<Option>();
        Option config3op1 = new Option("Config 3 Option 1",
                                       "config3op1",
                                       false);
        Option config3op2 = new Option("Config 3 Option 2",
                                       "config3op2",
                                       false);
        Option config3op3 = new Option("Config 3 Option 3",
                                       "config3op3",
                                       false);
        config3ops.add(config3op1);
        config3ops.add(config3op2);
        config3ops.add(config3op3);
        MultiSelectUserConfig config3 = new MultiSelectUserConfig(config3Name,
                                                                  "Config 3",
                                                                  config3ops);

        service1UserConfig.add(config1);
        service1UserConfig.add(config2);
        service1UserConfig.add(config3);

        service1.setUserConfigs(service1UserConfig);

        List<SystemConfig> systemConfig = new ArrayList<SystemConfig>();
        systemConfig.add(new SystemConfig("sysConfig1", null, "default"));
        service1.setSystemConfigs(systemConfig);

        DeploymentOption depOp = new DeploymentOption();
        depOp.setLocation(DeploymentOption.Location.PRIMARY);
        depOp.setState(DeploymentOption.State.AVAILABLE);
        List<DeploymentOption> depOptions = new ArrayList<DeploymentOption>();
        depOptions.add(depOp);

        service1.setDeploymentOptions(depOptions);
        return service1;
    }

    private ServiceInfo buildService2() {
        ServiceInfo service2 = new ServiceInfo();
        service2.setId(2);
        service2.setContentId("service2.zip");
        service2.setDescription("Service 2 Description");
        service2.setDisplayName("Service 2");
        service2.setUserConfigVersion("1.0");
        service2.setMaxDeploymentsAllowed(-1);

        List<UserConfig> service2UserConfig = new ArrayList<UserConfig>();
        TextUserConfig config1 = new TextUserConfig("config1",
                                                    "Config 1",
                                                    "Config Value");

        List<Option> config2ops = new ArrayList<Option>();
        Option config2op = new Option("Stores",
                                      ServiceConfigUtil.STORES_VAR,
                                      false);
        config2ops.add(config2op);
        SingleSelectUserConfig config2 = new SingleSelectUserConfig("config2",
                                                                    "Config 2",
                                                                    config2ops);

        List<Option> config3ops = new ArrayList<Option>();
        Option config3op = new Option("Spaces",
                                      ServiceConfigUtil.SPACES_VAR,
                                      false);
        config3ops.add(config3op);
        MultiSelectUserConfig config3 = new MultiSelectUserConfig("config3",
                                                                  "Config 3",
                                                                  config3ops);

        service2UserConfig.add(config1);
        service2UserConfig.add(config2);
        service2UserConfig.add(config3);

        service2.setUserConfigs(service2UserConfig);

        List<SystemConfig> systemConfig = new ArrayList<SystemConfig>();
        systemConfig.add(new SystemConfig("sysConfig1",
                                          "$DURASTORE-HOST",
                                          "default"));

        service2.setSystemConfigs(systemConfig);

        DeploymentOption depOp1 = new DeploymentOption();
        depOp1.setLocation(DeploymentOption.Location.PRIMARY);
        depOp1.setState(DeploymentOption.State.UNAVAILABLE);
        DeploymentOption depOp2 = new DeploymentOption();
        depOp2.setLocation(DeploymentOption.Location.NEW);
        depOp2.setState(DeploymentOption.State.AVAILABLE);
        List<DeploymentOption> depOptions = new ArrayList<DeploymentOption>();
        depOptions.add(depOp1);
        depOptions.add(depOp2);

        service2.setDeploymentOptions(depOptions);
        return service2;
    }

    private ServiceInfo buildService3() {
        ServiceInfo service3 = new ServiceInfo();
        service3.setId(3);
        service3.setContentId("service3.zip");
        service3.setDescription("Service 3 Description");
        service3.setDisplayName("Service 3");
        service3.setUserConfigVersion("1.0");
        service3.setMaxDeploymentsAllowed(-1);

        List<UserConfig> service3UserConfig = new ArrayList<UserConfig>();
        service3.setUserConfigs(service3UserConfig);

        DeploymentOption depOp1 = new DeploymentOption();
        depOp1.setLocation(DeploymentOption.Location.PRIMARY);
        depOp1.setState(DeploymentOption.State.UNAVAILABLE);
        DeploymentOption depOp2 = new DeploymentOption();
        depOp2.setLocation(DeploymentOption.Location.NEW);
        depOp2.setState(DeploymentOption.State.UNAVAILABLE);
        DeploymentOption depOp3 = new DeploymentOption();
        depOp3.setLocation(DeploymentOption.Location.EXISTING);
        depOp3.setState(DeploymentOption.State.UNAVAILABLE);
        List<DeploymentOption> depOptions = new ArrayList<DeploymentOption>();
        depOptions.add(depOp1);
        depOptions.add(depOp2);
        depOptions.add(depOp3);

        service3.setDeploymentOptions(depOptions);
        return service3;
    }

    /**
     * Mock ServiceComputeInstanceUtil
     */
    protected ServiceComputeInstanceUtil createMockServiceComputeInstanceUtil()
        throws Exception {
        RestHttpHelper.HttpResponse response = EasyMock.createMock(
            RestHttpHelper.HttpResponse.class);
        EasyMock.expect(response.getStatusCode()).andReturn(200).anyTimes();
        EasyMock.expect(response.getResponseBody()).andReturn("").anyTimes();
        EasyMock.replay(response);

        ServicesAdminClient adminClient = EasyMock.createMock(
            ServicesAdminClient.class);
        EasyMock.expect(adminClient.postServiceBundle(EasyMock.isA(String.class),
                                                      EasyMock.isA(InputStream.class),
                                                      EasyMock.anyLong()))
            .andReturn(response)
            .anyTimes();
        EasyMock.expect(adminClient.isServiceDeployed(EasyMock.isA(String.class)))
            .andReturn(true)
            .anyTimes();
        EasyMock.expect(adminClient.postServiceConfig(EasyMock.isA(String.class),
                                                      EasyMock.isA(Map.class)))
            .andReturn(response)
            .anyTimes();
        EasyMock.expect(adminClient.startServiceBundle(EasyMock.isA(String.class)))
            .andReturn(response)
            .anyTimes();

        EasyMock.expect(adminClient.getServiceConfig(EasyMock.isA(String.class)))
            .andStubAnswer(new ServiceConfigAnswer());

        Map<String, String> props = new HashMap<String, String>();
        EasyMock.expect(adminClient.getServiceProps(EasyMock.isA(String.class)))
            .andReturn(props)
            .anyTimes();
        EasyMock.expect(adminClient.stopServiceBundle(EasyMock.isA(String.class)))
            .andReturn(response)
            .anyTimes();
        EasyMock.expect(adminClient.deleteServiceBundle(EasyMock.isA(String.class)))
            .andReturn(response)
            .anyTimes();

        EasyMock.replay(adminClient);


        ServiceComputeInstanceUtil util = EasyMock.createMock(
            ServiceComputeInstanceUtil.class);
        ServiceComputeInstance instance = new ServiceComputeInstance("localhost",
                                                                     "",
                                                                     adminClient);
        EasyMock.expect(util.createComputeInstance(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
            .andReturn(instance)
            .anyTimes();
        EasyMock.replay(util);

        return util;
    }

    /**
     * Mock ContentStoreManagerUtil
     */
    protected ContentStoreManagerUtil createMockContentStoreManagerUtil()
        throws Exception {
        ContentStoreManager storeMgr = createMockContentStoreMgr();
        ContentStoreManagerUtil util = EasyMock.createMock(
            ContentStoreManagerUtil.class);
        EasyMock.expect(util.getContentStoreManager(EasyMock.isA(Store.class),
                                                    EasyMock.isA(Credential.class)))
            .andReturn(storeMgr);
        EasyMock.replay(util);
        return util;
    }

    /**
     * Mock ContentStoreManager
     */
    private ContentStoreManager createMockContentStoreMgr() throws Exception {

        // Services contentStore
        ContentStore mockContentStore = EasyMock.createMock(ContentStore.class);

        // Service Config content
        EasyMock.expect(mockContentStore.getContent(getServiceSpaceId(),
                                                    configFileName)).andAnswer(
            new ServiceConfigContent()).
            anyTimes();

        // Service content
        Content serviceContent = new Content();
        serviceContent.setStream(new ByteArrayInputStream("hello".getBytes()));
        EasyMock.expect(mockContentStore.getContent(getServiceSpaceId(), serviceId1))
            .andReturn(serviceContent)
            .anyTimes();
        EasyMock.expect(mockContentStore.getContent(getServiceSpaceId(), serviceId2))
            .andReturn(serviceContent)
            .anyTimes();

        EasyMock.replay(mockContentStore);

        // User Content Store Manager Mock
        ContentStoreManager mockContentStoreMgr = EasyMock.createMock(
            ContentStoreManager.class);
        EasyMock.expect(mockContentStoreMgr.getPrimaryContentStore()).
            andReturn(mockContentStore).anyTimes();

        EasyMock.replay(mockContentStoreMgr);

        return mockContentStoreMgr;
    }

    private class ServiceConfigContent implements IAnswer<Content> {
        public Content answer() throws Throwable {
            return new ServiceManagerTestMockSupport().getServiceInfoFile();
        }
    }

    /**
     * Mock ServiceConfigUtil
     */
    protected ServiceConfigUtil createMockServiceConfigUtil() {
        ServiceConfigUtil util = EasyMock.createMock(ServiceConfigUtil.class);
        EasyMock.expect(util.populateService(EasyMock.isA(ServiceInfo.class),
                                             EasyMock.isA(List.class),
                                             EasyMock.isA(UserStore.class),
                                             EasyMock.isA(String.class)))
            .andStubAnswer(new ServiceInfoAnswer());
        EasyMock.expect(util.resolveSystemConfigVars(EasyMock.isA(UserStore.class),
                                                     EasyMock.isA(List.class)))
            .andReturn(new ArrayList<SystemConfig>())
            .anyTimes();
        EasyMock.replay(util);
        return util;
    }

    private class ServiceInfoAnswer implements IAnswer<ServiceInfo> {
        public ServiceInfo answer() throws Throwable {
            Object[] args = EasyMock.getCurrentArguments();
            Assert.assertNotNull(args);
            Assert.assertEquals(4, args.length);
            ServiceInfo arg = (ServiceInfo) args[0];

            // remove system-config
            arg.setSystemConfigs(null);

            List<UserConfig> userConfigs = arg.getUserConfigs();
            if (userConfigs != null) {

                List<UserConfig> newConfigs = new ArrayList<UserConfig>();
                for (UserConfig config : userConfigs) {

                    if (config.getName().equals("config2")) {
                        List<Option> newOptions = new ArrayList<Option>();
                        Option option1 = new Option(null,
                                                    USER_CONTENT_STORE,
                                                    true);
                        newOptions.add(option1);

                        SingleSelectUserConfig ssConfig = (SingleSelectUserConfig) config;
                        SingleSelectUserConfig newConfig = new SingleSelectUserConfig(
                            ssConfig.getName(),
                            ssConfig.getDisplayName(),
                            newOptions);

                        newConfigs.add(newConfig);

                    } else if (config.getName().equals("config3")) {

                        List<Option> newOptions = new ArrayList<Option>();
                        Option option1 = new Option(null, USER_SPACE_1, true);
                        Option option2 = new Option(null, USER_SPACE_2, true);
                        newOptions.add(option1);
                        newOptions.add(option2);

                        MultiSelectUserConfig msConfig = (MultiSelectUserConfig) config;
                        MultiSelectUserConfig newConfig = new MultiSelectUserConfig(
                            msConfig.getName(),
                            msConfig.getDisplayName(),
                            newOptions);

                        newConfigs.add(newConfig);

                    } else {
                        newConfigs.add(config);
                    }
                }
                arg.setUserConfigs(newConfigs);

            }
            return arg;
        }
    }

    private class ServiceConfigAnswer implements IAnswer<Map<String, String>> {
        public Map<String, String> answer() throws Throwable {
            Object[] args = EasyMock.getCurrentArguments();
            Assert.assertNotNull(args);
            Assert.assertEquals(1, args.length);

            Map<String, String> config = new HashMap<String, String>();
            config.put(config1Name, config1Value);
            config.put(config2Name, USER_CONTENT_STORE);
            config.put(config3Name, USER_SPACE_1 + "," + USER_SPACE_2);

            return config;
        }
    }

}
