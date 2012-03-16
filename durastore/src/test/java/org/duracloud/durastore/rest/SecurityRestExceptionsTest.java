/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.rest.RestUtil;
import org.duracloud.security.DuracloudUserDetailsService;
import org.easymock.EasyMock;
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

    private RestExceptionsTestSupport support = new RestExceptionsTestSupport();

    @Before
    public void setUp() throws Exception {
        DuracloudUserDetailsService userDetailsService = support.createUserDetailsService();
        restUtil = support.createRestUtil();
        securityRest = new SecurityRest(userDetailsService, restUtil);
    }

    @Test
    public void testInitializeUsers() throws Exception {
        Response response = securityRest.initializeUsers();
        support.verifyErrorResponse(response);
    }

}
