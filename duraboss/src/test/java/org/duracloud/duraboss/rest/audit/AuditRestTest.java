/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest.audit;

import org.apache.http.HttpStatus;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * @author Andrew Woods
 *         Date: 3/19/12
 */
public class AuditRestTest {

    private AuditRest rest;
    private AuditResource resource;

    @Before
    public void setUp() throws Exception {
        resource = EasyMock.createMock("AuditResource", AuditResource.class);
        rest = new AuditRest(resource);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(resource);
    }

    private void replayMocks() {
        EasyMock.replay(resource);
    }


    @Test
    public void testCreateInitialAuditLog() throws Exception {
        resource.createInitialAuditLogs();
        EasyMock.expectLastCall();
        replayMocks();

        Response response = rest.createInitialAuditLog();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_ACCEPTED);
    }

    @Test
    public void testGetAuditLogs() throws Exception {
        String spaceId = "space-id";
        String logs = "logs";
        EasyMock.expect(resource.getAuditLogs(spaceId)).andReturn(logs);
        replayMocks();

        Response response = rest.getAuditLogs(spaceId);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertEquals(response.getEntity(), logs);
    }

    @Test
    public void testShutdownAuditor() throws Exception {
        resource.shutdownAuditor();
        EasyMock.expectLastCall();
        replayMocks();

        Response response = rest.shutdownAuditor();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertEquals(response.getEntity(), "auditor shutting down");
    }
}
