/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ec2typicacomputeprovider.mgmt;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.duracloud.ec2typicacomputeprovider.mgmt.EC2ComputeProviderProperties;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class EC2ComputeProviderPropertiesTest {

    private EC2ComputeProviderProperties props;

    private String content;

    private final String provider = "test-amazon-provider";

    private final String signatureMethod = "test-signature-method";

    private final String keyname = "test-keypair";

    private final String imageId = "test-image-id";

    private final int minCount = 3;

    private final int maxCount = 4;

    private final int maxAsyncThreads = 123;

    private final String protocol = "http";

    private final int port = 8080;

    private final String appname = "test-app-name";

    @Before
    public void setUp() throws Exception {
        props = new EC2ComputeProviderProperties();
        populateContent();
    }

    private void populateContent() throws Exception {
        InputStream in =
                EC2ComputeProviderPropertiesTest.class.getClassLoader()
                        .getResourceAsStream("testEC2Config.properties");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        content = new String(out.toByteArray());
    }

    @After
    public void tearDown() throws Exception {
        props = null;
        content = null;
    }

    @Test
    public void testStore() throws Exception
    {
        props.loadFromXml(content);

        String xml = props.getAsXml();
        assertNotNull(xml);
        assertTrue(xml.indexOf(provider) > 0);
        assertTrue(xml.indexOf(signatureMethod) > 0);
        assertTrue(xml.indexOf(keyname) > 0);
        assertTrue(xml.indexOf(imageId) > 0);
        assertTrue(xml.indexOf(Integer.toString(minCount)) > 0);
        assertTrue(xml.indexOf(Integer.toString(maxCount)) > 0);
        assertTrue(xml.indexOf(Integer.toString(maxAsyncThreads)) > 0);
        assertTrue(xml.indexOf(protocol) > 0);
        assertTrue(xml.indexOf(Integer.toString(port)) > 0);
        assertTrue(xml.indexOf(appname) > 0);

    }

    @Test
    public void testLoad() throws Exception {
        assertNotNull(content);
        props.loadFromXml(content);

        String pvdr = props.getProvider();
        String sig = props.getSignatureMethod();
        String key = props.getKeyname();
        String img = props.getImageId();
        int minCnt = props.getMinInstanceCount();
        int maxCnt = props.getMaxInstanceCount();
        int threads = props.getMaxAsyncThreads();
        String proto = props.getWebappProtocol();
        int prt = props.getWebappPort();
        String app = props.getWebappName();

        assertNotNull(pvdr);
        assertTrue(pvdr.equals(provider));

        assertNotNull(sig);
        assertTrue(sig.equals(signatureMethod));

        assertNotNull(key);
        assertTrue(key.equals(keyname));

        assertNotNull(img);
        assertTrue(img.equals(imageId));

        assertTrue(minCnt == minCount);
        assertTrue(maxCnt == maxCount);
        assertTrue(threads == maxAsyncThreads);

        assertNotNull(proto);
        assertTrue(proto.equals(protocol));

        assertTrue(prt == port);

        assertNotNull(app);
        assertTrue(app.equals(appname));

    }

}
