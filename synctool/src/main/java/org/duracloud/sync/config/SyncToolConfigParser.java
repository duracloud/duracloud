/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading the configuration parameters for the Sync Tool
 *
 * @author: Bill Branan
 * Date: Mar 15, 2010
 */
public class SyncToolConfigParser {

    protected static final long GIGABYTE = 1073741824;

    protected static final String BACKUP_FILE_NAME = "synctool.config";
    protected static final String PREV_BACKUP_FILE_NAME = "synctool.config.bak";

    protected static final int DEFAULT_PORT = 443;
    protected static final long DEFAULT_POLL_FREQUENCY = 10000;
    protected static final int DEFAULT_NUM_THREADS = 3;
    protected static final int DEFAULT_MAX_FILE_SIZE = 1; // 1 GB
    protected static final String context = "durastore";

    private Options cmdOptions;
    private Options configFileOptions;

    /**
     * Creates a parser for command line configuration options.
     */
    public SyncToolConfigParser() {
       // Command Line Options
       cmdOptions = new Options();

       Option hostOption =
           new Option("h", "host", true,
                      "the host address of the DuraCloud " +
                      "DuraStore application");
       hostOption.setRequired(true);
       cmdOptions.addOption(hostOption);

       Option portOption =
           new Option("r", "port", true,
                      "the port of the DuraCloud DuraStore application " +
                      "(optional, default value is " + DEFAULT_PORT + ")");
       portOption.setRequired(false);
       cmdOptions.addOption(portOption);

       Option usernameOption =
           new Option("u", "username", true,
                      "the username necessary to perform writes to DuraStore");
       usernameOption.setRequired(true);
       cmdOptions.addOption(usernameOption);

       Option passwordOption =
           new Option("p", "password", true,
                      "the password necessary to perform writes to DuraStore");
       passwordOption.setRequired(true);
       cmdOptions.addOption(passwordOption);

        Option storeIdOption =
            new Option("i", "store-id", true,
                       "the Store ID for the DuraCloud storage provider");
        storeIdOption.setRequired(false);
        cmdOptions.addOption(storeIdOption);        

       Option spaceId =
           new Option("s", "space-id", true,
                      "the ID of the DuraCloud space where content " +
                      "will be stored");
       spaceId.setRequired(true);
       cmdOptions.addOption(spaceId);

       Option workDirOption =
           new Option("w", "work-dir", true,
                      "the state of the sync tool is persisted to " +
                      "this directory (optional, default value is " +
                      "duracloud-sync-work directory in user home)");
       workDirOption.setRequired(false);
       cmdOptions.addOption(workDirOption);

       Option contentDirs =
           new Option("c", "content-dirs", true,
                      "the directory paths to monitor and sync with DuraCloud");
       contentDirs.setRequired(true);
       contentDirs.setArgs(Option.UNLIMITED_VALUES);
       cmdOptions.addOption(contentDirs);

       Option pollFrequency =
           new Option("f", "poll-frequency", true,
                      "the time (in ms) to wait between each poll of the " +
                      "sync-dirs (optional, default value is " +
                      DEFAULT_POLL_FREQUENCY + ")");
        pollFrequency.setRequired(false);
        cmdOptions.addOption(pollFrequency);

       Option numThreads =
           new Option("t", "threads", true,
                      "the number of threads in the pool used to manage " +
                      "file transfers (optional, default value is " +
                      DEFAULT_NUM_THREADS + ")");
        numThreads.setRequired(false);
        cmdOptions.addOption(numThreads);

       Option maxFileSize =
           new Option("m", "max-file-size", true,
                      "the maximum size of a stored file in GB (value must " +
                      "be between 1 and 5), larger files will be split into " +
                      "pieces (optional, default value is " +
                      DEFAULT_MAX_FILE_SIZE + ")");
        maxFileSize.setRequired(false);
        cmdOptions.addOption(maxFileSize);

       Option syncDeletes =
           new Option("d", "sync-deletes", false,
                      "indicates that deletes performed on files within the " +
                      "sync directories should also be performed on those " +
                      "files in DuraCloud; if this option is not included " +
                      "all deletes are ignored (optional, not set by default)");
        syncDeletes.setRequired(false);
        cmdOptions.addOption(syncDeletes);

       Option cleanStart =
           new Option("l", "clean-start", false,
                      "indicates that the sync tool should perform a clean " +
                      "start, ensuring that all files in all content " +
                      "directories are checked against DuraCloud, even if " +
                      "those files have not changed locally since the last " +
                      "run of the sync tool. (optional, not set by default)");
        cleanStart.setRequired(false);
        cmdOptions.addOption(cleanStart);

       Option exitOnCompletion =
           new Option("x", "exit-on-completion", false,
                      "indicates that the sync tool should exit once it has " +
                      "completed a scan of the content directories and synced " +
                      "all files; if this option is included, the sync tool " +
                      "will not continue to monitor the sync dirs " +
                      "(optional, not set by default)");
        exitOnCompletion.setRequired(false);
        cmdOptions.addOption(exitOnCompletion);

       Option excludeOption =
           new Option("e", "exclude", true,
                      "file which provides a list of files and/or " +
                      "directories to exclude from the sync (one file or " +
                      "directory name rule per line)");
       excludeOption.setRequired(false);
       cmdOptions.addOption(excludeOption);

       // Options to use Backup Config
       configFileOptions = new Options();

       Option configFileOption =
           new Option("g", "config-file", true,
                      "read configuration from this file (a file containing " +
                      "the most recently used configuration can be found in " +
                      "the work-dir, named " + BACKUP_FILE_NAME + ")");
       configFileOption.setRequired(true);
       configFileOptions.addOption(configFileOption);
    }

