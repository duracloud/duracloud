/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync;

import org.duracloud.sync.backup.SyncBackupManager;
import org.duracloud.sync.config.SyncToolConfig;
import org.duracloud.sync.config.SyncToolConfigParser;
import org.duracloud.sync.endpoint.DuraStoreChunkSyncEndpoint;
import org.duracloud.sync.endpoint.SyncEndpoint;
import org.duracloud.sync.mgmt.StatusManager;
import org.duracloud.sync.mgmt.SyncManager;
import org.duracloud.sync.monitor.DirectoryUpdateMonitor;
import org.duracloud.sync.util.LogUtil;
import org.duracloud.sync.walker.DirWalker;
import org.duracloud.sync.walker.DeleteChecker;
import org.duracloud.sync.walker.RestartDirWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
 * pointing to the same backup directory it will load its previous state and
 * look through the local file system for files which have changed since it
 * performed its last backup, which it will then sync with DuraCloud.
 *
 * @author: Bill Branan
 * Date: Mar 11, 2010
 */
public class SyncTool {

    private final Logger logger = LoggerFactory.getLogger(SyncTool.class);
    private SyncToolConfig syncConfig;
    private SyncManager syncManager;
    private SyncBackupManager syncBackupManager;
    private DirectoryUpdateMonitor dirMonitor;
    private SyncEndpoint syncEndpoint;
    private LogUtil logUtil;

    private SyncToolConfig processCommandLineArgs(String[] args) {
        SyncToolConfigParser syncConfigParser = new SyncToolConfigParser();
        syncConfig = syncConfigParser.processCommandLine(args);
        return syncConfig;
    }

    /**
     * Determines if the sync directory list has been changed since the
     * previous run. If it has, a restart cannot occur.
     * @return true if sync directories have not been changed, false otherwise
     */
    private boolean restartPossible() {
        SyncToolConfigParser syncConfigParser = new SyncToolConfigParser();
        SyncToolConfig prevConfig =
            syncConfigParser.retrievePrevConfig(syncConfig.getBackupDir());

        if(prevConfig != null) {
            return syncConfig.getSyncDirs().equals(prevConfig.getSyncDirs());
        } else {
            return false;
        }
    }

    private void setupLogging(){
        logUtil = new LogUtil();
        logUtil.setupLogger(syncConfig.getBackupDir());
    }

    private void startSyncManager() {
        syncEndpoint = 
            new DuraStoreChunkSyncEndpoint(syncConfig.getHost(),
                                           syncConfig.getPort(),
                                           syncConfig.getContext(),
                                           syncConfig.getUsername(),
                                           syncConfig.getPassword(),
                                           syncConfig.getSpaceId(),
                                           syncConfig.syncDeletes(),
                                           syncConfig.getMaxFileSize());
        syncManager = new SyncManager(syncConfig.getSyncDirs(),
                                      syncEndpoint,
                                      syncConfig.getNumThreads(),
                                      syncConfig.getPollFrequency());
        syncManager.beginSync();
    }

    private long startSyncBackupManager(boolean restart) {
        syncBackupManager =
            new SyncBackupManager(syncConfig.getBackupDir(),
                                  syncConfig.getPollFrequency());
        long lastBackup = 0;
        if(restart) {
            lastBackup = syncBackupManager.attemptRestart();
        }
        syncBackupManager.startupBackups();
        return lastBackup;
    }

    private void startDirWalker() {
        DirWalker.start(syncConfig.getSyncDirs());
    }

    private void startRestartDirWalker(long lastBackup) {
        RestartDirWalker.start(syncConfig.getSyncDirs(), lastBackup);
    }

    private void startDeleteChecker() {
        DeleteChecker.start(syncEndpoint.getFilesList(),
                            syncConfig.getSyncDirs());
    }

    private void startDirMonitor() {
        dirMonitor = new DirectoryUpdateMonitor(syncConfig.getSyncDirs(),
                                                syncConfig.getPollFrequency(),
                                                syncConfig.syncDeletes());
        dirMonitor.startMonitor();
    }

    private void listenForExit() {
        StatusManager statusManager = StatusManager.getInstance();
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
                } else if(input.startsWith("l ")) {
                    logUtil.setLogLevel(input.substring(2));
                    System.out.println("Log level set to " +
                                       logUtil.getLogLevel());
                } else {
                    System.out.println(getPrintableHelp());
                }
            } catch(IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        closeSyncTool();
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

    public void runSyncTool(SyncToolConfig syncConfig) {
        this.syncConfig = syncConfig;
        setupLogging();
        System.out.print("\nStarting up the Sync Tool ...");
        startSyncManager();

        System.out.print("...");
        boolean restart = restartPossible();
        System.out.print("...");
        long lastBackup = startSyncBackupManager(restart);
        System.out.print("...");
        if(restart && lastBackup > 0) {
            logger.info("Running Sync Tool re-start file check");
            startRestartDirWalker(lastBackup);
            System.out.print("...");
        } else {
            logger.info("Running Sync Tool complete file check");
            startDirWalker();
            System.out.print("...");
        }

        if(syncConfig.syncDeletes()) {
            startDeleteChecker();
        }
        System.out.print("...");

        startDirMonitor();
        System.out.println("... Startup Complete");
        printWelcome();
        listenForExit();
    }

    private void printWelcome() {
        System.out.println(syncConfig.getPrintableConfig());
        System.out.println(getPrintableHelp());
    }

    public String getPrintableHelp() {
        StringBuilder help = new StringBuilder();

        help.append("\n--------------------------------------\n");
        help.append(" Sync Tool Help");
        help.append("\n--------------------------------------\n");

        help.append("The following commands are available:\n");
        help.append("x - Exits the Sync Tool\n");
        help.append("c - Prints the Sync Tool configuration\n");
        help.append("s - Prints the Sync Tool status\n");
        help.append("l <Level> - Changes the log level to <Level> (may ");
        help.append("be any of DEBUG, INFO, WARN, ERROR)\n");
        help.append("Location of logs: " + logUtil.getLogLocation() + "\n");
        help.append("--------------------------------------\n");
        
        return help.toString();
    }

    public static void main(String[] args) throws Exception {
        SyncTool syncTool = new SyncTool();
        SyncToolConfig syncConfig = syncTool.processCommandLineArgs(args);
        syncTool.runSyncTool(syncConfig);
    }
}
