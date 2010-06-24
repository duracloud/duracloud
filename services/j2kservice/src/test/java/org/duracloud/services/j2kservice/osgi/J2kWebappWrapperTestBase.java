/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.j2kservice.osgi;

import org.duracloud.services.ComputeService;
import org.duracloud.services.j2kservice.J2kWebappWrapper;
import org.duracloud.common.web.RestHttpHelper;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.httpclient.Header;

import java.util.Map;
import java.net.HttpURLConnection;
import java.io.InputStream;

/**
 * @author Andrew Woods
 *         Date: Jan 25, 2010
 */
public class J2kWebappWrapperTestBase {

    protected J2kWebappWrapper wrapper;
    protected String urlOrig = "http://example.org";
    protected String warName = "adore-djatoka.war";
    protected String zipName = "adore-djatoka-1.1.zip";

    protected String urlRunningBase = "http://127.\\d.\\d.1";
    protected String context = FilenameUtils.getBaseName(warName);

    private String imageSuffix = "resolver?url_ver=Z39.88-2004&rft_id=" +
        "http://memory.loc.gov/gmd/gmd433/g4330/g4330/np000066.jp2&" +
        "svc_id=info:lanl-repo/svc/getRegion&" +
        "svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&svc.format=image/jpeg&" +
        "svc.level=3&svc.rotate=0&svc.region=0,0,500,500";

    protected void testStopStartCycle(String expectedUrlPattern)
        throws Exception {
        verifyURL(urlOrig);

        ComputeService.ServiceStatus status = wrapper.getServiceStatus();
        Assert.assertNotNull(status);
        Assert.assertEquals(ComputeService.ServiceStatus.INSTALLED, status);

        wrapper.start();
        status = wrapper.getServiceStatus();
        Assert.assertEquals(ComputeService.ServiceStatus.STARTED, status);

        verifyURL(expectedUrlPattern);

        wrapper.stop();
        status = wrapper.getServiceStatus();
        Assert.assertEquals(ComputeService.ServiceStatus.STOPPED, status);

        verifyURL(urlOrig);
    }

    private void verifyURL(String expectedURL) {
        Map<String, String> props = wrapper.getServiceProps();
        Assert.assertNotNull(props);

        String urlProp = props.get("url");
        Assert.assertNotNull(urlProp);
        Assert.assertTrue(urlProp, urlProp.matches(expectedURL));
    }

    protected void testImageServing(String expectedUrlPattern)
        throws Exception {
        verifyURL(urlOrig);

        ComputeService.ServiceStatus status = wrapper.getServiceStatus();
        assertNotNull(status);
        Assert.assertTrue(ComputeService.ServiceStatus.STARTED != status);

        wrapper.start();
        status = wrapper.getServiceStatus();
        assertEquals(ComputeService.ServiceStatus.STARTED, status);

        verifyURL(expectedUrlPattern);
        String url = wrapper.getServiceProps().get("url");

        verifyImageResponse(url);

        wrapper.stop();
        status = wrapper.getServiceStatus();
        assertEquals(ComputeService.ServiceStatus.STOPPED, status);

    }

    private void verifyImageResponse(String url) throws Exception {
        String imageUrl = url + "/" + imageSuffix;
        RestHttpHelper helper = new RestHttpHelper();
        RestHttpHelper.HttpResponse response = helper.get(imageUrl);
        assertNotNull(response);
        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());

        InputStream stream = response.getResponseStream();
        assertNotNull(stream);
        Header[] headers = response.getResponseHeaders();
        assertNotNull(headers);
        boolean contentTypeFound = false;
        boolean transferEncodingFound = false;
        for (Header header : headers) {
            String name = header.getName();
            String value = header.getValue();
            System.out.println("n:'" + name + "', v:'" + value + "'");
            if (name.equalsIgnoreCase("Content-Type")) {
                contentTypeFound = true;
                assertEquals("image/jpeg", value);
            } else if (name.equalsIgnoreCase("Transfer-Encoding")) {
                transferEncodingFound = true;
                assertEquals("chunked", value);
            }
        }
        Assert.assertTrue(contentTypeFound);
        Assert.assertTrue(transferEncodingFound);
    }

}
