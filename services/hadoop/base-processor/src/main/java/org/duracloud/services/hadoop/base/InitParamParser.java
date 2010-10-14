/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.duracloud.storage.domain.HadoopTypes;

import java.util.HashMap;
import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.*;

/**
 * Parses the initialization parameters for the hadoop job
 *
 * @author: Bill Branan
 * Date: Aug 13, 2010
 */
public class InitParamParser {

    public static final HadoopTypes.TASK_PARAMS host = TASK_PARAMS.DC_HOST;
    public static final HadoopTypes.TASK_PARAMS port = TASK_PARAMS.DC_PORT;
    public static final HadoopTypes.TASK_PARAMS ctxt = TASK_PARAMS.DC_CONTEXT;
    public static final HadoopTypes.TASK_PARAMS user = TASK_PARAMS.DC_USERNAME;
    public static final HadoopTypes.TASK_PARAMS pass = TASK_PARAMS.DC_PASSWORD;
    public static final HadoopTypes.TASK_PARAMS store = TASK_PARAMS.DC_STORE_ID;
    public static final HadoopTypes.TASK_PARAMS inPath = TASK_PARAMS.INPUT_PATH;
    public static final HadoopTypes.TASK_PARAMS outPath = TASK_PARAMS.OUTPUT_PATH;

    /**
     * Process the initialization parameters
     */
    public Map<String, String> parseInitParams(String[] args) {
        Map<String, String> initParams = new HashMap<String, String>();

        CommandLine cmd = processArgs(args);
        Option[] options = cmd.getOptions();
        for(Option option : options) {
            initParams.put(option.getLongOpt(), option.getValue());
        }

        return initParams;
    }

    private CommandLine processArgs(String[] args) {
        Options options = createOptions();
        CommandLine cmd = null;

        try {
            cmd = new PosixParser().parse(options, args);
        } catch (ParseException e) {
            printHelpText(options, e.getMessage());
        }

        return cmd;
    }

    private void printHelpText(Options options, String helpText) {
        (new HelpFormatter()).printHelp(helpText, options);
        throw new RuntimeException(helpText);
    }

    /**
     * Creates the input parameter options.
     * <p/>
     * This method can be overridden to handle an alternate set
     * of input parameters.
     */
    protected Options createOptions() {
        Options options = new Options();

        String inputDesc = "Path to the files that you wish to process " +
                           "e.g. s3n://<mybucket>/inputfiles/";
        Option inputOption = new Option("i", inPath.getLongForm(), true, inputDesc);
        inputOption.setRequired(true);
        options.addOption(inputOption);

        String outputDesc = "Path to the location where output files should " +
                            "be written e.g. s3n://<mybucket>/outputfiles/";
        Option outputOption = new Option("o", outPath.getLongForm(), true, outputDesc);
        outputOption.setRequired(true);
        options.addOption(outputOption);

        String dcHostDesc = "The host name used to connect to DuraCloud";
        Option dcHostOption = new Option("h", host.getLongForm(), true, dcHostDesc);
        dcHostOption.setRequired(true);
        options.addOption(dcHostOption);

        String dcPortDesc = "The port used to connect to DuraCloud";
        Option dcPortOption = new Option("r", port.getLongForm(), true, dcPortDesc);
        dcPortOption.setRequired(false);
        options.addOption(dcPortOption);

        String dcContextDesc = "The app context used to connect to DuraCloud";
        Option dcContextOption = new Option("x", ctxt.getLongForm(), true, dcContextDesc);
        dcContextOption.setRequired(false);
        options.addOption(dcContextOption);

        String dcUserDesc = "The username used to connect to DuraCloud";
        Option dcUserOption = new Option("u", user.getLongForm(), true, dcUserDesc);
        dcUserOption.setRequired(true);
        options.addOption(dcUserOption);

        String dcPassDesc = "The password used to connect to DuraCloud";
        Option dcPassOption = new Option("p", pass.getLongForm(), true, dcPassDesc);
        dcPassOption.setRequired(true);
        options.addOption(dcPassOption);

        String dcInStoreDesc = "The source storage provider id";
        Option dcInStoreOption = new Option("s", store.getLongForm(), true, dcInStoreDesc);
        dcInStoreOption.setRequired(false);
        options.addOption(dcInStoreOption);

        return options;
    }

}