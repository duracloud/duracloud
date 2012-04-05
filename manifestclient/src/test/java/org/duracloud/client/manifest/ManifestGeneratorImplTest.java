/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.manifest;

import org.duracloud.common.web.RestHttpHelper;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 4/5/12
 */
public class ManifestGeneratorImplTest {

    protected ManifestGeneratorImpl generator;
    
    protected RestHttpHelper mockRestHelper;
    protected String host = "host";
    protected String port = "8080";
    protected String context = "context";

    protected String baseUrl = "http://"+host+":"+port+"/"+context;
    protected RestHttpHelper.HttpResponse successResponse;

    @Before
    public void setup() {
        mockRestHelper = EasyMock.createMock(RestHttpHelper.class);

        generator = new ManifestGeneratorImpl(host, port, context);
        generator.setRestHelper(mockRestHelper);
        setResponse("result");
    }

    private void setResponse(String value) {
        InputStream stream = new ByteArrayInputStream(value.getBytes());
        successResponse =
            new RestHttpHelper.HttpResponse(200, null, null, stream);
    }

    private void replayMocks() {
        EasyMock.replay(mockRestHelper);
    }

    @After
    public void teardown() {
        EasyMock.verify(mockRestHelper);
    }

    private String getBaseUrl() {
        return baseUrl + "/manifest";
    }

    @Test
    public void testGetManifest() throws Exception {
        String spaceId = "space-id";
        String url = getBaseUrl() + "/" + spaceId;

        EasyMock.expect(mockRestHelper.get(url))
                .andReturn(successResponse);

        replayMocks();

        InputStream result =
            generator.getManifest("store-id", spaceId, null, null);
        assertNotNull(result);
    }

}
