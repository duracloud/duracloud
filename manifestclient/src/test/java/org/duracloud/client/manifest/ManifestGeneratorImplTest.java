/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.manifest;

import org.duracloud.common.util.DateUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.manifest.ManifestGenerator;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

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

        generator = new ManifestGeneratorImpl(host,
                                              port,
                                              context,
                                              mockRestHelper);
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
        String storeId = "store-id";
        String spaceId = "space-id";
        String url1 = getBaseUrl() + "/" + spaceId + "?storeID=" + storeId;

        EasyMock.expect(mockRestHelper.get(url1))
                .andReturn(successResponse);

        String format = ManifestGenerator.FORMAT.TSV.name();
        String url2 = url1 + "&format=" + format;

        EasyMock.expect(mockRestHelper.get(url2))
                .andReturn(successResponse);

        Date asOfDate = new Date();
        String url3 =
            url2 + "&date=" + DateUtil.convertToStringPlain(asOfDate.getTime());

        EasyMock.expect(mockRestHelper.get(url3))
                .andReturn(successResponse);

        replayMocks();

        InputStream result1 =
            generator.getManifest(storeId, spaceId, null, null);
        assertNotNull(result1);

        InputStream result2 =
            generator.getManifest(storeId,
                                  spaceId,
                                  ManifestGenerator.FORMAT.TSV,
                                  null);
        assertNotNull(result2);

        InputStream result3 =
            generator.getManifest(storeId,
                                  spaceId,
                                  ManifestGenerator.FORMAT.TSV,
                                  asOfDate);
        assertNotNull(result3);

    }

}
