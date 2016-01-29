/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.event.EventListenerSupport;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.sync.backup.SyncBackupManager;
import org.duracloud.sync.endpoint.DuraStoreChunkSyncEndpoint;
import org.duracloud.sync.endpoint.EndPointLogger;
import org.duracloud.sync.endpoint.MonitoredFile;
import org.duracloud.sync.endpoint.SyncEndpoint;
import org.duracloud.sync.mgmt.ChangedList;
import org.duracloud.sync.mgmt.ChangedListListener;
import org.duracloud.sync.mgmt.StatusManager;
import org.duracloud.sync.mgmt.SyncManager;
import org.duracloud.sync.mgmt.SyncSummary;
import org.duracloud.sync.monitor.DirectoryUpdateMonitor;
import org.duracloud.sync.walker.DeleteChecker;
import org.duracloud.sync.walker.DirWalker;
import org.duracloud.sync.walker.RestartDirWalker;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.domain.DuracloudConfiguration;
import org.duracloud.syncui.domain.SyncProcessState;
import org.duracloud.syncui.domain.SyncProcessStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The SyncProcessManagerImpl is an implementation of the SyncProcessManager
 * interface. It coordinates the various elements from synctool that perform the
 * synchronization activites.
 * 
 * @author Daniel Bernstein
 */
@Component("syncProcessManager")
public class SyncProcessManagerImpl implements SyncProcessManager {
    private static final int CHANGE_LIST_MONITOR_FREQUENCY = 5000;
    private static final int BACKUP_FREQUENCY = 5*60*1000;

    private static Logger log =
        LoggerFactory.getLogger(SyncProcessManagerImpl.class);
    private InternalState currentState;
    private SyncConfigurationManager syncConfigurationManager;
    private StoppedState stoppedState = new StoppedState();
    private StartingState startingState = new StartingState();
    private RunningState runningState = new RunningState();
    private StoppingState stoppingState = new StoppingState();
    private PausingState pausingState = new PausingState();
    private PausedState pausedState = new PausedState();
    private ResumingState resumingState = new ResumingState();
    
    private EventListenerSupport<SyncStateChangeListener> listeners;
    private SyncProcessStateTransitionValidator syncProcessStateTransitionValidator;

    private SyncManager syncManager;
    private DirWalker dirWalker;
    private DirectoryUpdateMonitor dirMonitor;
    private DeleteChecker deleteChecker;
    private SyncProcessError error;
    private SyncOptimizeManager syncOptimizeManager;
    private ContentStoreManagerFactory contentStoreManagerFactory;
    private Date syncStartedDate = null;
    private ChangedListListener changedListListener;
    private SyncBackupManager syncBackupManager;
    private File backupDir;

    private class InternalChangedListListener implements ChangedListListener {
        @Override
        public void listChanged(final ChangedList list){
            //ignore if there are items in the list
            if(list.getListSize() > 0){
                return;
            }

            //if appears to be empty remove listener
            list.removeListener(InternalChangedListListener.this);

            //in separate thread start shutdown only
            //after  list is absolutely empty (no reserved files)
            //and sync manager has finished transferring files.
            new Thread(new Runnable(){
                @Override
                public void run() {
                    while (!syncManager.getFilesInTransfer().isEmpty() && 
                        list.getListSizeIncludingReservedFiles() > 0) {
                        SyncProcessManagerImpl.this.sleep();
                    }

                    SyncProcessManagerImpl.this.stop();
                }
            }).start();
        }
    }
    
    @Autowired
    public SyncProcessManagerImpl(
        SyncConfigurationManager syncConfigurationManager,
            ContentStoreManagerFactory contentStoreManagerFactory,
            SyncOptimizeManager syncOptimizeManager) {
        this.syncConfigurationManager = syncConfigurationManager;
        this.currentState = this.stoppedState;
        this.listeners = new EventListenerSupport<>(SyncStateChangeListener.class);
        this.syncProcessStateTransitionValidator =
            new SyncProcessStateTransitionValidator();

        this.contentStoreManagerFactory = contentStoreManagerFactory;
        this.syncOptimizeManager = syncOptimizeManager;
        this.backupDir = new File(syncConfigurationManager.getWorkDirectory(), "backup");
        syncBackupManager =
            new SyncBackupManager(this.backupDir,
                                  BACKUP_FREQUENCY,
                                  syncConfigurationManager.retrieveDirectoryConfigs().toFileList());

        ChangedList.getInstance()
                   .addListener(this.changedListListener = new InternalChangedListListener());

    }
    
