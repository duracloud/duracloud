/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.services.controller;

import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.duracloud.serviceapi.ServicesManager;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.duracloud.services.ComputeService.SYSTEM_PREFIX;

public class ServiceControllerTest {

    private ServiceController controller;

    @Before
    public void setUp() throws Exception {
        controller = new ServiceController();
    }

    @Test
    public void testGetProperties() throws Exception {
        HttpServletRequest request = EasyMock.createMock("HttpServletRequest",
                                                         HttpServletRequest.class);
        EasyMock.expect(request.getParameter("serviceId")).andReturn("1");
        EasyMock.expect(request.getParameter("deploymentId")).andReturn("1");
        EasyMock.expect(request.getParameter("method")).andReturn("getproperties");

        Map<String,String> props = new HashMap<String,String>();
        props.put("test", "test");
        props.put(SYSTEM_PREFIX + "test", "remove");

        ServicesManager manager = EasyMock.createMock("ServicesManager",
                                                      ServicesManager.class);
        EasyMock.expect(manager.getDeployedServiceProps(EasyMock.anyInt(),
                                                        EasyMock.anyInt()))
            .andReturn(props);
        
        controller.setServicesManager(manager);

        EasyMock.replay(manager, request);

        ModelAndView mav = controller.handleRequest(request, null);

        EasyMock.verify(manager, request);

        Assert.assertNotNull(mav);
        Map map = mav.getModel();
        Assert.assertNotNull(map);
        List<Map<String,String>> result =
            (List<Map<String,String>>) map.get("properties");
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Map<String,String> test = result.get(0);
        Assert.assertNotNull(test);
        Assert.assertEquals("test", test.get("name"));
        Assert.assertEquals("test", test.get("value"));
    }
}
