/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.cloudsync;

import org.apache.commons.io.FilenameUtils;
import org.duracloud.common.web.NetworkUtil;
import org.duracloud.services.webapputil.WebAppUtil;
import org.duracloud.services.webapputil.tomcat.TomcatUtil;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.services.ComputeService.ServiceStatus;

/**
 * @author Andrew Woods
 *         Date: 9/20/11
 */
public class CloudSyncWebappWrapperTest {

    private TomcatUtil tomcatUtil;
    private WebAppUtil webAppUtil;
    private NetworkUtil networkUtil;
    private CloudSyncInstallHelper installHelper;

    private final File workDir = new File("target");

    private final String warName = "cloudsync";
    private final String portIndex = "1";

    @Before
    public void setUp() throws Exception {
        tomcatUtil = EasyMock.createMock("TomcatUtil", TomcatUtil.class);
        webAppUtil = EasyMock.createMock("WebAppUtil", WebAppUtil.class);
        networkUtil = EasyMock.createMock("NetworkUtil", NetworkUtil.class);
        installHelper = EasyMock.createMock("InstallHelper",
                                            CloudSyncInstallHelper.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(tomcatUtil, webAppUtil, networkUtil, installHelper);
    }

    private void replayMocks() {
        EasyMock.replay(tomcatUtil, webAppUtil, networkUtil, installHelper);
    }

    private CloudSyncWebappWrapper newCloudSyncWebappWrapper() {
        CloudSyncWebappWrapper cloudSync = new CloudSyncWebappWrapper();

        cloudSync.setServiceWorkDir(workDir.getPath());
        cloudSync.setWarName(warName + ".war");
        cloudSync.setWebappUtil(webAppUtil);
        cloudSync.setNetworkUtil(networkUtil);
        cloudSync.setInstallHelper(installHelper);
        cloudSync.setPortIndex(portIndex);

        return cloudSync;
    }

    @Test
    public void testStart() throws Exception {
        createStartMocks();
        replayMocks();

        CloudSyncWebappWrapper cloudSync = newCloudSyncWebappWrapper();
        cloudSync.start();
    }

    private void createStartMocks() throws Exception {
        File warFile = File.createTempFile("test-cloudsync-wrapper", ".war");
        String warFileName = FilenameUtils.getBaseName(warFile.getName());

        URL url = new URL("http://cloudsync.com");
        EasyMock.expect(webAppUtil.deploy(EasyMock.eq(warFileName),
                                          EasyMock.eq(portIndex),
                                          EasyMock.<InputStream>anyObject(),
                                          EasyMock.<Map<String, String>>anyObject()))
            .andReturn(url);

        networkUtil.waitForStartup(url.toString());
        EasyMock.expectLastCall();

        Map<String, String> env = new HashMap<String, String>();
        EasyMock.expect(installHelper.getInstallEnv()).andReturn(env);

        EasyMock.expect(installHelper.getWarFile(warName + ".war")).andReturn(
            warFile);
    }

    @Test
    public void testStop() throws Exception {
        String url = "https://cloudsync";
        createStopMocks(url);
        replayMocks();

        CloudSyncWebappWrapper cloudSync = newCloudSyncWebappWrapper();
        cloudSync.setUrl(url);
        cloudSync.stop();
    }

    private void createStopMocks(String urlName) throws Exception {
        URL url = new URL(urlName);

        webAppUtil.unDeploy(url);
        EasyMock.expectLastCall();

        networkUtil.waitForShutdown(urlName);
        EasyMock.expectLastCall();
    }

    @Test
    public void testGetServiceProps() {
        String url = "http://duracloud/cloudsync";
        replayMocks();

        CloudSyncWebappWrapper cloudSync = newCloudSyncWebappWrapper();
        cloudSync.setUrl(url);

        Map<String, String> props = cloudSync.getServiceProps();
        Assert.assertNotNull(props);

        Assert.assertEquals(3, props.size());

        String statusProp = props.get("Service Status");
        String urlProp = props.get("url");

        Assert.assertNotNull(statusProp);
        Assert.assertNotNull(urlProp);

        Assert.assertEquals(ServiceStatus.INSTALLED.name(), statusProp);
        Assert.assertEquals(url + "/login", urlProp);
    }

}
