/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.rest;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.duracloud.common.rest.RestUtil.RequestContent;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class RestUtilImplTest extends EasyMockSupport {

    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpHeaders headers;
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        verifyAll();
    }

    @Test
    public void testDuracloud882() throws Exception{
        long contentSize = (long)Integer.MAX_VALUE + 1l;

        File file = File.createTempFile("temp", "txt");
        file.createNewFile();
        file.deleteOnExit();

        ServletInputStream is = createMock(ServletInputStream.class);
        expect(request.getMethod()).andReturn("post");
        expect(request.getContentType()).andReturn("text/plain");
        expect(request.getInputStream()).andReturn(is);
        expect(request.getContentLength()).andReturn(-1);
        
        expect(headers.getMediaType()).andReturn(MediaType.TEXT_PLAIN_TYPE);
        List<String> contentLengthHeaders = Arrays.asList(new String[]{contentSize+""});
        expect(headers.getRequestHeader("Content-Length")).andReturn(contentLengthHeaders);
        replayAll();
        RestUtilImpl util = new RestUtilImpl();
        RequestContent content = util.getRequestContent(request, headers);
        assertEquals(contentSize, content.getSize());
        
    }

}