    @PostConstruct
    public void init(){
      automaticallyRestartIfAppropriate();
    }

    protected void automaticallyRestartIfAppropriate() {
        RuntimeStateMemento m = RuntimeStateMemento.get();
        if (this.syncConfigurationManager.isConfigurationComplete()
            && SyncProcessState.RUNNING.equals(m.getSyncProcessState())) {
            try {
                start();
            } catch (SyncProcessException e) {
                log.error("failed to automatically restart the sync process");
            }
        }
    }

    
    @Override
    public SyncProcessError getError() {
        return this.error;
    }

    @Override
    public void clearError() {
        this.error = null;
    }

    @Override
    public void start() throws SyncProcessException {
        this.currentState.start();
    }

    @Override
    public void resume() throws SyncProcessException {
        this.currentState.resume();
    }

    @Override
    public void stop() {
        this.currentState.stop();
    }

    @Override
    public void pause() {
        this.currentState.pause();
    }
    
    @Override
    public void restart() {
       this.currentState.restart();   
    }

    @Override
    public SyncProcessState getProcessState() {
        return this.currentState.getProcessState();
    }

    @Override
    public SyncProcessStats getProcessStats() {
        return this.currentState.getProcessStats();
    }

    @Override
    public void
        addSyncStateChangeListener(SyncStateChangeListener syncStateChangeListener) {
        this.listeners.addListener(syncStateChangeListener);
    }

    @Override
    public void
        removeSyncStateChangeListener(SyncStateChangeListener syncStateChangeListener) {
        this.listeners.removeListener(syncStateChangeListener);
    }

    private void fireStateChanged(SyncProcessState state) {
        SyncStateChangedEvent event = new SyncStateChangedEvent(state);
        listeners.fire().stateChanged(event);
    }

    private synchronized void changeState(InternalState state) {
        SyncProcessState current = this.currentState.getProcessState();
        SyncProcessState incoming = state.getProcessState();
        boolean validStateChange =
            syncProcessStateTransitionValidator.validate(current, incoming);
        if (validStateChange) {
            this.currentState = state;
            persistState(this.currentState);
            fireStateChanged(this.currentState.getProcessState());
        }
    }

    private void persistState(InternalState currentState) {
        RuntimeStateMemento state = RuntimeStateMemento.get();
        state.setSyncProcessState(currentState.getProcessState());
        RuntimeStateMemento.persist(state);
    }


    private void startImpl() throws SyncProcessException {
        if(syncOptimizeManager.isRunning()){
            String errorMsg = "The transfer rate is currently being optimized.";
            setError(new SyncProcessError(errorMsg));
            return;
        }

        changeState(startingState);
        this.syncStartedDate = new Date();
        setError(null);
        startAsynchronously();
    }
    
