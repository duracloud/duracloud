/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * This class tests top-level error handling of SpaceRest.
 *
 * @author Andrew Woods
 *         Date: Aug 31, 2010
 */
public class SpaceRestExceptionsTest {

    private SpaceRest spaceRest;
    private SpaceResource spaceResource;

    private RestExceptionsTestSupport support = new RestExceptionsTestSupport();

    @Before
    public void setUp() throws Exception {
        spaceResource = support.createSpaceResource();
        spaceRest = new SpaceRest(spaceResource, null);
    }

    @Test
    public void testGetSpaces() throws Exception {
        Response response = spaceRest.getSpaces(null);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testGetSpace() throws Exception {
        Response response = spaceRest.getSpace(null, null, null, -1, null);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testGetSpaceProperties() throws Exception {
        Response response = spaceRest.getSpaceProperties(null, null);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testAddSpace() throws Exception {
        Response response = spaceRest.addSpace(null, null);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testDeleteSpace() throws Exception {
        Response response = spaceRest.deleteSpace(null, null);
        support.verifyErrorResponse(response);
    }
}
