/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.internal;

import org.duracloud.services.common.error.ServiceRuntimeException;
import org.duracloud.services.webapputil.tomcat.TomcatInstance;
import org.duracloud.services.webapputil.tomcat.TomcatUtil;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 5/16/11
 */
public class WebAppUtilImplTest {

    private WebAppUtilImpl webAppUtil;

    private TomcatUtil tomcatUtil;
    private File workDir;
    private InputStream war;

    @Before
    public void setUp() throws Exception {
        webAppUtil = new WebAppUtilImpl();

        tomcatUtil = EasyMock.createMock("TomcatUtil", TomcatUtil.class);
        workDir = new File("target", "test-webapputil");
        if (!workDir.exists()) {
            Assert.assertTrue(workDir.mkdir());
        }

        war = createInputStream();
    }

    @After
    public void tearDown() throws Exception {
        war.close();
        EasyMock.verify(tomcatUtil);
    }

    @Test
    public void testDeployPortBehavior() throws Exception {
        setMockExpectations();

        Integer port = 18080;
        String serviceId = "webapp-util";

        webAppUtil.setInitialPort(port);
        webAppUtil.setServiceId(serviceId);
        webAppUtil.setTomcatUtil(tomcatUtil);
        webAppUtil.setServiceWorkDir(workDir.getPath());

        // first deploy
        Integer portIndex = 5;
        URL url = webAppUtil.deploy(serviceId, portIndex.toString(), war);
        Assert.assertNotNull(url);
        Assert.assertEquals(port + portIndex, url.getPort());

        // second deploy, new port
        portIndex = 0;
        url = webAppUtil.deploy(serviceId, portIndex.toString(), war);
        Assert.assertNotNull(url);
        Assert.assertEquals(port + portIndex, url.getPort());

        // third deploy, repeat port
        boolean thrown = false;
        try {
            url = webAppUtil.deploy(serviceId, portIndex.toString(), war);
            Assert.fail("exception expected");

        } catch (ServiceRuntimeException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);

        // undeploy
        webAppUtil.unDeploy(url);

        // fourth deploy, repeat port
        portIndex = 0;
        url = webAppUtil.deploy(serviceId, portIndex.toString(), war);
        Assert.assertNotNull(url);
        Assert.assertEquals(port + portIndex, url.getPort());
    }

    private void setMockExpectations() {
        // set up tomcatInstance
        TomcatInstance tomcatInstance = EasyMock.createMock("TomcatInstance",
                                                            TomcatInstance.class);
        tomcatInstance.start(EasyMock.<Map<String, String>>anyObject());
        EasyMock.expectLastCall().times(3);

        tomcatInstance.deploy(EasyMock.<String>anyObject(),
                              EasyMock.<InputStream>anyObject());
        EasyMock.expectLastCall().times(3);

        EasyMock.replay(tomcatInstance);

        TomcatInstance tomcatInstance2 = EasyMock.createMock("TomcatInstance2",
                                                             TomcatInstance.class);

        // set up tomcatInstance, from undeploy
        tomcatInstance2.unDeploy(EasyMock.<String>anyObject());
        EasyMock.expectLastCall();

        tomcatInstance2.stop();
        EasyMock.expectLastCall();
        
        EasyMock.replay(tomcatInstance2);

        // set up tomcatUtil
        tomcatUtil.setResourceDir(workDir);
        EasyMock.expectLastCall().times(6);

        EasyMock.expect(tomcatUtil.installTomcat(EasyMock.isA(File.class),
                                                 EasyMock.anyInt())).andReturn(
            tomcatInstance).times(3);
        EasyMock.expect(tomcatUtil.getCatalinaHome(EasyMock.<File>anyObject()))
            .andReturn(null);
        EasyMock.expect(tomcatUtil.getTomcatInstance(EasyMock.<File>anyObject(),
                                                     EasyMock.anyInt()))
            .andReturn(tomcatInstance2);

        tomcatUtil.unInstallTomcat(tomcatInstance2);
        EasyMock.expectLastCall();

        EasyMock.replay(tomcatUtil);
    }

    private InputStream createInputStream() {
        return new ByteArrayInputStream("war".getBytes());
    }
}
