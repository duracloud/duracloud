/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public abstract class Command {
    private Options options;
    private String command;

    public Command(String command) {
        this.options = createOptions();
        this.command = command;
    }

    protected abstract Options createOptions();

    public String getCommandName() {
        return this.command;
    }

    public void execute(String[] args) {
        try{
            PosixParser parser = new PosixParser();
            CommandLine cl = parser.parse(options, args);
            executeImpl(args, cl);
        }catch(ParseException ex){
            usage(args);
        }
    }

    protected abstract void executeImpl(String[] args, CommandLine cl);

    protected void usage(String[] args) {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("content-index-tool " + args[0] + " [options] ",
                       options);
        System.exit(1);
    }
}