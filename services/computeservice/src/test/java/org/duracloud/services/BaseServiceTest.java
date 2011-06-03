/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services;

import org.duracloud.common.util.DateUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Map;

import static org.duracloud.services.ComputeService.ServiceStatus;

/**
 * @author Andrew Woods
 *         Date: 5/30/11
 */
public class BaseServiceTest {

    private BaseService service;

    @Before
    public void setUp() throws Exception {
        service = new BaseServiceImpl();
    }

    @Test
    public void testDoneWorkingTime() throws Exception {
        Map<String, String> props = service.getServiceProps();
        Assert.assertNotNull(props);

        // no endTime yet
        String endTime = props.get(ComputeService.STOPTIME_KEY);
        Assert.assertNull(endTime);

        // method under test
        service.doneWorking();

        props = service.getServiceProps();
        Assert.assertNotNull(props);

        // now there should be an endTime.
        endTime = props.get(ComputeService.STOPTIME_KEY);
        Assert.assertNotNull(endTime);

        // inspect endTime found.
        Date date = DateUtil.convertToDate(endTime,
                                           DateUtil.DateFormat.DEFAULT_FORMAT);
        Assert.assertNotNull(date.toString(), date);

        // endTime should be near current time.
        long millis = date.getTime();
        long currentMillis = System.currentTimeMillis();

        long delta = currentMillis - millis;
        Assert.assertTrue(currentMillis + " - " + millis + " = " + delta,
                          delta < 2000);
    }

    @Test
    public void testDoneWorkingSuccess() throws Exception {
        ServiceStatus status = service.getServiceStatus();
        Assert.assertEquals(status, ServiceStatus.INSTALLED);

        // method under test
        service.doneWorking();

        // 'success' status after completing work with no errors.
        status = service.getServiceStatus();
        Assert.assertEquals(status, ServiceStatus.SUCCESS);
    }

    @Test
    public void testDoneWorkingError() throws Exception {
        ServiceStatus status = service.getServiceStatus();
        Assert.assertEquals(status, ServiceStatus.INSTALLED);

        // set an error
        service.setError("error");

        // method under test
        service.doneWorking();

        // 'failed' status after completing work with errors.
        status = service.getServiceStatus();
        Assert.assertEquals(status, ServiceStatus.FAILED);
    }

    /**
     * private implementation of abstract BaseService.
     */
    private class BaseServiceImpl extends BaseService {
    }
}
