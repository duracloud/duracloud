/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.duracloud.client.ContentStore;
import org.duracloud.client.util.StoreClientUtil;
import org.duracloud.common.util.ApplicationConfig;
import org.duracloud.sync.backup.SyncBackupManager;
import org.duracloud.sync.config.SyncToolConfig;
import org.duracloud.sync.config.SyncToolConfigParser;
import org.duracloud.sync.endpoint.DuraStoreChunkSyncEndpoint;
import org.duracloud.sync.endpoint.EndPointLogger;
import org.duracloud.sync.endpoint.SyncEndpoint;
import org.duracloud.sync.mgmt.ChangedList;
import org.duracloud.sync.mgmt.StatusManager;
import org.duracloud.sync.mgmt.SyncManager;
import org.duracloud.sync.monitor.DirectoryUpdateMonitor;
import org.duracloud.sync.walker.DeleteChecker;
import org.duracloud.sync.walker.DirWalker;
import org.duracloud.sync.walker.RestartDirWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starting point for the Sync Tool. The purpose of this tool is to synchronize
 * all of the files in a given set of local file system directories with a
 * space in DuraCloud. This means that as files are added, updated, and deleted
 * locally, the Sync Tool will perform the same activities on the files within
 * DuraCloud.
 *
 * When the Sync Tool is started for the first time, it will consider all local
 * files under the given directories (recursively) and determine if those files
 * already exist in the DuraCloud space. If not, the files will be added. At
 * that point, the Sync Tool will monitor for updates on the local file system
 * directories and make updates as needed.
 *
 * If the Sync Tool is turned off or exits for some reason, and is started again
 * pointing to the same work directory it will load its previous state and
 * look through the local file system for files which have changed since it
 * performed its last backup, which it will then sync with DuraCloud.
 *
 * @author: Bill Branan
 * Date: Mar 11, 2010
 */
public class SyncTool {

    private static final String SYNCTOOL_PROPERTIES = "synctool.properties";

    private final Logger logger = LoggerFactory.getLogger(SyncTool.class);
    private SyncToolConfig syncConfig;
    private SyncManager syncManager;
    private SyncBackupManager syncBackupManager;
    private DirectoryUpdateMonitor dirMonitor;
    private SyncEndpoint syncEndpoint;
    private DirWalker dirWalker;
    private DeleteChecker deleteChecker;
    private String version;

    public SyncTool() {
        Properties props =
            ApplicationConfig.getPropsFromResource(SYNCTOOL_PROPERTIES);
        this.version = props.getProperty("version");
    }

    /**
     * Sets the configuration of the sync tool.
     * @param syncConfig to use for running the Sync Tool
     */
    protected void setSyncConfig(SyncToolConfig syncConfig) {
        this.syncConfig = syncConfig;
        this.syncConfig.setVersion(version);
    }

    /**
     * Determines if this run of the sync tool will perform a restart.
     * @return true if restart should occur, false otherwise
     */
    protected boolean restartPossible() {
        boolean restart = false;
        if(!syncConfig.isCleanStart()) {
            // Determines if the configuration has been changed since the
            // previous run. If it has, a restart cannot occur.
            SyncToolConfigParser syncConfigParser = new SyncToolConfigParser();
            SyncToolConfig prevConfig =
                syncConfigParser.retrievePrevConfig(syncConfig.getWorkDir());

            if(prevConfig != null) {
                restart = configEquals(syncConfig, prevConfig);
            }
        }
        return restart;
    }

