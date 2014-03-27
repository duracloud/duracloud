/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.tools;

import org.duracloud.common.cli.CommandExecutor;
import org.duracloud.contentindex.tools.command.AddAccountCommand;
import org.duracloud.contentindex.tools.command.CreateCommand;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class Driver {
    /**
     * @param args
     */
    public static void main(String[] args) {
        CommandExecutor executor = new CommandExecutor();
        executor.add(new CreateCommand());
        executor.add(new AddAccountCommand());
        try {
            executor.execute(args);
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
}
