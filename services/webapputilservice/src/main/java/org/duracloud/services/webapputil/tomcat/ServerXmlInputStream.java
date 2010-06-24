/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.tomcat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andrew Woods
 *         Date: Nov 30, 2009
 */
public class ServerXmlInputStream extends ByteArrayInputStream {

    private static int BASE_PORT = 8080;
    private static final Pattern PORT_PATTERN = Pattern.compile("8\\d\\d\\d");

    public ServerXmlInputStream(InputStream input, int port) {
        super(filteredContent(input, port));

        if (port < 1000) {
            throw new IllegalArgumentException("port must be greater than 999");
        }
    }

    private static byte[] filteredContent(InputStream input, int port) {
        int portOffset = port - BASE_PORT;
        StringBuilder sb = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String line = readLine(br);
        while (null != line) {
            Matcher m = PORT_PATTERN.matcher(line);
            StringBuffer newLine = new StringBuffer();
            while (m.find()) {
                String oldPort = line.substring(m.start(), m.start() + 4);
                Integer newPort = Integer.parseInt(oldPort) + portOffset;
                m.appendReplacement(newLine, newPort.toString());
            }

            if (newLine.length() > 0) {
                m.appendTail(newLine);
                line = newLine.toString();
            }

            sb.append(line);
            line = readLine(br);
        }
        close(br);

        return sb.toString().getBytes();
    }

    private static String readLine(BufferedReader br) {
        String line = null;
        try {
            line = br.readLine();
        } catch (IOException e) {
            // do nothing.
        }
        return line;
    }

    private static void close(BufferedReader br) {
        try {
            br.close();
        } catch (IOException e) {
            // do nothing.
        }
    }

}
