/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ec2typicacomputeprovider.mgmt;

import java.io.FileInputStream;

import org.apache.commons.io.input.AutoCloseInputStream;

import org.duracloud.common.model.Credential;
import org.duracloud.computeprovider.mgmt.InstanceDescription;
import org.duracloud.ec2typicacomputeprovider.mgmt.EC2ComputeProvider;
import org.duracloud.ec2typicacomputeprovider.mgmt.EC2ComputeProviderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.Assert.assertNotNull;

public class EC2ComputeProviderDriver {

    protected final Logger log = LoggerFactory.getLogger(EC2ComputeProviderDriver.class);

    private EC2ComputeProvider service;

    private final String accessKeyId = "0YMHVZZZ5GP0P7VFJV82";

    private final String secretAccessKey;

    private Credential credential;

    private String instanceId;

    private final String configFilePath =
            "src/test/resources/test-service-props.xml";

    private String xmlProps;

    public EC2ComputeProviderDriver(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public void setUp() throws Exception {
        EC2ComputeProviderProperties props = new EC2ComputeProviderProperties();
        props
                .loadFromXmlStream(new AutoCloseInputStream(new FileInputStream(configFilePath)));
        xmlProps = props.getAsXml();

        service = new EC2ComputeProvider();

        credential = new Credential();
        credential.setUsername(accessKeyId);
        credential.setPassword(secretAccessKey);

    }

    public void testStart() throws Exception {
        instanceId = service.start(credential, xmlProps);

        log.info("instance id: " + instanceId);

        InstanceDescription desc = null;
        while (!service.isInstanceRunning(credential, instanceId, xmlProps)) {
            desc =
                    service.describeRunningInstance(credential,
                                                    instanceId,
                                                    xmlProps);
            log.info("instance state: " + desc.getState().name());
            Thread.sleep(10000);
        }
        desc =
                service.describeRunningInstance(credential,
                                                instanceId,
                                                xmlProps);
        assertNotNull(desc);
        log.info("instance state:     " + desc.getState().name());
        log.info("launch time: " + desc.getLaunchTime());
        log.info("provider:    " + desc.getProvider());
        log.info("url:         " + desc.getURL());

        boolean done = false;
        while (!done) {
            Thread.sleep(10000);
            try {
                done =
                        service.isWebappRunning(credential,
                                                instanceId,
                                                xmlProps);
                log.info("webapp state: " + done);
            } catch (Exception e) {
                e.printStackTrace();
                done = false;
            }
        }
        log.info("webapp is available at: \n\t" + desc.getURL());

    }

    public void testStop() throws Exception {
        log.info("stopping instance: " + instanceId);
        service.stop(credential, instanceId, xmlProps);
    }

    private static void usageAndQuit() {
        StringBuilder sb = new StringBuilder("Usage:\n");
        sb.append("\tOne argument is required: secretAccessKey\n\n");
        sb.append("\tjava EC2ServiceImplDriver secretAccessKey");
        System.err.println(sb.toString());
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length != 1) usageAndQuit();

        String secretAccessKey = args[0];
        EC2ComputeProviderDriver driver =
                new EC2ComputeProviderDriver(secretAccessKey);
        try {
            driver.setUp();

            driver.testStart();

            System.out.println();
            System.out.println("Press any key to terminate instance: "
                    + driver.instanceId);
            System.in.read();

            driver.testStop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
