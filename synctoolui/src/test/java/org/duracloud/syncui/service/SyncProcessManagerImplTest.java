/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

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
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        this.syncProcessManagerImpl =
            new SyncProcessManagerImpl(syncConfigurationManager,
                                       this.contentStoreManagerFactory,
                                       this.syncOptimizeManager);

    }

    protected void setupStart() throws ContentStoreException {
        setupStart(1);
    }
    protected void setupStart(int times) throws ContentStoreException {
        
        File directory = new File(System.getProperty("java.io.tmpdir") + File.separator + "test-" + System.currentTimeMillis());
        directory.mkdirs();
        directory.deleteOnExit();
            
        EasyMock.expect(this.syncOptimizeManager.isRunning())
                .andReturn(false).atLeastOnce();
        EasyMock.expect(this.syncConfigurationManager.getThreadCount())
                .andReturn(10).atLeastOnce();
        
        ContentStoreManager contentStoreManager =
            createMock(ContentStoreManager.class);
        contentStoreManager.login(EasyMock.isA(Credential.class));

        EasyMock.expectLastCall().times(times);

        EasyMock.expect(contentStoreManagerFactory.create())
                .andReturn(contentStoreManager).times(times);

        EasyMock.expect(contentStoreManager.getPrimaryContentStore())
                .andReturn(contentStore).times(times);
        EasyMock.expect(this.contentStore.getStoreId()).andReturn("0").anyTimes();

        EasyMock.expect(this.contentStore.getSpaceContents(EasyMock.isA(String.class)))
                .andAnswer(new IAnswer<Iterator<String>>() {
                    @Override
                    public Iterator<String> answer() throws Throwable {
                        return Arrays.asList(new String[] {}).iterator();
                    }
                }).anyTimes();

        DirectoryConfigs dconfigs = new DirectoryConfigs();
        dconfigs.add(new DirectoryConfig(directory.getAbsolutePath()));
        EasyMock.expect(this.syncConfigurationManager.retrieveDirectoryConfigs())
                .andReturn(dconfigs).times(times);

        EasyMock.expect(this.syncConfigurationManager.isSyncDeletes())
        .andReturn(true).times(times);
        
        EasyMock.expect(this.syncConfigurationManager.isSyncUpdates())
        .andReturn(true).times(times);

        EasyMock.expect(this.syncConfigurationManager.isRenameUpdates())
        .andReturn(false).times(times);

        EasyMock.expect(this.syncConfigurationManager.isJumpStart())
        .andReturn(false).times(times);

        EasyMock.expect(this.syncConfigurationManager.getUpdateSuffix())
        .andReturn(null).times(times);

        EasyMock.expect(this.syncConfigurationManager.getPrefix())
        .andReturn(null).times(times);

        DuracloudConfiguration dc = createMock(DuracloudConfiguration.class);
        EasyMock.expect(dc.getUsername()).andReturn("testusername").times(times);
        EasyMock.expect(dc.getPassword()).andReturn("testpassword").times(times);
        EasyMock.expect(dc.getSpaceId()).andReturn("testspace").times(times);

        EasyMock.expect(this.syncConfigurationManager.retrieveDuracloudConfiguration())
                .andReturn(dc).times(times);
    }
    
    @Test
    public void testResume() throws Exception {
        setupStart(2);

        replay();
        
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
        Assert.assertTrue(listeners[++i].success());
        syncProcessManagerImpl.pause();
        Assert.assertTrue(listeners[++i].success());
        Assert.assertTrue(listeners[++i].success());

        syncProcessManagerImpl.resume();
        
        Assert.assertTrue(listeners[++i].success());
        Assert.assertTrue(listeners[++i].success());
        
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
        TestSyncStateListener listener =
            new TestSyncStateListener(SyncProcessState.RUNNING);
        syncProcessManagerImpl.addSyncStateChangeListener(listener);
        syncProcessManagerImpl.start();
        Assert.assertTrue(listener.success());
    }

    @Test
    public void testStop() throws SyncProcessException, ContentStoreException {
        setupStart();
        
        replay();

        TestSyncStateListener listener0 =
            new TestSyncStateListener(SyncProcessState.RUNNING);
        TestSyncStateListener listener1 =
            new TestSyncStateListener(SyncProcessState.STOPPED);
        syncProcessManagerImpl.addSyncStateChangeListener(listener0);
        syncProcessManagerImpl.addSyncStateChangeListener(listener1);
        syncProcessManagerImpl.start();
        Assert.assertTrue(listener0.success());
        syncProcessManagerImpl.stop();
        Assert.assertTrue(listener1.success());
    }

    @Test
    public void testRestart() throws SyncProcessException, ContentStoreException {
        setupStart(2);
        EasyMock.expectLastCall();

        replay();

        TestSyncStateListener listener0 =
            new TestSyncStateListener(SyncProcessState.RUNNING);
        TestSyncStateListener listener1 =
            new TestSyncStateListener(SyncProcessState.STOPPED);
        TestSyncStateListener listener2 =
            new TestSyncStateListener(SyncProcessState.RUNNING);

        syncProcessManagerImpl.addSyncStateChangeListener(listener0);
        syncProcessManagerImpl.start();
        Assert.assertTrue(listener0.success());

        syncProcessManagerImpl.addSyncStateChangeListener(listener1);
        syncProcessManagerImpl.addSyncStateChangeListener(listener2);
        syncProcessManagerImpl.restart();

        Assert.assertTrue(listener1.success());
        Assert.assertTrue(listener2.success());

    }

    
    @Test
    public void testPaused() throws SyncProcessException, ContentStoreException {
        setupStart();
        replay();

        TestSyncStateListener listener0 =
            new TestSyncStateListener(SyncProcessState.RUNNING);

        TestSyncStateListener listener1 =
            new TestSyncStateListener(SyncProcessState.PAUSED);
        syncProcessManagerImpl.addSyncStateChangeListener(listener0);
        syncProcessManagerImpl.addSyncStateChangeListener(listener1);
        syncProcessManagerImpl.start();
        Assert.assertTrue(listener0.success());
        syncProcessManagerImpl.pause();
        Assert.assertTrue(listener1.success());
    }

    @Test
    public void testGetProcessState() {
        replay();
        SyncProcessState state = syncProcessManagerImpl.getProcessState();
        Assert.assertEquals(SyncProcessState.STOPPED, state);
    }

    @Test
    public void testGetProcessStats() {
        replay();
        SyncProcessStats stats = syncProcessManagerImpl.getProcessStats();
        Assert.assertNotNull(stats);
    }

    @Test
    public void testGetMonitoredFiles() {
        replay();
        List<MonitoredFile> files = syncProcessManagerImpl.getMonitoredFiles();
        Assert.assertNotNull(files);
    }

    @Test
    public void testGetQueuedFiles() {
        replay();
        List<File> files = syncProcessManagerImpl.getQueuedFiles();
        Assert.assertNotNull(files);
    }

    @Test
    public void testGetFailures() {
        replay();
        List<SyncSummary> failures = syncProcessManagerImpl.getFailures();
        Assert.assertNotNull(failures);
    }

    @Test
    public void testGetRecentlyCompleted() {
        replay();
        List<SyncSummary> completed = syncProcessManagerImpl.getRecentlyCompleted();
        Assert.assertNotNull(completed);
    }

}
