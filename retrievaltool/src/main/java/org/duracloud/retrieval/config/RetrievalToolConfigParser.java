/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.duracloud.common.util.CommandLineToolUtil;
import org.duracloud.common.util.ConsolePrompt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading the configuration parameters for the Retrieval Tool
 *
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public class RetrievalToolConfigParser {

    protected static final int DEFAULT_PORT = 443;
    protected static final int DEFAULT_NUM_THREADS = 3;
    protected static final String DEFAULT_CONTEXT = "durastore";
    
    private Options cmdOptions;

    private CommandLineToolUtil cmdLineUtil;

    /**
     * Creates a parser for command line configuration options.
     */
    public RetrievalToolConfigParser() {
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
           new Option("p", "password", true,
        		      "the password necessary to perform writes to DuraStore; NOTICE: "
                           + "if no password is specified in the command line the retrieval tool will "
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

       Option spaces =
           new Option("s", "spaces", true,
                      "the space or spaces from which content will be " +
                      "retrieved");
       spaces.setRequired(false);
       spaces.setArgs(Option.UNLIMITED_VALUES);
       cmdOptions.addOption(spaces);

       Option allSpaces =
           new Option("a", "all-spaces", false,
                      "indicates that all spaces should be retrieved; if " +
                      "this option is included the -s option is ignored " +
                      "(optional, not set by default)");
        allSpaces.setRequired(false);
        cmdOptions.addOption(allSpaces);

       Option contentDirOption =
           new Option("c", "content-dir", true,
                      "retrieved content is stored in this local directory");
       contentDirOption.setRequired(true);
       cmdOptions.addOption(contentDirOption);

       Option workDirOption =
           new Option("w", "work-dir", true,
                      "logs and output files will be stored in the work " +
                      "directory (optional, set to duracloud-retrieval-work " +
                      "directory in user's home directory by default)");
       workDirOption.setRequired(false);
       cmdOptions.addOption(workDirOption);

       Option overwrite =
           new Option("o", "overwrite", false,
                      "indicates that existing local files which differ " +
                      "from files in DuraCloud under the same path and name " +
                      "sould be overwritten rather than copied " +
                      "(optional, not set by default)");
        overwrite.setRequired(false);
        cmdOptions.addOption(overwrite);

       Option numThreads =
           new Option("t", "threads", true,
                      "the number of threads in the pool used to manage " +
                      "file transfers (optional, default value is " +
                      DEFAULT_NUM_THREADS + ")");
        numThreads.setRequired(false);
        cmdOptions.addOption(numThreads);

       Option disableTimestamps =
           new Option("d", "disable-timestamps", false,
                      "indicates that timestamp information found as content " +
                      "item properties in DuraCloud should not be applied to " +
                      "local files as they are retrieved (optional, not set " +
                      "by default)");
        disableTimestamps.setRequired(false);
        cmdOptions.addOption(disableTimestamps);

        Option listOnly = 
            new Option("l", "list-only", false,
                       "indicates that the retrieval tool should create a file " +
                       "listing the contents of the specified space rather than " +
                       "downloading the actual content files.  The list file " +
                       "will be placed in the specified content directory. " +
                       "One list file will be created for each specified space." +
                       "(optional, not set by default)");
        listOnly.setRequired(false);
        cmdOptions.addOption(listOnly);

        Option listFile =
            new Option("f", "list-file", true,
                       "retrieve specific contents using content IDs in the " +
                       "specified file.  The specified file should contain " +
                       "one content ID per line.  This option can only " +
                       "operate on one space at a time.");
        listFile.setRequired(false);
        cmdOptions.addOption(listFile);
    }

    /**
     * Parses command line configuration into an object structure, validates
     * correct values along the way.
     *
     * Prints a help message and exits the JVM on parse failure.
     *
     * @param args command line configuration values
     * @return populated RetrievalToolConfig
     */
    public RetrievalToolConfig processCommandLine(String[] args) {
        RetrievalToolConfig config = null;
        try {
            config = processOptions(args);
        } catch (ParseException e) {
            printHelp(e.getMessage());
        }

        // Make sure work dir is set
        RetrievalConfig.setWorkDir(config.getWorkDir());
        config.setWorkDir(RetrievalConfig.getWorkDir());

        return config;
    }

    protected RetrievalToolConfig processOptions(String[] args)
        throws ParseException {
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(cmdOptions, args);
        RetrievalToolConfig config = new RetrievalToolConfig();

        config.setContext(DEFAULT_CONTEXT);
        config.setHost(cmd.getOptionValue("h"));
        config.setUsername(cmd.getOptionValue("u"));
        
        if (null != cmd.getOptionValue("p")) {
            config.setPassword(cmd.getOptionValue("p"));
        } else if (null != getPasswordEnvVariable()) {
            config.setPassword(getPasswordEnvVariable());
        } else {
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

        if(cmd.hasOption("i")) {
            config.setStoreId(cmd.getOptionValue("i"));
        }

        if(!cmd.hasOption("s") && !cmd.hasOption("a")) {
            throw new ParseException("Either a list of spaces (-s) should be " +
                "provided or the all spaces flag (-a) must be set.");
        }

        if(cmd.hasOption("s")) {
            String[] spaces = cmd.getOptionValues("s");
            List<String> spacesList = new ArrayList<String>();
            for(String space : spaces) {
                if(space != null && !space.equals("")) {
                    spacesList.add(space);
                }
            }
            config.setSpaces(spacesList);
        }

        if(cmd.hasOption("a")) {
            config.setAllSpaces(true);
        } else {
            config.setAllSpaces(false);
        }        

        File contentDir = new File(cmd.getOptionValue("c"));
        if(contentDir.exists()) {
            if(!contentDir.isDirectory()) {
                throw new ParseException("Content Dir paramter must provide " +
                                         "the path to a directory.");
            }
        } else {
            contentDir.mkdirs();
        }
        contentDir.setWritable(true);
        config.setContentDir(contentDir);

        if(cmd.hasOption("w")) {
            File workDir = new File(cmd.getOptionValue("w"));
            if(workDir.exists()) {
                if(!workDir.isDirectory()) {
                    throw new ParseException("Work Dir parameter must provide " +
                                             "the path to a directory.");
                }
            } else {
                workDir.mkdirs();
            }
            workDir.setWritable(true);
            config.setWorkDir(workDir);
        } else {
            config.setWorkDir(null);
        }

        if(cmd.hasOption("o")) {
            config.setOverwrite(true);
        } else {
            config.setOverwrite(false);
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

        if(cmd.hasOption("d")) {
            config.setApplyTimestamps(false);
        } else {
            config.setApplyTimestamps(true);
        }

        if(cmd.hasOption("l")) {
            config.setListOnly(true);
        } else {
            config.setListOnly(false);
        }

        if(cmd.hasOption("f")) {
            if((config.getSpaces() != null && config.getSpaces().size() > 1) ||
                    config.isAllSpaces()) {
                throw new ParseException("The 'list-file' option (-f) can " +
                                         "only operate on one space at a time.");
            } else if(config.isListOnly()) {
                throw new ParseException("The 'list-file' option (-f) can " +
                        "not be used at the same time with the 'list-only' option (-l).");
            } else {
                File listFile = new File(cmd.getOptionValue("f"));
                if(listFile.exists()) {
                    config.setListFile(listFile);
                } else {
                    throw new ParseException("The specified 'list-file' containing " +
                                             "content IDs to retrieve does not exist.");
                }
            }
        }

        return config;
    }
    
    private void printHelp(String message) {
        System.out.println("\n-----------------------\n" +
                           message +
                           "\n-----------------------\n");

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Running Retrieval Tool",
                            cmdOptions);
        System.exit(1);
    }

    protected String getPasswordEnvVariable() {
        return cmdLineUtil.getPasswordEnvVariable();
    }

    protected ConsolePrompt getConsole() {
        return cmdLineUtil.getConsole();
    }

}