    /**
     * Parses command line configuration into an object structure, validates
     * correct values along the way.
     *
     * Prints a help message and exits the JVM on parse failure.
     *
     * @param args command line configuration values
     * @return populated SyncToolConfig
     */
    public SyncToolConfig processCommandLine(String[] args) {
        SyncToolConfig config = null;
        try {
            config = processConfigFileOptions(args);
        } catch (ParseException e) {
            printHelp(e.getMessage());
        }
        return config;
    }

    protected SyncToolConfig processConfigFileOptions(String[] args)
        throws ParseException {
        try {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(configFileOptions, args);

            String configFilePath = cmd.getOptionValue("g");
            File configFile = new File(configFilePath);
            if(!configFile.exists()) {
                throw new ParseException("No configuration file exists at " +
                                         "the indicated path: " +
                                         configFilePath);
            }

            String[] configFileArgs = retrieveConfig(configFile);
            return processAndBackup(configFileArgs);
        } catch(ParseException e) {           
            return processAndBackup(args);
        }
    }

    private SyncToolConfig processAndBackup(String[] args)
        throws ParseException {
        SyncToolConfig config = processStandardOptions(args);
        backupConfig(config.getWorkDir(), args);
        return config;
    }

    protected SyncToolConfig processStandardOptions(String[] args)
        throws ParseException {
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(cmdOptions, args);
        SyncToolConfig config = new SyncToolConfig();

        config.setContext(context);
        config.setHost(cmd.getOptionValue("h"));
        config.setUsername(cmd.getOptionValue("u"));
        config.setPassword(cmd.getOptionValue("p"));
        config.setSpaceId(cmd.getOptionValue("s"));

        if(cmd.hasOption("i")) {
            config.setStoreId(cmd.getOptionValue("i"));
        }

        if(cmd.hasOption("r")) {
            try {
                config.setPort(Integer.valueOf(cmd.getOptionValue("r")));
            } catch(NumberFormatException e) {
                throw new ParseException("The value for port (-r) must be " +
                                         "a number.");
            }
        } else {
            config.setPort(DEFAULT_PORT);
        }

        if(cmd.hasOption("w")) {
            File workDir = new File(cmd.getOptionValue("w"));
            if(workDir.exists()) {
                if(!workDir.isDirectory()) {
                    throw new ParseException("Work Dir parameter must provide " +
                                             "the path to a directory. " +
                                             "(optional, set to duracloud-" +
                                             "sync-work directory in user's " +
                                             "home directory by default)");
                }
            } else {
                workDir.mkdirs();
            }
            workDir.setWritable(true);
            config.setWorkDir(workDir);
        } else {
            config.setWorkDir(null);
        }

        String[] contentDirPaths = cmd.getOptionValues("c");
        List<File> contentDirs = new ArrayList<File>();
        for(String path : contentDirPaths) {
            File contentDir = new File(path);
            if(!contentDir.exists() || !contentDir.isDirectory()) {
                throw new ParseException("Each content dir value must provide " +
                                         "the path to a directory.");
            }
            contentDirs.add(contentDir);
        }
        config.setContentDirs(contentDirs);

        if(cmd.hasOption("f")) {
            try {
                config.setPollFrequency(Long.valueOf(cmd.getOptionValue("f")));
            } catch(NumberFormatException e) {
                throw new ParseException("The value for poll frequency (-f) " +
                                         "must be a number.");
            }
        } else {
            config.setPollFrequency(DEFAULT_POLL_FREQUENCY);
        }

        if(cmd.hasOption("t")) {
            try {
                config.setNumThreads(Integer.valueOf(cmd.getOptionValue("t")));
            } catch(NumberFormatException e) {
                throw new ParseException("The value for threads (-t) must " +
                                         "be a number.");
            }
        } else {
            config.setNumThreads(DEFAULT_NUM_THREADS);
        }

        if(cmd.hasOption("m")) {
            String error = "The value for max-file-size (-m) must be a " +
                           "number between 1 and 5.";
            try {
                long maxFileSize = Integer.valueOf(cmd.getOptionValue("m"));
                if(maxFileSize >= 1 && maxFileSize <= 5) {
                    config.setMaxFileSize(maxFileSize * GIGABYTE);
                } else {
                    throw new ParseException(error);
                }
            } catch(NumberFormatException e) {
                throw new ParseException(error);
            }
        } else {
            config.setMaxFileSize(DEFAULT_MAX_FILE_SIZE * GIGABYTE);
        }

        if(cmd.hasOption("d")) {
            config.setSyncDeletes(true);
        } else {
            config.setSyncDeletes(false);
        }

        if(cmd.hasOption("l")) {
            config.setCleanStart(true);
        } else {
            config.setCleanStart(false);
        }

        if(cmd.hasOption("x")) {
            config.setExitOnCompletion(true);
        } else {
            config.setExitOnCompletion(false);
        }

        if(cmd.hasOption("e")) {
            File excludeFile = new File(cmd.getOptionValue("e"));
            if(!excludeFile.exists()) {
                throw new ParseException("Exclude parameter must provide the " +
                                         "path to a valid file.");
            }
            config.setExcludeList(excludeFile);
        }

        return config;
    }

