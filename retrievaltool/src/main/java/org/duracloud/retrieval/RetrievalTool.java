/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval;

import org.duracloud.client.ContentStore;
import org.duracloud.common.util.ApplicationConfig;
import org.duracloud.error.ContentStoreException;
import org.duracloud.retrieval.config.RetrievalToolConfig;
import org.duracloud.retrieval.config.RetrievalToolConfigParser;
import org.duracloud.retrieval.mgmt.CSVFileOutputWriter;
import org.duracloud.retrieval.mgmt.OutputWriter;
import org.duracloud.retrieval.mgmt.RetrievalManager;
import org.duracloud.retrieval.mgmt.SpaceListManager;
import org.duracloud.retrieval.mgmt.StatusManager;
import org.duracloud.retrieval.source.DuraStoreStitchingRetrievalSource;
import org.duracloud.retrieval.source.RetrievalSource;
import org.duracloud.retrieval.util.LogUtil;
import org.duracloud.retrieval.util.StoreClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Starting point for the Retrieval Tool. The purpose of this tool is to
 * retrieve content files or content listings from DuraCloud. When the Retrieval
 * Tool is started and the '-l' or '--list-only' option is not specified, it
 * will connect to DuraCloud and proceed to copy files from the provided list
 * of spaces to the local file system under the content directory. Any files
 * which already exist locally will be compared (via checksum) with the file in
 * DuraCloud. If the files are different the local file will either be renamed
 * or overwritten, depending on whether the overwrite flag is set.
 * If the '-l' or '--list-only' option is specified the the Retrieval Tool will
 * create a text file, in the content directory, for each space provided and
 * list each content ID in the space, one content ID per line.
 *
 * Once all files have been transferred to the local system the Retrieval Tool
 * will exit. This tool (currently) provides no ongoing synchronization between
 * DuraCloud and the local system.
 *
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public class RetrievalTool {

    private static final String RETRIEVALTOOL_PROPERTIES =
        "retrievaltool.properties";

    private final Logger logger = LoggerFactory.getLogger(RetrievalTool.class);
    private RetrievalToolConfig retConfig;
    private ExecutorService executor;
    private OutputWriter outWriter;
    private RetrievalManager retManager;
    private RetrievalSource retSource;
    private LogUtil logUtil;
    private String version;

    public RetrievalTool() {
        Properties props =
            ApplicationConfig.getPropsFromResource(RETRIEVALTOOL_PROPERTIES);
        this.version = props.getProperty("version");
    }

    private RetrievalToolConfig processCommandLineArgs(String[] args) {
        RetrievalToolConfigParser retConfigParser =
            new RetrievalToolConfigParser();
        retConfig = retConfigParser.processCommandLine(args);
        retConfig.setVersion(version);

        logger.info("Running Retrieval Tool with configuration: " +
                    retConfig.getPrintableConfig());

        return retConfig;
    }

    private void setupLogging(){
        File workDir = retConfig.getWorkDir();
        if(!workDir.exists()) {
            workDir.mkdirs();
            workDir.setWritable(true);
        }

        logUtil = new LogUtil();
        logUtil.setupLogger(workDir);
    }

    private void startRetrievalManager(ContentStore contentStore) {
        retSource =
            new DuraStoreStitchingRetrievalSource(contentStore,
                                         retConfig.getSpaces(),
                                         retConfig.isAllSpaces());

        outWriter = new CSVFileOutputWriter(retConfig.getWorkDir());
        boolean createSpaceDir = isCreateSpaceDir();
        boolean applyTimestamps = retConfig.isApplyTimestamps() ;
        retManager = new RetrievalManager(retSource,
                                          retConfig.getContentDir(),
                                          retConfig.getWorkDir(),
                                          retConfig.isOverwrite(),
                                          retConfig.getNumThreads(),
                                          outWriter,
                                          createSpaceDir,
                                          applyTimestamps);

        executor.execute(retManager);
    }

    private boolean isCreateSpaceDir() {
        boolean createDir = retConfig.isAllSpaces();
        if(! createDir) {
            createDir = retConfig.getSpaces().size() > 1;
        }
        return createDir;
    }

    private void waitForExit() {
        StatusManager statusManager = StatusManager.getInstance();
        statusManager.setVersion(version);

        int loops = 0;
        while(!retManager.isComplete()) {
            if(loops >= 60) { // Print status every 10 minutes
                System.out.println(statusManager.getPrintableStatus());
                loops = 0;
            } else {
                loops++;
            }
            sleep(10000);
        }

        logger.info("Shutting down the Retrieval Tool");
        
        outWriter.close();
        executor.shutdown();
        System.out.println("Retrieval Tool processing complete, final status:");
        System.out.println(statusManager.getPrintableStatus());
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private void startSpaceListManager(ContentStore contentStore)
        throws ContentStoreException {
        List<String> spaces = null;
        if(retConfig.isAllSpaces()) {
            spaces = contentStore.getSpaces();
        } else {
            spaces = retConfig.getSpaces();
        }
        SpaceListManager spaceListManager = new SpaceListManager(contentStore,
                                                                 retConfig.getContentDir(),
                                                                 spaces,
                                                                 retConfig.isOverwrite(),
                                                                 retConfig.getNumThreads());
        executor.execute(spaceListManager);
        while(!spaceListManager.isComplete()) {
            sleep(1000);
        }
        executor.shutdown();
    }

    public void runRetrievalTool(RetrievalToolConfig retConfig) {
        this.retConfig = retConfig;
        setupLogging();
        logger.info("Starting Retrieval Tool version " + version);
        System.out.print("\nStarting up the Retrieval Tool ...");
        System.out.println(retConfig.getPrintableConfig());

        StoreClientUtil clientUtil = new StoreClientUtil();
        ContentStore contentStore =
            clientUtil.createContentStore(retConfig.getHost(),
                                          retConfig.getPort(),
                                          retConfig.getContext(),
                                          retConfig.getUsername(),
                                          retConfig.getPassword(),
                                          retConfig.getStoreId());

        executor = Executors.newFixedThreadPool(1);
        if(retConfig.isListOnly()) {
            try {
                startSpaceListManager(contentStore);
            } catch(ContentStoreException cse) {
                String error = "Error: could not retrieve list of spaces.\n" +
                               "Error Message: " + cse.getMessage();
                System.err.println(error);
                logger.error(error, cse);
            }
        } else {
            startRetrievalManager(contentStore);
            System.out.println("... Startup Complete");
            System.out.println("The Retrieval Tool will exit when processing " +
                           "is complete. Status will be printed every " +
                           "10 minutes.\n");
            waitForExit();
        }
    }

    public static void main(String[] args) throws Exception {
        RetrievalTool retTool = new RetrievalTool();
        RetrievalToolConfig retConfig = retTool.processCommandLineArgs(args);
        retTool.runRetrievalTool(retConfig);
    }
}
