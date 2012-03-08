/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.handler;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.domain.Content;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.easymock.Capture;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: 3/7/12
 */
public class MediaStreamingHandlerTest {

    private MediaStreamingHandler handler;
    private ContentStoreManager storeMgr;
    private ServicesManager servicesMar;
    private ContentStore contentStore;

    private int serviceId = 5;
    private String configVersion = "config-version";
    private String hostname = "host-name";
    private int depId = 7;
    private String spaceId = "space-id";

    @Before
    public void setup() throws Exception {
        handler = new MediaStreamingHandler();

        storeMgr = EasyMock.createMock("ContentStoreManager",
                                       ContentStoreManager.class);
        servicesMar = EasyMock.createMock("ServicesManager",
                                           ServicesManager.class);
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);

        EasyMock.expect(storeMgr.getPrimaryContentStore())
                .andReturn(contentStore)
                .anyTimes();

        EasyMock.expect(contentStore.getSpaceProperties(
            MediaStreamingHandler.HANDLER_STATE_SPACE))
                .andReturn(null);
    }

    private void replayMocks() {
        EasyMock.replay(storeMgr, servicesMar, contentStore);

        handler.initialize(storeMgr, servicesMar);
    }

    @After
    public void teardown() {
        EasyMock.verify(storeMgr, servicesMar, contentStore);
    }

    @Test
    public void testGetName() {
        replayMocks();

        String name = handler.getName();
        assertNotNull(name);
        assertEquals(MediaStreamingHandler.HANDLER_NAME, name);
    }

    @Test
    public void testStartInitial() throws Exception {
        testStart(false);
    }

    @Test
    public void testStartState() throws Exception {
        testStart(true);
    }

    private void testStart(boolean state) throws Exception {
        List<UserConfigModeSet> userConfig = createConfig(createOption(false));
        List<ServiceInfo> services =
            createServiceList(createService(userConfig));

        EasyMock.expect(servicesMar.getAvailableServices())
                .andReturn(services);
        EasyMock.expect(servicesMar
                .deployService(serviceId, hostname, configVersion, userConfig))
                .andReturn(depId);

        setUpGetState(state);
        replayMocks();

        handler.start();
        String status = handler.getStatus();
        assertNotNull(status);
        if(state) {
            assertEquals(status, "Media Streamer: Started. Spaces Streamed: 1");
        } else {
            assertEquals(status, "Media Streamer: Started. Spaces Streamed: 0");
        }
    }

    private void setUpGetState(boolean state) throws Exception {
        Content content = createContent(state);
        EasyMock.expect(contentStore.getContent(
            MediaStreamingHandler.HANDLER_STATE_SPACE,
            MediaStreamingHandler.HANDLER_STATE_FILE))
                .andReturn(content);
    }

    private Content createContent(boolean state) throws Exception {
        Map<String, String> contentMap = new HashMap<String, String>();
        if(state) {
            contentMap.put(spaceId, Boolean.TRUE.toString());
        }
        String contentXml = SerializationUtil.serializeMap(contentMap);
        Content content = new Content();
        content.setStream(IOUtil.writeStringToStream(contentXml));
        return content;
    }

    private Capture<InputStream> setUpStoreState() throws Exception {
        Capture<InputStream> streamCapture = new Capture<InputStream>();
        EasyMock.expect(contentStore.addContent(
            EasyMock.eq(MediaStreamingHandler.HANDLER_STATE_SPACE),
            EasyMock.eq(MediaStreamingHandler.HANDLER_STATE_FILE),
            EasyMock.capture(streamCapture),
            EasyMock.anyLong(),
            EasyMock.eq("text/xml"),
            EasyMock.isA(String.class),
            EasyMock.<Map<String, String>>isNull()))
                .andReturn(null);
        return streamCapture;
    }

    private List<ServiceInfo> createServiceList(ServiceInfo service) {
        List<ServiceInfo> services = new ArrayList<ServiceInfo>();
        services.add(service);
        return services;
    }

    private ServiceInfo createService(List<UserConfigModeSet> userConfig) {
        ServiceInfo service = new ServiceInfo();
        service.setId(serviceId);
        service.setUserConfigVersion(configVersion);
        service.setDisplayName(MediaStreamingHandler.MEDIA_STREAMER_NAME);

        service.setUserConfigModeSets(userConfig);

        List<DeploymentOption> depOptions = new ArrayList<DeploymentOption>();
        DeploymentOption depOption = new DeploymentOption();
        depOption.setHostname(hostname);
        depOptions.add(depOption);
        service.setDeploymentOptions(depOptions);

        return service;
    }

    private List<UserConfigModeSet> createConfig(Option option) {
        List<UserConfigModeSet> userConfig = new ArrayList<UserConfigModeSet>();
        UserConfigModeSet modeSet = new UserConfigModeSet();
        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();
        UserConfigMode mode = new UserConfigMode();
        List<UserConfig> configs = new ArrayList<UserConfig>();

        List<Option> options = new ArrayList<Option>();
        options.add(option);

        String configName = MediaStreamingHandler.SOURCE_SPACE_ID;
        UserConfig config =
            new MultiSelectUserConfig(configName, "display-name", options);
        configs.add(config);
        mode.setUserConfigs(configs);
        modes.add(mode);
        modeSet.setModes(modes);
        userConfig.add(modeSet);

        return userConfig;
    }

    @Test
    public void testStop() throws Exception {
        List<UserConfigModeSet> userConfig = createConfig(createOption(true));
        ServiceInfo service = createService(userConfig);
        service.setDeployments(createDeployments(userConfig));
        List<ServiceInfo> services = createServiceList(service);

        EasyMock.expect(servicesMar.getDeployedServices())
                .andReturn(services);
        servicesMar.undeployService(serviceId, depId);
        EasyMock.expectLastCall();

        replayMocks();

        handler.stop();
        String status = handler.getStatus();
        assertNotNull(status);
        assertEquals(status, "Media Streamer: Stopped");
    }

    private List<Deployment> createDeployments(
        List<UserConfigModeSet> userConfig) {
        Deployment dep = new Deployment();
        dep.setId(depId);
        dep.setUserConfigModeSets(userConfig);

        List<Deployment> deployments = new ArrayList<Deployment>();
        deployments.add(dep);
        return deployments;
    }

    private Option createOption(boolean selected) {
        return new Option(spaceId, spaceId, selected);
    }

    /**
     * Tests the start streaming action when the given spaceId is included
     * as an option in the service.
     */
    @Test
    public void testPerformActionStart() throws Exception {
        Option spaceOption = createOption(false);
        setUpGetState(false);
        Capture<InputStream> streamCapture = setUpStoreState();
        setUpPerformAction(spaceOption);
        replayMocks();

        handler.performAction(MediaStreamingHandler.START_STREAMING, spaceId);

        String status = handler.getStatus();
        assertNotNull(status);
        assertEquals(status, "Media Streamer: Started. Spaces Streamed: 1");
        assertTrue(spaceOption.isSelected());

        verifyStreamAdd(streamCapture, spaceId);
    }

    /**
     * Tests the start streaming action when the given spaceId is not
     * included as an option in the service. This is likely to occur
     * when spaces are created after the service has started.
     */
    @Test
    public void testPerformActionStartMissingOption() throws Exception {
        String newSpaceId = "new-space-id";

        setUpGetState(false);
        Capture<InputStream> streamCapture = setUpStoreState();

        Option spaceOption = createOption(false);
        setUpPerformAction(spaceOption);

        EasyMock.expect(contentStore.getSpaceProperties(newSpaceId))
                .andReturn(null);

        replayMocks();

        handler.performAction(MediaStreamingHandler.START_STREAMING, newSpaceId);

        String status = handler.getStatus();
        assertNotNull(status);
        assertEquals(status, "Media Streamer: Started. Spaces Streamed: 1");
        assertFalse(spaceOption.isSelected());

        verifyStreamAdd(streamCapture, newSpaceId);
    }

    @Test
    public void testPerformActionStop() throws Exception {
        Option spaceOption = createOption(true);

        setUpGetState(true);
        Capture<InputStream> streamCapture = setUpStoreState();

        setUpPerformAction(spaceOption);
        replayMocks();

        handler.performAction(MediaStreamingHandler.STOP_STREAMING, spaceId);

        String status = handler.getStatus();
        assertNotNull(status);
        assertEquals(status, "Media Streamer: Started. Spaces Streamed: -1");
        assertFalse(spaceOption.isSelected());

        verifyStreamRemove(streamCapture);
    }

    private void setUpPerformAction(Option spaceOption) throws Exception {
        List<UserConfigModeSet> userConfig = createConfig(spaceOption);
        ServiceInfo service = createService(userConfig);
        service.setDeployments(createDeployments(userConfig));
        List<ServiceInfo> services = createServiceList(service);

        EasyMock.expect(servicesMar.getDeployedServices())
                .andReturn(services);
        servicesMar.updateServiceConfig(serviceId,
                                        depId,
                                        configVersion,
                                        userConfig);
        EasyMock.expectLastCall();
    }

    private void verifyStreamAdd(Capture<InputStream> streamCapture, String key)
        throws Exception {
        InputStream stream = streamCapture.getValue();
        Map<String, String> state = SerializationUtil.deserializeMap(
            IOUtil.readStringFromStream(stream));
        assertNotNull(state);
        assertEquals(1, state.size());
        assertTrue(state.containsKey(key));
    }

    private void verifyStreamRemove(Capture<InputStream> streamCapture)
        throws Exception {
        InputStream stream = streamCapture.getValue();
        Map<String, String> state = SerializationUtil.deserializeMap(
            IOUtil.readStringFromStream(stream));
        assertNotNull(state);
        assertEquals(0, state.size());
        assertFalse(state.containsKey(spaceId));
    }

}
