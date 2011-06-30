/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.report;

import org.duracloud.common.web.RestHttpHelper;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: 6/30/11
 */
public class ReportManagerTestBase {

    protected RestHttpHelper mockRestHelper;
    protected String host = "host";
    protected String port = "8080";
    protected String context = "context";

    protected String baseUrl = "http://"+host+":"+port+"/"+context;
    protected RestHttpHelper.HttpResponse successResponse;

    @Before
    public void setup() {
        mockRestHelper = EasyMock.createMock(RestHttpHelper.class);
    }

    protected void setResponse(String value) {
        InputStream stream = new ByteArrayInputStream(value.getBytes());
        successResponse =
            new RestHttpHelper.HttpResponse(200, null, null, stream);
    }

    protected void replayMocks() {
        EasyMock.replay(mockRestHelper);
    }

    @After
    public void teardown() {
        EasyMock.verify(mockRestHelper);
    }


}
