/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.image.osgi;

import static junit.framework.Assert.assertNotNull;
import org.apache.commons.io.FileUtils;
import org.duracloud.services.ComputeService;
import org.duracloud.services.image.ImageConversionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Tests the Image Conversion Service. Note that this test requires that
 * ImageMagick be installed on the host machine. This is why the test is
 * not included in the standard build.
 *
 * To run this test use: mvn clean install -P profile-systest
 *
 * @author Bill Branan
 *         Date: Jan 27, 2010
 */
public class NotRunTestServices extends AbstractDuracloudOSGiTestBasePax {

    private final Logger log = LoggerFactory.getLogger(NotRunTestServices.class);

    private final int MAX_TRIES = 10;

    private ImageConversionService service;

    @Before
    public void setUp() throws Exception {
        String workDir = "target/image-conversion-test";
        File serviceWorkDir = new File(workDir);
        serviceWorkDir.mkdirs();

        getService().setServiceWorkDir(workDir);
    }

    @After
    public void tearDown() throws Exception {
        File workDir = new File(getService().getServiceWorkDir());
        FileUtils.deleteDirectory(workDir);
    }

    @Test
    public void testImageConversionService() throws Exception {
        log.debug("testing ImageConversionService");

        ImageConversionServiceTester tester =
            new ImageConversionServiceTester(getService());
        tester.testImageConversionService();
    }

    protected Object getService(String serviceInterface) throws Exception {
        return getService(serviceInterface, null);
    }

    private Object getService(String serviceInterface, String filter)
        throws Exception {
        ServiceReference[] refs = getBundleContext().getServiceReferences(
            serviceInterface,
            filter);

        int count = 0;
        while ((refs == null || refs.length == 0) && count < MAX_TRIES) {
            count++;
            log.debug("Trying to find service: '" + serviceInterface + "'");
            Thread.sleep(1000);
            refs = getBundleContext().getServiceReferences(serviceInterface,
                                                           filter);
        }
        assertNotNull("service not found: " + serviceInterface, refs[0]);
        log.debug(getPropsText(refs[0]));
        return getBundleContext().getService(refs[0]);
    }

    private String getPropsText(ServiceReference ref) {
        StringBuilder sb = new StringBuilder("properties:");
        for (String key : ref.getPropertyKeys()) {
            sb.append("\tprop: [" + key);
            sb.append(":" + ref.getProperty(key) + "]\n");
        }
        return sb.toString();
    }

    public ImageConversionService getService() throws Exception {
        if (service == null) {
            service =
                (ImageConversionService) getService(ComputeService.class.getName(),
                                                    "(duraService=imageconversion)");
        }
        assertNotNull(service);
        return service;
    }

}