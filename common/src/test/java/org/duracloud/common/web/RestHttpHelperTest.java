/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.duracloud.common.model.Credential;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RestHttpHelperTest {

    private static Server server;

    private static String host = null;
    
    static {
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private static int port = 8088;

    private static String context = "/test";

    private RestHttpHelper helper;

    private Map<String, String> headers;

    @BeforeClass
    public static void startServer() throws Exception {
        server = new Server(port);

        ServletContextHandler context =
            new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new MockServlet()),"/*");

        server.start();
    }

    @Before
    public void setUp() throws Exception {
        helper = new RestHttpHelper();
        headers = new HashMap<>();
        headers.put("header-key0", "header-value0");
    }

    @After
    public void tearDown() throws Exception {
        helper = null;
        headers = null;
    }

    @AfterClass
    public static void stopServer() throws Exception {
        server.stop();
        server = null;
    }

    @Test
    public void testGet() throws Exception {
        HttpResponse response = helper.get(getUrl());
        verifyResponse(response);
    }

    @Test
    public void testGetBasicAuth() throws Exception {
        Credential credential = new Credential("joeUser", "joesPassword");
        helper = new RestHttpHelper(credential);

        HttpResponse response = helper.get(getUrl());
        verifyResponse(response);
    }

    @Test
    public void testDelete() throws Exception {
        HttpResponse response = helper.delete(getUrl());
        verifyResponse(response);
    }

    @Test
    public void testPost() throws Exception {
        String requestContent = "<x>junk</x>";

        HttpResponse response = helper.post(getUrl(), requestContent, headers);
        verifyResponse(response);
    }

    @Test
    public void testPut() throws Exception {
        String requestContent = "<x>junk</x>";

        HttpResponse response = helper.put(getUrl(), requestContent, headers);
        verifyResponse(response);
    }

    @Test
    public void testMultipartPost() throws Exception {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("text-part", "text");
        // Note: Including a file as one piece of the multipart body
        // causes this test to fail intermittently. Replacing it here with string bytes.
        builder.addBinaryBody("binary-part", "binary-content".getBytes());
        HttpEntity reqEntity = builder.build();

        HttpResponse response = helper.multipartPost(getUrl(), reqEntity);
        verifyResponse(response);
    }

    @Test
    public void testMultipartFilePost() throws Exception {
        File file = createTmpFile();
        HttpResponse response = helper.multipartFilePost(getUrl(), file);
        verifyResponse(response);
        file.delete();
    }

    @Test
    public void testMultipartFileStreamPost() throws Exception {
        File file = createTmpFile();
        FileInputStream fileStream = new FileInputStream(file);
        HttpResponse response =
                helper.multipartFileStreamPost(getUrl(),
                        file.getName(),
                        fileStream);
        verifyResponse(response);
        file.delete();
    }

    private String getUrl() {
        return "http://" + host + ":" + port + context;
    }

    private File createTmpFile() throws IOException {
        return File.createTempFile("test-file", ".tmp");
    }

    private void verifyResponse(HttpResponse response) {
        assertNotNull(response);
        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
    }
}
