/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ec2typicacomputeprovider.mgmt;

import java.io.FileInputStream;

import java.net.URL;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.ReservationDescription.Instance;
import com.xerox.amazonws.typica.jaxb.InstanceStateType;

import org.apache.commons.io.input.AutoCloseInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.duracloud.computeprovider.mgmt.InstanceState;
import org.duracloud.ec2typicacomputeprovider.mgmt.EC2ComputeProviderProperties;
import org.duracloud.ec2typicacomputeprovider.mgmt.EC2InstanceDescription;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class EC2InstanceDescriptionTest {

    private EC2InstanceDescription descGood;

    private EC2InstanceDescription descWithError;

    private List<ReservationDescription> mockResponse;

    private ReservationDescription mockReservation;

    private List<Instance> mockInstances;

    private Instance mockInstance;

    private final String state = "pending";

    private EC2ComputeProviderProperties props;

    private Exception e;

    private final String provider = "test-amazon-provider";

    private final String instanceId = "test-instanceId";

    private InstanceStateType mockState;

    private final Calendar launch = Calendar.getInstance();

    private final int year = 2009;

    private final int month = Calendar.APRIL;

    private final int date = 8;

    private final String urlSpec = "test.org";

    private final String url = "http://" + urlSpec + ":8080test-app-name";

    private final String exceptionMsg = "test-exception";

    private final String configFilePath =
            "src/test/resources/testEC2Config.properties";

    @Before
    public void setUp() throws Exception {
        buildMockObjects();
        props = new EC2ComputeProviderProperties();
        props
                .loadFromXmlStream(new AutoCloseInputStream(new FileInputStream(configFilePath)));
        e = new Exception(exceptionMsg);
    }

    private void buildMockObjects() {

        launch.set(year, month, date);
        mockState = createMock(InstanceStateType.class);
        mockState.setName(state);

        mockResponse = new ArrayList<ReservationDescription>();
        mockReservation = createMock(ReservationDescription.class);

        mockResponse.add(mockReservation);

        mockInstance = createMock(Instance.class);

        expect(mockInstance.getLaunchTime()).andReturn(launch);
        expect(mockInstance.getInstanceId()).andReturn(instanceId);
        expect(mockInstance.getState()).andReturn(state);
        expect(mockInstance.getDnsName()).andReturn(urlSpec).times(2);
        expect(mockInstance.isRunning()).andReturn(true);
        replay(mockInstance);

        mockInstances = new ArrayList<Instance>();
        mockInstances.add(mockInstance);
        expect(mockReservation.getInstances()).andReturn(mockInstances);
        replay(mockReservation);

    }

    @After
    public void tearDown() throws Exception {
        mockResponse = null;
        mockReservation = null;
        mockInstance = null;
        mockState = null;

        props = null;
        e = null;

        descGood = null;
        descWithError = null;
    }

    @Test
    public void testDescription() throws ParseException {
        descGood = new EC2InstanceDescription(mockResponse, props);

        Exception ex = descGood.getException();
        String id = descGood.getInstanceId();
        Date d8 = descGood.getLaunchTime();
        String pvdr = descGood.getProvider();
        InstanceState st = descGood.getState();
        URL u = descGood.getURL();

        assertTrue(ex == null);
        assertNotNull(id);
        assertNotNull(d8);
        assertNotNull(pvdr);
        assertNotNull(st);
        assertNotNull(u);

        assertTrue(id.equals(instanceId));
        assertTrue(d8.equals(launch.getTime()));
        assertTrue(pvdr.equals(provider));
        assertTrue(st.equals(InstanceState.fromString(state)));
        assertTrue("expect: " + url + ", found: " + u, u.toString().equals(url));

    }

    @Test
    public void testDescriptionWithError() {
        descWithError = new EC2InstanceDescription(e);
        descWithError.setProps(props);

        assertTrue(descWithError.hasError());

        Exception ex = descWithError.getException();
        assertNotNull(ex);

        assertTrue(exceptionMsg.equals(ex.getMessage()));
    }

}
