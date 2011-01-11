/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.web;

import org.apache.commons.httpclient.HttpMethod;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Andrew Woods
 *         Date: Jan 11, 2011
 */
public class HttpResponseTest {

    private RestHttpHelper.HttpResponse response;
    private HttpMethod method;

    @Before
    public void setUp() throws IOException {
        method = createMockHttpMethod();
        response = new RestHttpHelper.HttpResponse(method);
    }

    @After
    public void tearDown() {
        EasyMock.verify(method);
    }

    private HttpMethod createMockHttpMethod() throws IOException {
        method = EasyMock.createMock("HttpMethod", HttpMethod.class);

        EasyMock.expect(method.getStatusCode()).andReturn(200);
        EasyMock.expect(method.getResponseHeaders()).andReturn(null);
        EasyMock.expect(method.getResponseFooters()).andReturn(null);
        EasyMock.expect(method.getResponseBodyAsStream()).andReturn(null);

        method.releaseConnection();
        EasyMock.expectLastCall();

        EasyMock.replay(method);
        return method;
    }

    @Test
    public void testClose() {
        // test is verified if mock object verifies.
        response.close();
    }
}
