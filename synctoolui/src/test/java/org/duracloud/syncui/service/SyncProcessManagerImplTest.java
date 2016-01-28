/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.sync.endpoint.MonitoredFile;
import org.duracloud.sync.mgmt.SyncSummary;
import org.duracloud.syncui.AbstractTest;
import org.duracloud.syncui.domain.DirectoryConfig;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.domain.DuracloudConfiguration;
import org.duracloud.syncui.domain.SyncProcessState;
import org.duracloud.syncui.domain.SyncProcessStats;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
/**
 * 
 * @author Daniel Bernstein
 *
 */
public class SyncProcessManagerImplTest extends AbstractTest {
    private SyncProcessManagerImpl syncProcessManagerImpl;
    private SyncConfigurationManager syncConfigurationManager;
    private ContentStore contentStore;
    private ContentStoreManagerFactory contentStoreManagerFactory;
    private SyncOptimizeManager syncOptimizeManager;
    private File contentDir = null;
    private File workDir = null;
    
    class TestSyncStateListener implements SyncStateChangeListener {
        private CountDownLatch latch = new CountDownLatch(1);
        private SyncProcessState state;

        public TestSyncStateListener(SyncProcessState state) {
            this.state = state;
        }

        @Override
        public void stateChanged(SyncStateChangedEvent event) {
            if (event.getProcessState() == this.state) {
                latch.countDown();
            }
        }

