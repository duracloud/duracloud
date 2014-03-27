package org.duracloud.contentindex.tools;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.duracloud.contentindex.client.ESContentIndexInitializer;


public class Driver {
    private static Options options;

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            options = createOptions();
            CommandLineParser parser = new PosixParser();
            CommandLine cl = parser.parse(options, args);

            List<String> commands = cl.getArgList();
            if(commands.size() != 1){
                usage();
            }

            if (cl.hasOption("h")) {
                usage();
            }

            for(String command : commands){
                if(command.equals("create")){
                    ESContentIndexInitializer initializer =
                        new ESContentIndexInitializer();
                    initializer.initialize();
                }
            }

        } catch (ParseException e) {
            usage();
        }

    }


    private static void usage() {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("content-index-tool create [options] ", options);
        System.exit(1);
    }

    private static Options createOptions() {
        Options options = new Options();
        Option help = new Option("h", "help", true, "help");
        help.setRequired(false);
        options.addOption(help);

        return options;
    }

}
