/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ec2typicacomputeprovider.mgmt;

import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.List;

import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.ReservationDescription.Instance;

import org.apache.commons.io.input.AutoCloseInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.duracloud.common.model.Credential;
import org.duracloud.computeprovider.mgmt.InstanceDescription;
import org.duracloud.ec2typicacomputeprovider.mgmt.EC2ComputeProvider;
import org.duracloud.ec2typicacomputeprovider.mgmt.EC2ComputeProviderProperties;
import org.easymock.classextension.EasyMock;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class EC2ComputeProviderTest {

    private EC2ComputeProvider serviceProvider;

    private Credential credential;

    private final String username = "username";

    private final String password = "password";

    private Jec2 mockEC2;

    private LaunchConfiguration mockRunRequest;

    private List<ReservationDescription> mockRunResponse;

    private ReservationDescription mockReservation;

    private List<Instance> mockRunningInstances;

    private Instance mockRunningInstance;

    private final String instanceId = "test-instance-id";

    private final String configFilePath =
            "src/test/resources/testEC2Config.properties";

    private String xmlProps;

    @Before
    public void setUp() throws Exception {
        credential = new Credential();
        credential.setUsername(username);
        credential.setPassword(password);

        EC2ComputeProviderProperties props = new EC2ComputeProviderProperties();
        props
                .loadFromXmlStream(new AutoCloseInputStream(new FileInputStream(configFilePath)));
        xmlProps = props.getAsXml();

        serviceProvider = new EC2ComputeProvider();
    }

    private void buildMockStartServiceWrapper() throws Exception {

        mockEC2 = createMock(Jec2.class);
        mockRunRequest = createMock(LaunchConfiguration.class);
        mockReservation = createMock(ReservationDescription.class);

        mockRunResponse = new ArrayList<ReservationDescription>();
        mockRunResponse.add(mockReservation);

        mockRunningInstance = createMock(Instance.class);

        expect(mockRunningInstance.isRunning()).andReturn(true);
        expect(mockRunningInstance.getInstanceId()).andReturn(instanceId);
        mockRunningInstances = new ArrayList<Instance>();
        mockRunningInstances.add(mockRunningInstance);

        expect(mockReservation.getInstances()).andReturn(mockRunningInstances);

        expect(mockEC2.runInstances((LaunchConfiguration) EasyMock.anyObject()))
                .andReturn(mockReservation);

        replay(mockRunRequest);
        replay(mockRunningInstance);
        replay(mockReservation);
        replay(mockEC2);

    }

    @After
    public void tearDown() throws Exception {
        xmlProps = null;
        serviceProvider = null;
        credential = null;
        mockEC2 = null;
        mockRunRequest = null;
        mockRunResponse = null;
        mockReservation = null;
        mockRunningInstance = null;
    }

    @Test
    public void testStart() throws Exception {
        buildMockStartServiceWrapper();
        serviceProvider.setEC2(mockEC2);

        String instId = serviceProvider.start(credential, xmlProps);
        assertNotNull(instId);
        assertTrue(instId.equals(instanceId));
    }

    //    @Test
    public void testStop() throws Exception {
        serviceProvider.stop(credential, instanceId, xmlProps);
    }

    //    @Test
    public void testDescribeRunningInstance() throws Exception {
        InstanceDescription instDesc =
                serviceProvider.describeRunningInstance(credential,
                                                        instanceId,
                                                        xmlProps);
        assertNotNull(instDesc);
    }

    //    @Test
    public void testIsInstanceRunning() throws Exception {
        boolean r =
                serviceProvider.isInstanceRunning(credential,
                                                  instanceId,
                                                  xmlProps);
        assertTrue(r);
    }

    //    @Test
    public void testIsWebappRunning() throws Exception {
        boolean r =
                serviceProvider.isWebappRunning(credential,
                                                instanceId,
                                                xmlProps);
        assertTrue(r);
    }

}
