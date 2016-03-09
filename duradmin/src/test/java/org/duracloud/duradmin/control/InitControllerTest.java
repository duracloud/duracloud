/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.appconfig.domain.DuradminConfig;
import org.duracloud.appconfig.xml.DuradminInitDocumentBinding;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.constant.Constants;
import org.duracloud.duradmin.domain.AdminInit;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Andrew Woods
 *         Date: 4/18/11
 */
public class InitControllerTest {

    private InitController controller;

    private String durastoreHost = "durastore-host";
    private String durastorePort = "111";
    private String durastoreContext = "durastore-context";
    private String amaUrl = "http://a.com";
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestAttributes requestAttributes;
    
    @Before
    public void setUp() throws Exception {
        ContentStoreManager contentStoreManager = EasyMock.createMock(
            "ContentStoreManager",
            ContentStoreManager.class);
        contentStoreManager.reinitialize((String)EasyMock.anyObject(), (String)EasyMock.anyObject(), (String)EasyMock.anyObject());
        EasyMock.expectLastCall();

        this.requestAttributes = EasyMock.createMock(RequestAttributes.class);
        RequestContextHolder.setRequestAttributes(this.requestAttributes);
        resetDuradminConfig();

        ControllerSupport support = EasyMock.createMock("ControllerSupport",
                                                        ControllerSupport.class);
        
        EasyMock.expect(support.getContentStoreManager()).andReturn(contentStoreManager);
        controller = new InitController(support);
        EasyMock.replay(support, contentStoreManager);

    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(request, response, requestAttributes);
    }

    private void resetDuradminConfig() {
        org.duracloud.duradmin.config.DuradminConfig.setConfig(new AdminInit());
    }

    @Test
    public void testInit() throws Exception {
        doTest(SC_OK);
    }

    @Test
    public void testInitFail400() throws Exception {
        doTest(SC_BAD_REQUEST);
    }

    @Test
    public void testInitFail500() throws Exception {
        doTest(SC_INTERNAL_SERVER_ERROR);
    }

    private void doTest(int status) throws Exception {
        createMocks(status);

        
        ModelAndView mav = controller.initialize(request, response);
        Assert.assertNotNull(mav);

        org.duracloud.duradmin.config.DuradminConfig config = new org.duracloud.duradmin.config.DuradminConfig();

        String url = config.getAmaUrl();

        if (status == SC_OK) {
            Assert.assertNotNull(url);

            Assert.assertEquals(amaUrl, url);

        } else {
            Assert.assertNull(url);
        }

    }

    private void createMocks(int status) throws IOException {
        request = EasyMock.createMock("HttpServletRequest",
                                      HttpServletRequest.class);

        ServletInputStream configStream = createConfigStream(status);
        EasyMock.expect(request.getInputStream()).andReturn(configStream);

        response = EasyMock.createMock("HttpServletResponse",
                                       HttpServletResponse.class);
        response.setStatus(status);
        EasyMock.expectLastCall();

        
        if(status == SC_OK){
            EasyMock.expect(requestAttributes.getAttribute(Constants.SERVER_HOST,
                                                           RequestAttributes.SCOPE_REQUEST))
                    .andReturn("localhost");
            EasyMock.expect(requestAttributes.getAttribute(Constants.SERVER_PORT,
                                                           RequestAttributes.SCOPE_REQUEST))
                    .andReturn(8080);

        }

        
        EasyMock.replay(request, response, requestAttributes);
        
    }

    private ServletInputStream createConfigStream(int status) {

        final InputStream in;
        String xml;
        if (status == SC_OK) {
            xml = createConfigXml(createDuradminConfig());
            in = new AutoCloseInputStream(new ByteArrayInputStream(xml.getBytes()));

        } else if (status == SC_INTERNAL_SERVER_ERROR) {
            xml = "junk";
            in = new AutoCloseInputStream(new ByteArrayInputStream(xml.getBytes()));

        } else {
            in = null;
        }

        ServletInputStream stream = new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return in.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };

        if (status == SC_BAD_REQUEST) {
            stream = null;
        }

        return stream;
    }

    private String createConfigXml(DuradminConfig duradminConfig) {
        return DuradminInitDocumentBinding.createDocumentFrom(duradminConfig);
    }

    private DuradminConfig createDuradminConfig() {
        DuradminConfig config = new DuradminConfig();

        String p = DuradminConfig.QUALIFIER + ".";

        Map<String, String> props = new HashMap<String, String>();
        props.put(p + DuradminConfig.duraStoreHostKey, durastoreHost);
        props.put(p + DuradminConfig.duraStorePortKey, durastorePort);
        props.put(p + DuradminConfig.duraStoreContextKey, durastoreContext);
        props.put(p + DuradminConfig.amaUrlKey, amaUrl);

        config.load(props);

        return config;
    }
}
