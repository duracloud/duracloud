/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.duracloud.sync.SyncToolInitializer;
import org.duracloud.syncui.config.SyncUIConfig;

/**
 * Runs the sync application in either the command line mode or GUI mode. If
 * command line parameters are present, command line mode is selected, otherwise
 * GUI mode is run.
 *
 * @author: Bill Branan
 * Date: 2/18/13
 */
public class SyncSelector {

    private SyncSelector() {
        // Ensures no instances are made of this class, as there are only static members.
    }

    public static void main(String[] args) throws Exception {
        // Ensure that work dir is initialized
        SyncUIConfig.getWorkDir();

        // Determine which tool to execute
        if (args.length > 0) {
            SyncToolInitializer.main(args);
        } else {
            disableStdErr();
            SyncUIDriver.main(args);
        }
    }

    /*
     * Ensures that nothing is written to system.err.
     */
    private static void disableStdErr() {
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {
                // this method intentionally left blank
            }
        }));
    }

}
