/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.duracloud.common.util.ApplicationConfig;
import org.duracloud.syncoptimize.config.SyncOptimizeConfig;
import org.duracloud.syncoptimize.config.SyncOptimizeConfigParser;
import org.duracloud.syncoptimize.data.TestDataHandler;
import org.duracloud.syncoptimize.status.SyncTestStatus;
import org.duracloud.syncoptimize.test.SyncTestManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the starting point the for Sync optimizer. The purpose of this tool
 * is to determine the optimal number of threads that should be used to run
 * the DuraCloud SyncTool such that the greatest transfer rate possible is
 * achieved.
 *
 * @author Bill Branan
 * Date: 5/16/14
 */
public class SyncOptimizeDriver {

    private static final String SYNCOPT_PROPERTIES = "syncopt.properties";
    private static final String DATA_DIR_NAME = "duracloud-sync-optimize-data";
    private static final String WORK_DIR_NAME = "duracloud-sync-optimize-work";

    private final Logger log = LoggerFactory.getLogger(SyncOptimizeDriver.class);

    /* Picks up the tool version, which is printed on tool startup */
    private String version;
    /* Where test content is stored */
    private File dataDir;
    /* Where logs, config, and other runtime information is stored */
    private File workDir;

    private SyncTestStatus syncTestStatus;

    /**
     * Creates the SyncToolDriver
     *
     * @param printStatus indicates whether or not status information should be
     *                    printed to the console while the tool is running
     */
    public SyncOptimizeDriver(boolean printStatus) {
        Properties props =
            ApplicationConfig.getPropsFromResource(SYNCOPT_PROPERTIES);
        this.version = props.getProperty("version");
        this.syncTestStatus = new SyncTestStatus(printStatus);
    }

    /**
     * Determines the optimal SyncTool thread count value. This value is
     * discovered by running a series of timed tests and returning the fastest
     * performer. The results of these tests depend highly on the machine they
     * are run on, and the capacity of the network available to that machine.
     *
     * @param syncOptConfig tool configuration
     * @return optimal thread count
     * @throws IOException
     */
    public int getOptimalThreads(SyncOptimizeConfig syncOptConfig)
        throws IOException {
        File tempDir = FileUtils.getTempDirectory();
        this.dataDir = new File(tempDir, DATA_DIR_NAME);
        this.workDir = new File(tempDir, WORK_DIR_NAME);

        String prefix = "sync-optimize/" +
                        InetAddress.getLocalHost().getHostName() + "/";

        TestDataHandler dataHandler = new TestDataHandler();
        dataHandler.createDirectories(dataDir, workDir);
        dataHandler.createTestData(dataDir,
                                   syncOptConfig.getNumFiles(),
                                   syncOptConfig.getSizeFiles());

        SyncTestManager testManager =
            new SyncTestManager(syncOptConfig, dataDir, workDir,
                                syncTestStatus, prefix);
        int optimalThreads = testManager.runTest();

        dataHandler.removeDirectories(dataDir, workDir);

        return optimalThreads;
    }

    /**
     * Gets a listing of status events that have occurred (mostly test
     * completion events) as the tests are running.
     *
     * @return sync status
     */
    public SyncTestStatus getSyncTestStatus() {
        return syncTestStatus;
    }

    private SyncOptimizeConfig processCommandLineArgs(String[] args) {
        SyncOptimizeConfigParser configParser = new SyncOptimizeConfigParser();
        SyncOptimizeConfig syncOptConfig = configParser.processCommandLine(args);
        syncOptConfig.setVersion(version);

        log.info("### Running Sync Thread Optimizer with configuration: " +
                 syncOptConfig.getPrintableConfig());

        return syncOptConfig;
    }

    /**
     * Picks up the command line parameters and kicks off the optimization tests
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        SyncOptimizeDriver syncOptDriver = new SyncOptimizeDriver(true);
        SyncOptimizeConfig syncOptConfig =
            syncOptDriver.processCommandLineArgs(args);

        System.out.println("### Running Sync Thread Optimizer with configuration: " +
                           syncOptConfig.getPrintableConfig());

        int optimalThreads = syncOptDriver.getOptimalThreads(syncOptConfig);

        System.out.println("### Sync Thread Optimizer complete. Optimal thread " +
                           "count for running the DuraCloud SyncTool on " +
                           "this machine is: " + optimalThreads);
    }
}
