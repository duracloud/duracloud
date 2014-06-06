/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import java.io.IOException;

import org.duracloud.syncoptimize.SyncOptimizeDriver;
import org.duracloud.syncoptimize.config.SyncOptimizeConfig;
import org.duracloud.syncui.controller.SyncOptimizeManagerResultCallBack;
import org.duracloud.syncui.domain.DuracloudConfiguration;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;
/**
 * 
 * @author Daniel Bernstein
 *
 */
@RunWith(EasyMockRunner.class)
public class SyncOptimizeManagerTest extends EasyMockSupport{

    @Mock
    private SyncConfigurationManager syncConfigurationManager;

    @Mock
    private SyncOptimizeDriver syncOptimizeDriver;
    
    @Mock
    private DuracloudConfiguration duracloudConfig;

    @Mock
    private SyncOptimizeManagerResultCallBack callback;
    
    private SyncOptimizeManager syncOptimizeManager;
    
    private int threadCount = 10;
    
    @Before
    public void setUp() throws Exception {
        
        
    }
    
    @After
    public void tearDown(){
        verifyAll();
    }
    
    @Test
    public void testStart() throws Exception{
        setupStart();
        setupDriver();

        this.syncConfigurationManager.setThreadCount(threadCount);
        expectLastCall();
        this.callback.onSuccess();
        expectLastCall();

        replayAll();
        this.syncOptimizeManager.start(callback);

        Thread.sleep(500);
        assertFalse(this.syncOptimizeManager.isRunning());
        assertFalse(this.syncOptimizeManager.isFailed());
    }

    protected void setupStart() throws IOException {
        createTestSubject();
        setupDuracloudConfig();
    }        

    protected void setupDriver() throws IOException {
        expect(this.syncOptimizeDriver.getOptimalThreads(isA(SyncOptimizeConfig.class))).andReturn(threadCount);
    }

    protected void createTestSubject() {
        this.syncOptimizeManager = new SyncOptimizeManager(this.syncConfigurationManager, this.syncOptimizeDriver);
    }

    
    @Test
    public void testStartFailure() throws Exception{
        setupStart();
        expect(this.syncOptimizeDriver.getOptimalThreads(isA(SyncOptimizeConfig.class))).andThrow(new IOException());
        this.callback.onFailure(isA(IOException.class), isA(String.class));
        expectLastCall();
        replayAll();
        this.syncOptimizeManager.start(callback);
        Thread.sleep(500);
        assertFalse(this.syncOptimizeManager.isRunning());
        assertTrue(this.syncOptimizeManager.isFailed());
        
    }

    protected void setupDuracloudConfig() {
        expect(this.duracloudConfig.getHost()).andReturn("host");
        expect(this.duracloudConfig.getPort()).andReturn(8080);
        expect(this.duracloudConfig.getPassword()).andReturn("password");
        expect(this.duracloudConfig.getUsername()).andReturn("username");
        expect(this.duracloudConfig.getSpaceId()).andReturn("spaceId");
        expect(this.syncConfigurationManager.retrieveDuracloudConfiguration()).andReturn(this.duracloudConfig);
    }

}
