/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest.audit;

import org.duracloud.audit.Auditor;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: 3/19/12
 */
public class AuditResourceTest {

    private AuditResource resource;
    private Auditor auditor;

    @Before
    public void setUp() throws Exception {
        auditor = EasyMock.createMock("Auditor", Auditor.class);
        resource = new AuditResource(auditor);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(auditor);
    }

    private void replayMocks() {
        EasyMock.replay(auditor);
    }

    @Test
    public void testCreateInitialAuditLogs() throws Exception {
        auditor.createInitialAuditLogs(true);
        EasyMock.expectLastCall();
        replayMocks();

        resource.createInitialAuditLogs();
    }

    @Test
    public void testShutdownAuditor() throws Exception {
        auditor.stop();
        EasyMock.expectLastCall();
        replayMocks();

        resource.shutdownAuditor();
    }

    @Test
    public void testGetAuditLogs() throws Exception {
        String spaceId = "space-id";
        List<String> logs = new ArrayList<String>();
        String log0 = "log-0";
        String log1 = "log-1";
        logs.add(log0);
        logs.add(log1);
        EasyMock.expect(auditor.getAuditLogs(spaceId)).andReturn(logs);
        replayMocks();

        String result = resource.getAuditLogs(spaceId);
        Assert.assertNotNull(result);

        int size = log0.length() + log1.length() + 2;
        Assert.assertEquals(size, result.length());
        Assert.assertTrue(result.contains(log0));
        Assert.assertTrue(result.contains(log1));
    }
}
