/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadmin.control;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.servicesadmin.util.HttpRequestHelper;
import org.duracloud.servicesutil.util.ServiceUninstaller;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Andrew Woods
 *         Date: Nov 26, 2010
 */
public class UninstallControllerTest {

    private UninstallController controller;
    private static final String serviceId = "service-id";

    private ServiceUninstaller uninstaller;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private ByteArrayOutputStream out;

    @Before
    public void setUp() throws Exception {
        controller = new UninstallController();
        controller.setRequestHelper(new HttpRequestHelper());

        out = new ByteArrayOutputStream();
    }

    @After
    public void tearDown() throws Exception {
        Assert.assertNotNull(request);
        EasyMock.verify(request);

        Assert.assertNotNull(response);
        EasyMock.verify(response);

        Assert.assertNotNull(uninstaller);
        EasyMock.verify(uninstaller);
    }

    @Test
    public void testHandleRequestOne() throws Exception {
        controller.setServiceUninstaller(createMockUninstaller(serviceId));

        String requestPath = "uninstall/" + serviceId;
        HttpServletRequest request = createMockRequest(requestPath);
        HttpServletResponse response = createMockResponse(true);

        ModelAndView mav = controller.handleRequestInternal(request, response);
        Assert.assertNull(mav);

        Assert.assertEquals(out.toString().trim(), serviceId + " uninstalled");
    }

    @Test
    public void testHandleRequestAll() throws Exception {
        String allId = UninstallController.ALL_SERVICES;
        controller.setServiceUninstaller(createMockUninstaller(allId));

        String requestPath = "uninstall/" + allId;
        HttpServletRequest request = createMockRequest(requestPath);
        HttpServletResponse response = createMockResponse(true);

        ModelAndView mav = controller.handleRequestInternal(request, response);
        Assert.assertNull(mav);

        Assert.assertEquals(out.toString().trim(), allId + " uninstalled");
    }

    @Test
    public void testHandleRequestNull() throws Exception {
        controller.setServiceUninstaller(createMockUninstaller(""));

        String requestPath = "uninstall/";
        HttpServletRequest request = createMockRequest(requestPath);
        HttpServletResponse response = createMockResponse(false);

        ModelAndView mav = controller.handleRequestInternal(request, response);
        Assert.assertNull(mav);

        String text = out.toString().trim();
        Assert.assertTrue(" '" + text + "'", text.startsWith("Error: "));
    }

    @Test
    public void testHandleRequestException() throws Exception {
        controller.setServiceUninstaller(createMockUninstaller("throw"));

        String requestPath = "uninstall/" + serviceId;
        HttpServletRequest request = createMockRequest(requestPath);
        HttpServletResponse response = createMockResponse(false);

        ModelAndView mav = controller.handleRequestInternal(request, response);
        Assert.assertNull(mav);

        String text = out.toString().trim();
        Assert.assertTrue(" '" + text + "'", text.startsWith("Error: "));
    }

    private ServiceUninstaller createMockUninstaller(String id)
        throws Exception {
        uninstaller = EasyMock.createMock("ServiceUninstaller",
                                          ServiceUninstaller.class);
        if (id.equals(serviceId)) {
            uninstaller.uninstall(id);
            EasyMock.expectLastCall().times(1);

        } else if (id.equals(UninstallController.ALL_SERVICES)) {
            uninstaller.uninstallAll();
            EasyMock.expectLastCall().times(1);

        } else if (id.equals("")) {
            // do nothing

        } else if (id.equals("throw")) {
            uninstaller.uninstall(EasyMock.isA(String.class));
            EasyMock.expectLastCall().andThrow(new Exception()).times(1);

        } else {
            Assert.fail("Unexpected id: " + id);
        }

        EasyMock.replay(uninstaller);
        return uninstaller;
    }

    private HttpServletRequest createMockRequest(String path) {
        request = EasyMock.createMock("HttpServletRequest",
                                      HttpServletRequest.class);
        EasyMock.expect(request.getMethod()).andReturn("DELETE").times(1);

        int times = path.equals("uninstall/") ? 2 : 1;
        EasyMock.expect(request.getRequestURI()).andReturn(
            "http://host/servicesadmin/" + path).times(times);

        EasyMock.expect(request.getPathInfo()).andReturn(path).times(1);

        EasyMock.replay(request);
        return request;
    }

    private HttpServletResponse createMockResponse(boolean success)
        throws IOException {
        response = EasyMock.createMock("HttpServletResponse",
                                       HttpServletResponse.class);

        if (success) {
            response.setStatus(HttpStatus.SC_OK);
        } else {
            response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        EasyMock.expectLastCall().times(1);

        EasyMock.expect(response.getOutputStream())
            .andReturn(createOutputStream(out))
            .times(1);

        EasyMock.replay(response);
        return response;
    }

    private ServletOutputStream createOutputStream(final ByteArrayOutputStream out) {
        return new ServletOutputStream() {
            @Override
            public void write(int i) throws IOException {
                out.write(i);
            }
        };
    }
}