    private void startAsynchronously() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    startSyncProcess();
                    changeState(runningState);
                } catch (SyncProcessException e) {
                    log.error("start failed: " + e.getMessage(), e);
                    changeState(stoppingState);
                    changeState(stoppedState);
                }
            }
        }).start();
    }

    private void resumeImpl() throws SyncProcessException {
        changeState(resumingState);
        startAsynchronously();
    }
    
    private void startSyncProcess() throws SyncProcessException {
        DirectoryConfigs directories =
            this.syncConfigurationManager.retrieveDirectoryConfigs();
        
        if(directories.isEmpty()){
            throw new SyncProcessException("unable to start because "+ 
                                           "no watch directories are configured.");
        }
        List<File> dirs = directories.toFileList();

        DuracloudConfiguration dc =
            this.syncConfigurationManager.retrieveDuracloudConfiguration();

        try {
            ContentStoreManager csm = contentStoreManagerFactory.create();
            String username = dc.getUsername();
            String spaceId = dc.getSpaceId();
            csm.login(new Credential(username, dc.getPassword()));
            ContentStore contentStore = csm.getPrimaryContentStore();
            boolean syncDeletes = this.syncConfigurationManager.isSyncDeletes();
            String prefix = this.syncConfigurationManager.getPrefix();
            SyncEndpoint syncEndpoint =
                new DuraStoreChunkSyncEndpoint(contentStore,
                                               username,
                                               spaceId,
                                               syncDeletes,
                                               1073741824, // 1GB chunk size
                                               this.syncConfigurationManager.isSyncUpdates(),
                                               this.syncConfigurationManager.isRenameUpdates(),
                                               this.syncConfigurationManager.isJumpStart(),
                                               this.syncConfigurationManager.getUpdateSuffix(),
                                               prefix);

            
            syncEndpoint.addEndPointListener(new EndPointLogger());
            
            this.backupDir.mkdirs();

            syncBackupManager = new SyncBackupManager(this.backupDir, 
                                                      BACKUP_FREQUENCY, 
                                                      dirs);
            
            long backup = -1;
            if(syncBackupManager.hasBackups()){
                backup = syncBackupManager.attemptRestart();
            }
            
            syncManager = new SyncManager(dirs, syncEndpoint, 
                                          this.syncConfigurationManager.getThreadCount(), // threads
                                          CHANGE_LIST_MONITOR_FREQUENCY); // change list poll frequency
            syncManager.beginSync();

            RunMode mode = this.syncConfigurationManager.getMode();
                
            if(backup < 0){
                dirWalker = DirWalker.start(dirs, null);
            }else if(mode.equals(RunMode.CONTINUOUS)){
                dirWalker = RestartDirWalker.start(dirs, null, backup);
            }
            
            startBackupsOnDirWalkerCompletion();

            dirMonitor =
                new DirectoryUpdateMonitor(dirs,
                                           CHANGE_LIST_MONITOR_FREQUENCY,
                                           syncDeletes);
            
            configureMode(mode);
            if(syncDeletes) {
                deleteChecker = DeleteChecker.start(syncEndpoint,
                                                    spaceId,
                                                    dirs,
                                                    prefix);
            }


        } catch (ContentStoreException e) {
            String message =  StringUtils.abbreviate(e.getMessage(),100);
            handleStartupException(message, e);
        } catch (Exception e){
            String message = StringUtils.abbreviate(e.getMessage(),100);
            handleStartupException(message, e);
        }
    }

    private void startBackupsOnDirWalkerCompletion() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                while(dirWalker != null && !dirWalker.walkComplete()){
                    sleep(100);
                }
                log.info("Starting back up manager...");
                syncBackupManager.startupBackups();
                
            }
        }, "walk-completion-checker thread").start();
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
    
    private void configureMode(RunMode mode) {
        // only start the dirMonitor if the sync config manager is
        // set to run continuously.
        try {
            if (mode.equals(RunMode.CONTINUOUS)) {
                dirMonitor.startMonitor();
                ChangedList.getInstance().removeListener(changedListListener);
            } else {
                ChangedList.getInstance().addListener(changedListListener);
                dirMonitor.stopMonitor();
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void handleStartupException(String message, Exception e)
        throws SyncProcessException {
        log.error(message, e);
        setError(new SyncProcessError(message));
        shutdownSyncProcess();
        changeState(stoppingState);
        changeState(stoppedState);
        throw new SyncProcessException(message, e);
    }

    private void setError(SyncProcessError error) {
        this.error = error;
    }

    private SyncProcessStats getProcessStatsImpl() {
        int queueSize = ChangedList.getInstance().getListSize();
        int errorSize = StatusManager.getInstance().getFailed().size();
        return new SyncProcessStats(this.syncStartedDate,
                                    null,
                                    errorSize,
                                    0,
                                    0,
                                    queueSize);
    }

    private void shutdownSyncProcess() {
        if(this.syncManager != null){
            this.syncManager.terminateSync();
        }
        try{
            if(this.dirMonitor != null){
                this.dirMonitor.stopMonitor();
            }
        }catch(Exception ex){
            log.warn("stop monitor failed: " + ex.getMessage());
        }
        
        if(this.deleteChecker != null){
            this.deleteChecker.stop();
        }

        if(this.dirWalker != null){
            this.dirWalker.stopWalk();
        }
    }
    
    private void resetChangeList() {
        ChangedList.getInstance().clear();
        syncBackupManager.endBackups();
        syncBackupManager.clearBackups();
    }
    
    @SuppressWarnings("unused")
    private class InternalListener implements SyncStateChangeListener {
        private CountDownLatch latch = new CountDownLatch(1);
        private SyncProcessState state;

        public InternalListener(SyncProcessState state) {
            this.state = state;
        }

        @Override
        public void stateChanged(SyncStateChangedEvent event) {
            if (event.getProcessState() == this.state) {
                latch.countDown();
            }
        }

        private void waitForStateChange() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private void stopImpl()  {
        changeState(stoppingState);
        new Thread() {
            @Override
            public void run() {
                shutdownSyncProcess();
                syncStartedDate = null;
                while (!syncManager.getFilesInTransfer().isEmpty()) {
                    SyncProcessManagerImpl.this.sleep();
                }
                
                resetChangeList();
                changeState(stoppedState);
            }
        }.start();
    }
    
    protected void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void pauseImpl() {
        changeState(pausingState);
        final Thread t = new Thread() {
            @Override
            public void run() {
                shutdownSyncProcess();
                SyncManager sm = SyncProcessManagerImpl.this.syncManager;
                while (!sm.getFilesInTransfer().isEmpty()) {
                    SyncProcessManagerImpl.this.sleep();
                }

                changeState(pausedState);
            }
        };

        t.start();
    }

    // internally the SyncProcessManagerImpl makes use of the Gof4 State
    // pattern.
    private abstract class InternalState implements SyncProcess {
        @Override
        public void start() throws SyncProcessException {
            // do nothing by default
        }

        @Override
        public void stop() {
            // do nothing by default
        }

        @Override
        public void resume() throws SyncProcessException{
            // do nothing by default
        }

        @Override
        public void pause() {
            //do nothing by default
        }
        
        @Override 
        public void restart(){
            //do nothing by default
        }

        @Override
        public SyncProcessStats getProcessStats() {
            return getProcessStatsImpl();
        }
    }

    private class StoppedState extends InternalState {
        @Override
        public void start() throws SyncProcessException {
            startImpl();
        }

        @Override
        public SyncProcessState getProcessState() {
            return SyncProcessState.STOPPED;
        }
    }

    
    private class PausedState extends InternalState {
        @Override
        public void resume() throws SyncProcessException {
            resumeImpl();
        }

        @Override
        public void stop() {
            stopImpl();
        }

        @Override
        public SyncProcessState getProcessState() {
            return SyncProcessState.PAUSED;
        }
    }

    
    private class StartingState extends InternalState {
        @Override
        public SyncProcessState getProcessState() {
            return SyncProcessState.STARTING;
        }
    }

    private class ResumingState extends InternalState {
        @Override
        public SyncProcessState getProcessState() {
            return SyncProcessState.RESUMING;
        }
    }

    private class RunningState extends InternalState {

        @Override
        public void stop() {
            stopImpl();
        }
        
        @Override
        public void pause() {
            pauseImpl();
        }
        
        @Override
        public void restart() {
            //register stopped listener
            addSyncStateChangeListener(new SyncStateChangeListener() {
                @Override
                public void stateChanged(SyncStateChangedEvent event) {
                    if(event.getProcessState().equals(SyncProcessState.STOPPED)){
                        try {
                            removeSyncStateChangeListener(this);
                            startImpl();
                        } catch (SyncProcessException e) {
                            log.warn(e.getMessage(), e);
                        }
                    }
                }
            });
            
            //invoke stop
            stopImpl();
            
        }

        @Override
        public SyncProcessState getProcessState() {
            return SyncProcessState.RUNNING;
        }
    }

    private class StoppingState extends InternalState {
        @Override
        public SyncProcessState getProcessState() {
            return SyncProcessState.STOPPING;
        }
    }

    private class PausingState extends InternalState {
        @Override
        public SyncProcessState getProcessState() {
            return SyncProcessState.PAUSING;
        }
    }

    @Override
    public List<MonitoredFile> getMonitoredFiles() {
        if (this.syncManager != null) {
            return this.syncManager.getFilesInTransfer();
        }
        return new LinkedList<MonitoredFile>();
    }


    @Override
    public List<SyncSummary> getFailures() {
        if (this.syncManager != null) {
            return StatusManager.getInstance().getFailed();
        }
        return new LinkedList<SyncSummary>();
    }

    @Override
    public List<SyncSummary> getRecentlyCompleted() {
        if (this.syncManager != null) {
            return StatusManager.getInstance().getRecentlyCompleted();
        }
        return new LinkedList<SyncSummary>();
    }


    @Override
    public List<File> getQueuedFiles() {
        return ChangedList.getInstance().peek(10);
    }
    
    @Override
    public void clearFailures() {
        StatusManager.getInstance().clearFailed();
    }
}
