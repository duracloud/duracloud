/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.duracloud.syncui.AbstractTest;
import org.duracloud.syncui.service.SyncConfigurationManager;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class LogControllerTest extends AbstractTest {
    private SyncConfigurationManager syncConfigurationManager;
    @Before
    @Override
    public void setup()  {
        super.setup();

        syncConfigurationManager = createMock(SyncConfigurationManager.class);

    }

    @After
    @Override
    public void tearDown()  {
        super.tearDown();
    }

    @Test
    public void testGet() {
        replay();
        LogController c = new LogController(syncConfigurationManager);
        String s = c.get();
        Assert.assertNotNull(s);
    }
    
    public void testDownload() throws IOException{
        HttpServletResponse response = createMock(HttpServletResponse.class);
        response.setHeader(EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall().once();
        PrintWriter pw = createMock(PrintWriter.class);
        pw.write(EasyMock.isA(String.class));
        EasyMock.expectLastCall().once();
        EasyMock.expect(response.getWriter()).andReturn(pw);
        replay();
        LogController c = new LogController(syncConfigurationManager);
        String s = c.download(response);
        Assert.assertNotNull(s);
        
    }

}