    /**
     * Determines if two sets of configuration are "equal enough" to indicate
     * that the content to be reviewed for sync operations has not changed on
     * either the local or remote end, suggesting that a re-start would be
     * permissable.
     *
     * @param currConfig the current sync configuration
     * @param prevConfig the sync configuration of the previous run
     * @return true if the configs are "equal enough", false otherwise
     */
    protected boolean configEquals(SyncToolConfig currConfig,
                                   SyncToolConfig prevConfig) {
        boolean sameHost = currConfig.getHost().equals(prevConfig.getHost());
        boolean sameSpaceId =
            currConfig.getSpaceId().equals(prevConfig.getSpaceId());
        boolean sameStoreId = sameConfig(currConfig.getStoreId(),
                                         prevConfig.getStoreId());
        boolean sameSyncDeletes =
            currConfig.syncDeletes() == prevConfig.syncDeletes();
        boolean sameContentDirs =
            currConfig.getContentDirs().equals(prevConfig.getContentDirs());
        boolean sameSyncUpdates =
            currConfig.isSyncUpdates() == prevConfig.isSyncUpdates();
        boolean sameRenameUpdates =
            currConfig.isRenameUpdates() == prevConfig.isRenameUpdates();
        boolean sameExclude = sameConfig(currConfig.getExcludeList(),
                                         prevConfig.getExcludeList());
        boolean samePrefix = sameConfig(currConfig.getPrefix(),
                                        prevConfig.getPrefix());

        if(sameHost && sameSpaceId && sameStoreId && sameSyncDeletes &&
           sameContentDirs && sameSyncUpdates && sameRenameUpdates &&
           sameExclude && samePrefix) {
            return true;
        }
        return false;
    }

    private boolean sameConfig(Object nowConfig, Object prevConfig) {
        if((null == nowConfig && null == prevConfig) ||
           (null != nowConfig && nowConfig.equals(prevConfig))) {
            return true;
        }
        return false;
    }

    private void startSyncManager() {
        StoreClientUtil clientUtil = new StoreClientUtil();
        ContentStore contentStore =
            clientUtil.createContentStore(syncConfig.getHost(),
                                          syncConfig.getPort(),
                                          syncConfig.getContext(),
                                          syncConfig.getUsername(),
                                          syncConfig.getPassword(),
                                          syncConfig.getStoreId());

        syncEndpoint = 
            new DuraStoreChunkSyncEndpoint(contentStore,
                                           syncConfig.getUsername(),
                                           syncConfig.getSpaceId(),
                                           syncConfig.syncDeletes(),
                                           syncConfig.getMaxFileSize(),
                                           syncConfig.isSyncUpdates(),
                                           syncConfig.isRenameUpdates(),
                                           syncConfig.isJumpStart(),
                                           syncConfig.getUpdateSuffix(),
                                           syncConfig.getPrefix());
        
        this.syncEndpoint.addEndPointListener(new EndPointLogger());
        
        syncManager = new SyncManager(syncConfig.getContentDirs(),
                                      syncEndpoint,
                                      syncConfig.getNumThreads(),
                                      syncConfig.getPollFrequency());
        syncManager.beginSync();
    }

    private void startDirWalker() {
        dirWalker = DirWalker.start(syncConfig.getContentDirs(),
                                    syncConfig.getExcludeList());
    }

    private void startRestartDirWalker(long lastBackup) {
        dirWalker = RestartDirWalker.start(syncConfig.getContentDirs(),
                                           syncConfig.getExcludeList(),
                                           lastBackup);
    }

    private void startDeleteChecker() {
        deleteChecker = DeleteChecker.start(syncEndpoint,
                                            syncConfig.getSpaceId(),
                                            syncConfig.getContentDirs(),
                                            syncConfig.getPrefix());
    }

    private void startDirMonitor() {
        dirMonitor = new DirectoryUpdateMonitor(syncConfig.getContentDirs(),
                                                syncConfig.getPollFrequency(),
                                                syncConfig.syncDeletes());
        dirMonitor.startMonitor();
    }

