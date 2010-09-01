/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.duracloud.common.rest.RestUtil;
import org.duracloud.security.DuracloudUserDetailsService;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * This class tests top-level error handling of SecurityRest.
 *
 * @author Andrew Woods
 *         Date: Aug 31, 2010
 */
public class SecurityRestExceptionsTest {

    private SecurityRest securityRest;
    private RestUtil restUtil;

    @Before
    public void setUp() throws Exception {
        DuracloudUserDetailsService userDetailsService = createUserDetailsService();
        restUtil = createRestUtil();
        securityRest = new SecurityRest(userDetailsService, restUtil);
    }

    @Test
    public void testInitializeUsers() throws Exception {
        Response response = securityRest.initializeUsers();
        verifyErrorResponse(response);
    }

    private void verifyErrorResponse(Response response) {
        Assert.assertNotNull(response);

        String entity = (String) response.getEntity();
        Assert.assertNotNull(entity);

        int status = response.getStatus();
        int expectedStatus = Response.Status
            .INTERNAL_SERVER_ERROR
            .getStatusCode();
        Assert.assertEquals(expectedStatus, status);
    }

    private DuracloudUserDetailsService createUserDetailsService() {
        DuracloudUserDetailsService details = EasyMock.createMock("DetailsService",
                                                       DuracloudUserDetailsService.class);

        EasyMock.replay(details);
        return details;
    }

    private RestUtil createRestUtil() throws Exception {
        RestUtil util = EasyMock.createMock("RestUtil", RestUtil.class);
        EasyMock.expect(util.getRequestContent(null, null)).andThrow(
            createRuntimeException()).anyTimes();

        EasyMock.replay(util);
        return util;
    }

    private Throwable createRuntimeException() {
        Throwable t = null;
        try {
            t.toString();
        } catch (Exception e) {
            t = e;
        }
        Assert.assertNotNull(t);
        return t;
    }

}
