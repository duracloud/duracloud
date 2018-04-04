/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.io.Console;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * Class used for prompting users for input from the command line.
 * This class is simply a wrapper for java.io.Console.
 * This class was created because java.io.Console is a final class
 * which makes it difficult to mock for unit tests.
 *
 * @author: Erik Paulsson
 * Date: June 25, 2013
 */
public class ConsolePrompt {

    private Console console;

    public ConsolePrompt() {
        console = System.console();
    }

    public void flush() {
        console.flush();
    }

    public Console format(String fmt, Object... args) {
        return console.format(fmt, args);
    }

    public Console printf(String fmt, Object... args) {
        return console.format(fmt, args);
    }

    public Reader reader() {
        return console.reader();
    }

    public String readLine() {
        return console.readLine();
    }

    public String readLine(String fmt, Object... args) {
        return console.readLine(fmt, args);
    }

    public char[] readPassword() {
        return console.readPassword();
    }

    public char[] readPassword(String fmt, Object... args) {
        return console.readPassword(fmt, args);
    }

    public PrintWriter writer() {
        return console.writer();
    }
}