    private void listenForExit() {
        StatusManager statusManager = StatusManager.getInstance();
        statusManager.setVersion(version);
        BufferedReader br =
            new BufferedReader(new InputStreamReader(System.in));
        boolean exit = false;
        while(!exit) {
            String input;
            try {
                input = br.readLine();
                if(input.equalsIgnoreCase("exit") ||
                   input.equalsIgnoreCase("x")) {
                    exit = true;
                } else if(input.equalsIgnoreCase("config") ||
                          input.equalsIgnoreCase("c")) {
                    System.out.println(syncConfig.getPrintableConfig());
                } else if(input.equalsIgnoreCase("status") ||
                          input.equalsIgnoreCase("s")) {
                    System.out.println(statusManager.getPrintableStatus());
                } else {
                    System.out.println(getPrintableHelp());
                }
            } catch(IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        closeSyncTool();
    }

    private void waitForExit() {
        StatusManager statusManager = StatusManager.getInstance();
        statusManager.setVersion(version);
        boolean syncDeletes = syncConfig.syncDeletes();

        int loops = 0;
        boolean exit = false;
        while(!exit) {
            if(dirWalker.walkComplete()) {
                if(!syncDeletes ||
                   (syncDeletes && deleteChecker.checkComplete())) {
                    if(ChangedList.getInstance().getListSize() <= 0) {
                        if(statusManager.getInWork() <= 0) {
                            exit = true;
                            System.out.println(
                                "Sync Tool processing complete, final status:");
                            System.out.println(
                                statusManager.getPrintableStatus());
                            break;
                        }
                    }
                }
            }
            if(loops >= 60) { // Print status every 10 minutes
                System.out.println(statusManager.getPrintableStatus());
                loops = 0;
            } else {
                loops++;
            }
            sleep(10000);
        }
        closeSyncTool();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private void closeSyncTool() {
        syncBackupManager.endBackups();
        syncManager.endSync();
        dirMonitor.stopMonitor();

        long inWork = StatusManager.getInstance().getInWork();
        if(inWork > 0) {
            System.out.println("\nThe Sync Tool will exit after the remaining "
                               + inWork + " work items have completed\n");
        }
    }

    public void runSyncTool() {
        logger.info("Starting Sync Tool version " + version);
        logger.info("Running Sync Tool with configuration: " +
                    syncConfig.getPrintableConfig());
        System.out.print("\nStarting up the Sync Tool ...");
        startSyncManager();

        System.out.print("...");
        boolean restart = restartPossible();
        System.out.print("...");

        File backupDir = new File(syncConfig.getWorkDir(), "backup");
        backupDir.mkdirs();
        syncBackupManager =
            new SyncBackupManager(backupDir,
                                  syncConfig.getPollFrequency(),
                                  syncConfig.getContentDirs());

        boolean hasABackupFile = this.syncBackupManager.hasBackups();

        if(restart && hasABackupFile){
            //attempt restart
            long lastBackup = syncBackupManager.attemptRestart();
            System.out.print("...");
            if(lastBackup > 0) {
                logger.info("Running Sync Tool re-start file check");
                startRestartDirWalker(lastBackup);
                System.out.print("...");
            }
        }

        if(dirWalker == null){
            logger.info("Running Sync Tool complete file check");
            startDirWalker();
        }

        startBackupsOnDirWalkerCompletion();
        
        if(syncConfig.syncDeletes()) {
            startDeleteChecker();
        }
        System.out.print("...");

        startDirMonitor();
        System.out.println("... Startup Complete");

        if(syncConfig.exitOnCompletion()) {
            System.out.println(syncConfig.getPrintableConfig());
            System.out.println("The Sync Tool will exit when processing " +
                               "is complete. Status will be printed every " +
                               "10 minutes.\n");
            waitForExit();
        } else {
            printWelcome();
            listenForExit();
        }
    }

    private void startBackupsOnDirWalkerCompletion() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                while(!dirWalker.walkComplete()){
                    sleep(100);
                }
                logger.info("Walk complete: starting back up manager...");
                syncBackupManager.startupBackups();
                
            }
        }, "walk-completion-checker thread").start();
    }


    private void printWelcome() {
        System.out.println(syncConfig.getPrintableConfig());
        System.out.println(getPrintableHelp());
    }

    public String getPrintableHelp() {
        StringBuilder help = new StringBuilder();

        help.append("\n-------------------------------------------\n");
        help.append(" Sync Tool " + version + " - Help");
        help.append("\n-------------------------------------------\n");

        help.append("The following commands are available:\n");
        help.append("x - Exits the Sync Tool\n");
        help.append("c - Prints the Sync Tool configuration\n");
        help.append("s - Prints the Sync Tool status\n");
        help.append("-------------------------------------------\n");
        
        return help.toString();
    }
}
