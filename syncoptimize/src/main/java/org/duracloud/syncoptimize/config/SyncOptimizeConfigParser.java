/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.duracloud.common.util.CommandLineToolUtil;
import org.duracloud.common.util.ConsolePrompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the command line configuration parameters into a config object that
 * is used to run the Sync Optimizer tests.
 *
 * @author Bill Branan
 *         Date: 5/16/14
 */

public class SyncOptimizeConfigParser {

    private final Logger logger =
        LoggerFactory.getLogger(SyncOptimizeConfigParser.class);

    protected static final int DEFAULT_PORT = 443;
    public static final String DEFAULT_CONTEXT = "durastore";
    protected static final int DEFAULT_NUM_FILES = 10;
    protected static final int DEFAULT_SIZE_FILES = 5;

    private Options cmdOptions;

    private CommandLineToolUtil cmdLineUtil;

    /**
     * Creates a parser for command line configuration options.
     */
    public SyncOptimizeConfigParser() {
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

       Option spaceIdOption =
           new Option("s", "space", true,
                      "the space in which test content will be placed");
       spaceIdOption.setRequired(true);
       spaceIdOption.setArgs(Option.UNLIMITED_VALUES);
       cmdOptions.addOption(spaceIdOption);

       Option numFilesOption =
           new Option("n", "num-files", true,
                      "the number of files to transfer on each test run");
       numFilesOption.setRequired(false);
       cmdOptions.addOption(numFilesOption);

        Option sizeFilesOption =
            new Option("m", "size-files", true,
                       "the size of files to transfer on each test run, in MB");
        sizeFilesOption.setRequired(false);
        cmdOptions.addOption(sizeFilesOption);

    }

    /**
     * Parses command line configuration into an object structure, validates
     * correct values along the way.
     *
     * Prints a help message and exits the JVM on parse failure.
     *
     * @param args command line configuration values
     * @return populated SyncOptimizeConfig
     */
    public SyncOptimizeConfig processCommandLine(String[] args) {
        SyncOptimizeConfig config = null;
        try {
            config = processOptions(args);
        } catch (ParseException e) {
            printHelp(e.getMessage());
        }
        return config;
    }

    protected SyncOptimizeConfig processOptions(String[] args)
        throws ParseException {
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(cmdOptions, args);
        SyncOptimizeConfig config = new SyncOptimizeConfig();

        config.setContext(DEFAULT_CONTEXT);
        config.setHost(cmd.getOptionValue("h"));
        config.setUsername(cmd.getOptionValue("u"));
        config.setSpaceId(cmd.getOptionValue("s"));

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

        if(cmd.hasOption("n")) {
            try {
                config.setNumFiles(Integer.valueOf(cmd.getOptionValue("n")));
            } catch(NumberFormatException e) {
                throw new ParseException("The value for num-files (-n) must " +
                                         "be a number.");
            }
        } else {
            config.setNumFiles(DEFAULT_NUM_FILES);
        }

        if(cmd.hasOption("m")) {
            try {
                config.setSizeFiles(Integer.valueOf(cmd.getOptionValue("m")));
            } catch(NumberFormatException e) {
                throw new ParseException("The value for size-files (-m) must " +
                                         "be a number.");
            }
        } else {
            config.setSizeFiles(DEFAULT_SIZE_FILES);
        }

        return config;
    }

    private void printHelp(String message) {
        System.out.println("\n-----------------------\n" +
                           message +
                           "\n-----------------------\n");

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Running Sync Thread Optimizer", cmdOptions);
        System.exit(1);
    }

    protected String getPasswordEnvVariable() {
        return cmdLineUtil.getPasswordEnvVariable();
    }

    protected ConsolePrompt getConsole() {
        return cmdLineUtil.getConsole();
    }

}
