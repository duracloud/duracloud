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
import org.apache.commons.lang3.StringUtils;
import org.duracloud.common.util.CommandLineToolUtil;
import org.duracloud.common.util.ConsolePrompt;

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

    private CommandLineToolUtil cmdLineUtil;

    /**
     * Creates a parser for command line configuration options.
     */
    public SyncToolConfigParser() {
        cmdLineUtil = new CommandLineToolUtil();

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
            new Option("p",
                       "password",
                       true,
                       "the password necessary to perform writes to DuraStore; NOTICE: "
                           + "if no password is specified in the command line the sync tool will "
                           + "look for an environment variable named "
                           + CommandLineToolUtil.PASSWORD_ENV_VARIABLE_NAME
                           + " containing the password.  Finally, if this environment variable "
                           + "does not exist the user will be prompted for the password.");
       passwordOption.setRequired(false);
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


         Option renameUpdates =
             new Option("n", "rename-updates", true,
                        "indicates that updates should be synced to the cloud and renamed. " +
                        "Specify an optional suffix to override default " +
                        "( \"" + SyncToolConfig.DEFAULT_UPDATE_SUFFIX + "\"); " + 
                        "To prevent updates altogether, see option -o. " +
                        "(optional, not set by default)");
         renameUpdates.setRequired(false);
         renameUpdates.setArgName("suffix");
         renameUpdates.setOptionalArg(true);
         cmdOptions.addOption(renameUpdates);

         Option syncUpdates =
             new Option("o", "no-update", false,
                        "indicates that changed files should not be updated; " +
                        "to perform updates without overwriting, see option -n. " +
                        "(optional, not set by default)");
         syncUpdates.setRequired(false);
         cmdOptions.addOption(syncUpdates);

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

       Option jumpStart =
           new Option("j", "jump-start", false,
                      "indicates that the sync tool should not attempt to " +
                      "check if content to be synchronized is already in " +
                      "DuraCloud, but should instead transfer all content. " +
                      "This option is best used for new data sets. " +
                      "(optional, not set by default)");
        jumpStart.setRequired(false);
        cmdOptions.addOption(jumpStart);

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

       Option prefixOption =
           new Option("a", "prefix", true,
                      "a prefix that is added to the beginning of the ID of " +
                      "each content item that is stored in DuraCloud. For " +
                      "example, a prefix value of 'a/b/c/' with a content " +
                      "item whose path is 'dir1/file.txt' would result in " +
                      "the file stored in DuraCloud as 'a/b/c/dir1/file.txt " +
                      "(optional)");
       prefixOption.setRequired(false);
       cmdOptions.addOption(prefixOption);

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

        // Make sure work dir is set
        SyncConfig.setWorkDir(config.getWorkDir());
        config.setWorkDir(SyncConfig.getWorkDir());

        backupConfig(config.getWorkDir(), args);
        return config;
    }

    protected SyncToolConfig processStandardOptions(String[] args)
        throws ParseException {
        return processStandardOptions(args, true);
    }

    protected SyncToolConfig processStandardOptions(String[] args,
                                                    boolean requirePassword)
        throws ParseException {
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(cmdOptions, args);
        SyncToolConfig config = new SyncToolConfig();

        config.setContext(context);
        config.setHost(cmd.getOptionValue("h"));
        config.setUsername(cmd.getOptionValue("u"));
        
        if (null != cmd.getOptionValue("p")) {
            config.setPassword(cmd.getOptionValue("p"));
        } else if (null != getPasswordEnvVariable()) {
            config.setPassword(getPasswordEnvVariable());
        } else if (requirePassword) {
            ConsolePrompt console = getConsole();
            if (null == console) {
                printHelp("You must either specify a password in the command "+ 
                          "line or specify the " +
                          CommandLineToolUtil.PASSWORD_ENV_VARIABLE_NAME + 
                          " environmental variable.");
            } else {
                char[] password = console.readPassword("DuraCloud password: ");
                config.setPassword(new String(password));
            }
        }

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

        if(cmd.hasOption("o") && cmd.hasOption("n")){
            throw new ParseException("Options -o (no updates) and -n " +
                                     "(rename updates) cannot be used together.");
        }
            
        if(cmd.hasOption("o")){
            config.setSyncUpdates(false);
        }

        if(cmd.hasOption("n") && cmd.hasOption("d")){
            throw new ParseException("Options -n (rename updates) and -d " +
                                     "(sync deletes) cannot be used together.");
        }

        if(cmd.hasOption("n")){
            config.setRenameUpdates(true);
            String suffix = cmd.getOptionValue("n");
            if(StringUtils.isNotBlank(suffix)){
                config.setUpdateSuffix(suffix);
            }
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

        if(cmd.hasOption("j")) {
            config.setJumpStart(true);

            if(cmd.hasOption("n") || cmd.hasOption("o")){
                throw new ParseException(
                    "The Jump Start option (-j) requires that updates be " +
                    "handled as overwrites, thus options -n (rename updates) " +
                    "and -o (no-updates) cannot be used at the same time.");
            }
        } else {
            config.setJumpStart(false);
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

        if(cmd.hasOption("a")) {
            config.setPrefix(cmd.getOptionValue("a"));
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
                return processStandardOptions(prevConfigArgs, false);
            } catch (ParseException e) {
                return null;
            }          
        } else {
            return null;
        }
    }

    protected String getPasswordEnvVariable() {
        return cmdLineUtil.getPasswordEnvVariable();
    }

    protected ConsolePrompt getConsole() {
        return cmdLineUtil.getConsole();
    }
}
