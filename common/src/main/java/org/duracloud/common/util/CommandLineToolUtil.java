/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

/**
 * Class that can be used for command line tools that read
 * a DuraCloud password from an evironment variable or need to prompt
 * users for input from the command line.
 *
 * @author: Erik Paulsson
 * Date: June 25, 2013
 */
public class CommandLineToolUtil {

    public static final String PASSWORD_ENV_VARIABLE_NAME =
            "DURACLOUD_PASSWORD";
    
    private ConsolePrompt console;

    public String getPasswordEnvVariable() {
        return System.getenv(PASSWORD_ENV_VARIABLE_NAME);
    }

    public ConsolePrompt getConsole() {
        if(console == null) {
            console = new ConsolePrompt();
        }
        return console;
    }
}