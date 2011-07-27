/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil;

import org.apache.commons.io.FileUtils;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.services.common.model.NamedFilterList;
import org.duracloud.services.webapputil.internal.WebAppUtilImpl;
import org.duracloud.services.webapputil.osgi.WebAppUtilTestBase;
import org.duracloud.services.webapputil.tomcat.TomcatUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Andrew Woods
 *         Date: Nov 30, 2009
 */
public class TestWebAppUtil extends WebAppUtilTestBase {

    private WebAppUtilImpl webappUtil;
    private String serviceWorkPath = "target/webapputil-test";
    private String testResourcesPath = "src/test/resources";
    private String serviceId = "hello";
    private String portIndex = "0";
    private String binariesName = "apache-tomcat-6.0.20.zip";
    private String warName = "hellowebapp-" + getVersion() + ".war";
    private int port = 18080;

    private String filterName1 = "filter1.txt";
    private String filterName2 = "filter2.txt";

    private static final String AMAZON_HOST_QUERY = "http://169.254.169.254/2009-04-04/meta-data/public-ipv4";

    @Before
    public void setUp() throws IOException {
        File serviceWorkDir = populateServiceWork();

        TomcatUtil tomcatUtil = new TomcatUtil();
        tomcatUtil.setBinariesZipName(binariesName);

        webappUtil = new WebAppUtilImpl();
        webappUtil.setServiceId(serviceId);
        webappUtil.setInitialPort(port);
        webappUtil.setServiceWorkDir(serviceWorkDir.getAbsolutePath());
        webappUtil.setTomcatUtil(tomcatUtil);

        war = new FileInputStream(new File(serviceWorkDir, warName));
    }

    private File populateServiceWork() throws IOException {
        File serviceWorkDir = new File(serviceWorkPath);
        serviceWorkDir.mkdirs();

        File testResources = new File(testResourcesPath);
        File binaries = new File(testResources, binariesName);
        File warFile = new File(testResources, warName);
        
        // Add two files to be filtered to the war file
        addFilesToWarAndCopy(warFile, serviceWorkDir);

        FileUtils.copyFileToDirectory(binaries, serviceWorkDir);

        return serviceWorkDir;
    }

    @After
    public void tearDown() {
        doTearDown(webappUtil);
    }

    @Test
    public void testDeploy() throws Exception {
        String portIndex = "0";
        url = webappUtil.deploy(serviceId, portIndex, war);
        Thread.sleep(3000);

        verifyDeployment(url, true);
    }

    @Test
    public void testFilteredDeploy() throws Exception {
        String host = getHost();
        Assert.assertNotNull("Unable to find host ip.", host);
        
        Map<String, String> env = new HashMap<String, String>();
        env.put("argEnv", "argVal");

        Map<String, String> filter1 = new HashMap<String, String>();
        filter1.put("$DURA_HOST$", "placeholder");
        NamedFilterList.NamedFilter namedFilter1 = new NamedFilterList.NamedFilter(
            filterName1,
            filter1);

        Map<String, String> filters2 = new HashMap<String, String>();
        filters2.put("$DURA_HOST$", host);
        NamedFilterList.NamedFilter namedFilter2 = new NamedFilterList.NamedFilter(
            filterName2,
            filters2);

        List<NamedFilterList.NamedFilter> filterList = new ArrayList<NamedFilterList.NamedFilter>();
        filterList.add(namedFilter1);
        filterList.add(namedFilter2);

        NamedFilterList filters = new NamedFilterList(filterList);

        url = webappUtil.filteredDeploy(serviceId,
                                        portIndex,
                                        war,
                                        env,
                                        filters);
        Thread.sleep(3000);

        verifyDeployment(url, true);

        File file = new File(webappUtil.getServiceWorkDir(), "tomcat");
        Assert.assertTrue("Path should exist:" + file.getAbsolutePath(),
                          file.exists());
        file = new File(file, serviceId + "-" + port);
        Assert.assertTrue("Path should exist:" + file.getAbsolutePath(),
                          file.exists());
        file = new File(file, "apache-tomcat-6.0.20");
        Assert.assertTrue("Path should exist:" + file.getAbsolutePath(),
                          file.exists());
        file = new File(file, "webapps");
        Assert.assertTrue("Path should exist:" + file.getAbsolutePath(),
                          file.exists());
        file = new File(file, "hello");
        Assert.assertTrue("Path should exist:" + file.getAbsolutePath(),
                          file.exists());

        File file1 = new File(file, filterName1);
        Assert.assertTrue("File 1 should exist:" + file1.getAbsolutePath(),
                          file1.exists());

        File file2 = new File(file, filterName2);
        Assert.assertTrue("File 2 should exist:" + file2.getAbsolutePath(),
                          file2.exists());

        verifyContains(file1, host);
        verifyContains(file2, host);
    }

    @Test
    public void testDeployEnv() throws Exception {
        Map<String, String> env = new HashMap<String, String>();
        env.put("argEnv", "argVal");
        url = webappUtil.deploy(serviceId, portIndex, war, env);
        Thread.sleep(3000);

        verifyDeployment(url, true);
    }

    @Test
    public void testUnDeploy() throws Exception {
        url = webappUtil.deploy(serviceId, portIndex, war);
        Thread.sleep(3000);
        verifyDeployment(url, true);

        webappUtil.unDeploy(url);
        verifyDeployment(url, false);
    }

    private String getHost() {
        String host = getAmazonHost();
        if (null != host) {
            return host;
        }

        host = getLocalHost();
        if (null != host) {
            return host;
        }
        return host;
    }

    private String getAmazonHost() {
        String host = null;
        RestHttpHelper httpHelper = new RestHttpHelper();
        try {
            RestHttpHelper.HttpResponse response = httpHelper.get(
                AMAZON_HOST_QUERY);
            if (null != response && response.getStatusCode() == 200) {
                host = response.getResponseBody();
            }
        } catch (Exception e) {
        }
        return host;
    }

    private String getLocalHost() {
        return "localhost";
    }

    private void verifyContains(File file, String text)
        throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        boolean found = false;
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(text)) {
                found = true;
            }
        }
        Assert.assertTrue(text, found);
    }

    private void addFilesToWarAndCopy(File warFile, File copyToDir) throws IOException {
        File newWar = new File(copyToDir, warFile.getName());

		ZipInputStream zin = new ZipInputStream(new FileInputStream(warFile));
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(newWar));

        int len;
        byte[] buf = new byte[1024];

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			out.putNextEntry(new ZipEntry(entry.getName()));

            while ((len = zin.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
			entry = zin.getNextEntry();
		}
		zin.close();

        String value = "$DURA_HOST$:$DURA_PORT$";
        buf = value.getBytes();

        out.putNextEntry(new ZipEntry(filterName1));
        out.write(buf, 0, buf.length);
        out.closeEntry();

        out.putNextEntry(new ZipEntry(filterName2));
        out.write(buf, 0, buf.length);
        out.closeEntry();

        out.close();
    }
}
