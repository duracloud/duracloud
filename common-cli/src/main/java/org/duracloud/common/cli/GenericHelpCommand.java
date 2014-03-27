/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.cli;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class GenericHelpCommand extends Command {
    private Map<String,Command> commandMap;
    public GenericHelpCommand(Map<String,Command> commandMap){
        super("help");
        this.commandMap = commandMap;
    }
    
    @Override
    protected Options createOptions() {
        return new Options();
    }
    
    @Override
    protected void executeImpl(String[] args, CommandLine cl) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("("+StringUtils.join(commandMap.keySet(), "|")+") [options]", new Options());
    }
}