/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.cli;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Bernstein
 */
public class CommandExecutor {
    private Map<String, Command> commandMap = new HashMap<>();
    private Command help = new GenericHelpCommand(commandMap);

    public CommandExecutor() {
        add(help);
    }

    public void add(Command command) {
        this.commandMap.put(command.getCommandName(), command);
    }

    public void execute(String[] args) {
        Command command = null;
        if (args.length == 0 || (command = commandMap.get(args[0])) == null) {
            help.execute(args);
        } else {
            command.execute(args);
        }
    }

}