    private void printHelp(String message) {
        System.out.println("\n-----------------------\n" +
                           message +
                           "\n-----------------------\n");

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Running SyncTool",
                            cmdOptions);
        formatter.printHelp("ReRunning SyncTool",
                            configFileOptions); 
        System.exit(1);
    }

    protected void backupConfig(File backupDir, String[] args) {
        File configBackupFile = new File(backupDir, BACKUP_FILE_NAME);
        try {
            if(configBackupFile.exists()) {
                File prevConfigBackupFile =
                    new File(backupDir, PREV_BACKUP_FILE_NAME);
                FileUtils.copyFile(configBackupFile, prevConfigBackupFile);
            }

            BufferedWriter backupWriter =
                new BufferedWriter(new FileWriter(configBackupFile));
            for(String arg : args) {
                backupWriter.write(arg);
                backupWriter.newLine();
                backupWriter.flush();
            }
            backupWriter.close();
        } catch(IOException e) {
            throw new RuntimeException("Unable to write configuration file " +
                                       "due to: " + e.getMessage(), e);
        }
    }

    protected String[] retrieveConfig(File configBackupFile) {
        String[] config = null;
        if(configBackupFile.exists()) {
            ArrayList<String> args = new ArrayList<String>();
            try {
                BufferedReader backupReader =
                    new BufferedReader(new FileReader(configBackupFile));
                String line = backupReader.readLine();
                while(line != null) {
                    args.add(line);
                    line = backupReader.readLine();
                }
                config = args.toArray(new String[args.size()]);
            } catch(IOException e) {
                throw new RuntimeException("Unable to read configuration file " +
                                           "due to: " + e.getMessage(), e);
            }
        }
        return config;
    }

    /**
     * Retrieves the configuration of the previous run of the Sync Tool.
     * If there was no previous run, the backup file cannot be found, or
     * the backup file cannot be read, returns null, otherwise returns the
     * parsed configuration
     * @param backupDir the current backup directory
     * @return config for previous sync tool run, or null
     */
    public SyncToolConfig retrievePrevConfig(File backupDir) {
        File prevConfigBackupFile =
                    new File(backupDir, PREV_BACKUP_FILE_NAME);
        if(prevConfigBackupFile.exists()) {
            String[] prevConfigArgs = retrieveConfig(prevConfigBackupFile);
            try {
                return processStandardOptions(prevConfigArgs);
            } catch (ParseException e) {
                return null;
            }          
        } else {
            return null;
        }
    }
}