        public boolean success() {
            try {
                return latch.await(20000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                return false;
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        super.setup();
        syncConfigurationManager = createMock(SyncConfigurationManager.class);
        this.contentStore = createMock(ContentStore.class);
        this.contentStoreManagerFactory =
            createMock(ContentStoreManagerFactory.class);
        this.syncOptimizeManager = createMock(SyncOptimizeManager.class);
        setupWorkingDirectory();
        setupContentDirectories();


    }
    
    
    @After
    public void tearDown(){
        super.tearDown();
        try {
            FileUtils.deleteDirectory(this.contentDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileUtils.deleteDirectory(this.workDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void createTestSubject() {
        this.syncProcessManagerImpl =
            new SyncProcessManagerImpl(syncConfigurationManager,
                                       this.contentStoreManagerFactory,
                                       this.syncOptimizeManager);
    }

    protected void setupStart() throws ContentStoreException {
        setupStart(1);
    }
    protected void setupStart(int times) throws ContentStoreException {
        
            
        expect(this.syncOptimizeManager.isRunning())
                .andReturn(false).atLeastOnce();
        expect(this.syncConfigurationManager.getThreadCount())
                .andReturn(10).atLeastOnce();
        ContentStoreManager contentStoreManager =
            createMock(ContentStoreManager.class);
        contentStoreManager.login(isA(Credential.class));

        expectLastCall().times(times);

        expect(contentStoreManagerFactory.create())
                .andReturn(contentStoreManager).times(times);

        expect(contentStoreManager.getPrimaryContentStore())
                .andReturn(contentStore).times(times);
        expect(this.contentStore.getStoreId()).andReturn("0").anyTimes();

        expect(this.contentStore.getSpaceContents(isA(String.class)))
                .andAnswer(new IAnswer<Iterator<String>>() {
                    @Override
                    public Iterator<String> answer() throws Throwable {
                        return Arrays.asList(new String[] {}).iterator();
                    }
                }).anyTimes();

        

        expect(this.syncConfigurationManager.isSyncDeletes())
        .andReturn(true).times(times);
        
        expect(this.syncConfigurationManager.isSyncUpdates())
        .andReturn(true).times(times);

        expect(this.syncConfigurationManager.isRenameUpdates())
        .andReturn(false).times(times);

        expect(this.syncConfigurationManager.isJumpStart())
        .andReturn(false).times(times);

        expect(this.syncConfigurationManager.getUpdateSuffix())
        .andReturn(null).times(times);

        expect(this.syncConfigurationManager.getPrefix())
        .andReturn(null).times(times);

        DuracloudConfiguration dc = createMock(DuracloudConfiguration.class);
        expect(dc.getUsername()).andReturn("testusername").times(times);
        expect(dc.getPassword()).andReturn("testpassword").times(times);
        expect(dc.getSpaceId()).andReturn("testspace").times(times);

        expect(this.syncConfigurationManager.retrieveDuracloudConfiguration())
                .andReturn(dc).times(times);
        expect(this.syncConfigurationManager.getMode())
        .andReturn(RunMode.CONTINUOUS).times(times);

    }
    
    protected void setupContentDirectories(){
        this.contentDir =
            new File(System.getProperty("java.io.tmpdir") + File.separator
                     + "test-"
                     + System.currentTimeMillis());
        this.contentDir.mkdirs();
        this.contentDir.deleteOnExit();


        DirectoryConfigs dconfigs = new DirectoryConfigs();
        dconfigs.add(new DirectoryConfig(this.contentDir.getAbsolutePath()));
        expect(this.syncConfigurationManager.retrieveDirectoryConfigs())
                .andReturn(dconfigs).atLeastOnce();
    }

    protected void setupWorkingDirectory() {
        this.workDir =
            new File(System.getProperty("java.io.tmpdir") + File.separator
                     + "workdir-"
                     + System.currentTimeMillis());
        this.workDir.mkdirs();
        this.workDir.deleteOnExit();


        expect(this.syncConfigurationManager.getWorkDirectory()).andReturn(this.workDir)
                                                                .anyTimes();
    }
    
    @Test
    public void testResume() throws Exception {
        setupStart(2);
        replay();
        createTestSubject();

        TestSyncStateListener[] listeners =  {
            new TestSyncStateListener(SyncProcessState.RUNNING),
            new TestSyncStateListener(SyncProcessState.PAUSING),
            new TestSyncStateListener(SyncProcessState.PAUSED),
            new TestSyncStateListener(SyncProcessState.RESUMING),
            new TestSyncStateListener(SyncProcessState.RUNNING)            
        };


        for(TestSyncStateListener listener : listeners){
            syncProcessManagerImpl.addSyncStateChangeListener(listener);
        }

        syncProcessManagerImpl.start();
        int i = -1;
        assertTrue(listeners[++i].success());
        syncProcessManagerImpl.pause();
        assertTrue(listeners[++i].success());
        assertTrue(listeners[++i].success());

        syncProcessManagerImpl.resume();
        
        assertTrue(listeners[++i].success());
        assertTrue(listeners[++i].success());
        
        //this sleep seems to be necessary: otherwise 
        //the verify step is returning strange info: 
        //namely it complains about a verify problem
        //even though the expected and actual calls are equal.
        Thread.sleep(1000);
    }    
    
    @Test
    public void testStart()
        throws SyncProcessException,
            ContentStoreException {
        setupStart();
        replay();
        createTestSubject();

        TestSyncStateListener listener =
            new TestSyncStateListener(SyncProcessState.RUNNING);
        syncProcessManagerImpl.addSyncStateChangeListener(listener);
        syncProcessManagerImpl.start();
        assertTrue(listener.success());
    }

    @Test
    public void testStop() throws SyncProcessException, ContentStoreException {
        setupStart();
        replay();
        createTestSubject();

        TestSyncStateListener listener0 =
            new TestSyncStateListener(SyncProcessState.RUNNING);
        TestSyncStateListener listener1 =
            new TestSyncStateListener(SyncProcessState.STOPPED);
        syncProcessManagerImpl.addSyncStateChangeListener(listener0);
        syncProcessManagerImpl.addSyncStateChangeListener(listener1);
        syncProcessManagerImpl.start();
        assertTrue(listener0.success());
        syncProcessManagerImpl.stop();
        assertTrue(listener1.success());
    }

    @Test
    public void testRestart() throws SyncProcessException, ContentStoreException {
        setupStart(2);
        expectLastCall();

        replay();
        createTestSubject();

        TestSyncStateListener listener0 =
            new TestSyncStateListener(SyncProcessState.RUNNING);
        TestSyncStateListener listener1 =
            new TestSyncStateListener(SyncProcessState.STOPPED);
        TestSyncStateListener listener2 =
            new TestSyncStateListener(SyncProcessState.RUNNING);

        syncProcessManagerImpl.addSyncStateChangeListener(listener0);
        syncProcessManagerImpl.start();
        assertTrue(listener0.success());

        syncProcessManagerImpl.addSyncStateChangeListener(listener1);
        syncProcessManagerImpl.addSyncStateChangeListener(listener2);
        syncProcessManagerImpl.restart();

        assertTrue(listener1.success());
        assertTrue(listener2.success());

    }

    
    @Test
    public void testPaused() throws SyncProcessException, ContentStoreException {
        setupStart();
        replay();
        createTestSubject();

        TestSyncStateListener listener0 =
            new TestSyncStateListener(SyncProcessState.RUNNING);

        TestSyncStateListener listener1 =
            new TestSyncStateListener(SyncProcessState.PAUSED);
        syncProcessManagerImpl.addSyncStateChangeListener(listener0);
        syncProcessManagerImpl.addSyncStateChangeListener(listener1);
        syncProcessManagerImpl.start();
        assertTrue(listener0.success());
        syncProcessManagerImpl.pause();
        assertTrue(listener1.success());
    }

    @Test
    public void testGetProcessState() {
        replay();
        createTestSubject();

        SyncProcessState state = syncProcessManagerImpl.getProcessState();
        assertEquals(SyncProcessState.STOPPED, state);
    }

    @Test
    public void testGetProcessStats() {
        replay();
        createTestSubject();

        SyncProcessStats stats = syncProcessManagerImpl.getProcessStats();
        assertNotNull(stats);
    }

    @Test
    public void testGetMonitoredFiles() {
        replay();
        createTestSubject();

        List<MonitoredFile> files = syncProcessManagerImpl.getMonitoredFiles();
        assertNotNull(files);
    }

    @Test
    public void testGetQueuedFiles() {
        replay();
        createTestSubject();

        List<File> files = syncProcessManagerImpl.getQueuedFiles();
        assertNotNull(files);
    }

    @Test
    public void testGetFailures() {
        replay();
        createTestSubject();

        List<SyncSummary> failures = syncProcessManagerImpl.getFailures();
        assertNotNull(failures);
    }

    @Test
    public void testGetRecentlyCompleted() {
        replay();
        createTestSubject();

        List<SyncSummary> completed = syncProcessManagerImpl.getRecentlyCompleted();
        assertNotNull(completed);
    }

}
