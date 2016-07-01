/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.rest;

import static org.easymock.EasyMock.*;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.common.constant.Constants;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
/**
 * 
 * @author Daniel Bernstein 
 *         Date: 12/09/2015
 *
 */
@RunWith(EasyMockRunner.class)
public class DuraCloudRequestContextFilterTest extends EasyMockSupport {

    @Mock
    private HttpServletRequest request;
    
    @Mock 
    private HttpServletResponse response;
    
    @Mock
    private FilterChain chain;
    
    @Mock
    private FilterConfig filterConfig;

    private String accountId = "test";

    private String host = accountId + ".duracloud.org";
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        verifyAll();
        System.clearProperty(DuraCloudRequestContextFilter.ACCOUNT_ID_OVERRIDE_SYSTEM_PROP_KEY);

    }

    @Test
    public void testDoFilterWithOverride() throws Exception{
        System.setProperty(DuraCloudRequestContextFilter.ACCOUNT_ID_OVERRIDE_SYSTEM_PROP_KEY,accountId);
        setupSetAttribute();
        executeDoFilter();
    }

    @Test
    public void testDoFilterWithoutOverride() throws Exception{
        setupSetAttribute();
        executeDoFilter();
    }

    protected void setupSetAttribute() throws IOException, ServletException {
        
        expect(request.getHeader(DuraCloudRequestContextFilter.X_FORWARDED_HOST_HEADER)).andReturn(host);
        expect(request.getServerPort()).andReturn(443);

        request.setAttribute(Constants.ACCOUNT_ID_ATTRIBUTE, accountId);
        request.setAttribute(Constants.SERVER_PORT, 443);
        request.setAttribute(Constants.SERVER_HOST, host);

        chain.doFilter(request, response);
        expectLastCall();
    }

    protected void executeDoFilter()
        throws ServletException,
            IOException {
        replayAll();
        Filter filter = new DuraCloudRequestContextFilter();
        filter.init(filterConfig);
        filter.doFilter(request, response, chain);
    }

}
