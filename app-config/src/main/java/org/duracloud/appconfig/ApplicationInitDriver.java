/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig;

import java.io.File;
import java.io.IOException;

/**
 * @author Andrew Woods
 *         Date: Apr 22, 2010
 */
public class ApplicationInitDriver {

    public static void main(String[] args) throws IOException {
        if (args.length != 1 && args.length != 2) {
            usage("Must have 1 or 2 args: " + args.length);
            System.exit(1);
        }

        File propsFile = new File(args[0]);
        if (!propsFile.exists()) {
            usage("Arg file does not exist: " + propsFile.getPath());
            System.exit(1);
        }

        File dir = null;
        if (args.length == 2) {
            dir = new File(args[1]);
            if (!dir.exists()) {
                usage("Output dir does not exist: " + dir.getPath());
                System.exit(1);
            }
        }

        ApplicationInitializer appInit = new ApplicationInitializer(propsFile);
        appInit.initialize();
        appInit.setSecurityUsers();
        if (dir != null) {
            appInit.outputXml(dir);
            System.out.println("config xml files written to: " + dir.getPath());
        }
        System.out.println("success");
    }

    private static void usage(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error: " + msg);
        sb.append("\n\n");
        sb.append("Usage: ");
        sb.append("\n\t");
        sb.append("ApplicationInitDriver <props-file> [output-dir]");
        sb.append("\n\n\t");
        sb.append("where <props-file> is the path to the configuration");
        sb.append("\n\t");
        sb.append("properties file.");
        sb.append("\n\t");
        sb.append("An example props-file can be found in the 'resources' dir ");
        sb.append("\n\t");
        sb.append("of the project and this jar.");
        sb.append("\n\n\t");
        sb.append("where <output-dir> is an optional path to a directory where");
        sb.append("\n\t");
        sb.append("the configuration files will be written as xml.");

        System.out.println(sb.toString());
    }
}
