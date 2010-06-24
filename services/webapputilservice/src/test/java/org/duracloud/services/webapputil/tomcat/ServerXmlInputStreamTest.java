/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.tomcat;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Andrew Woods
 *         Date: Nov 30, 2009
 */
public class ServerXmlInputStreamTest {

    @Test
    public void testConstructor() {
        verifyConstructs(-1, false);
        verifyConstructs(100, false);
        verifyConstructs(999, false);
        verifyConstructs(1000, true);
        verifyConstructs(8080, true);
        verifyConstructs(10000, true);
    }

    private void verifyConstructs(int port, boolean expected) {
        boolean success = true;
        InputStream input = null;
        try {
            input = createStream();
            ServerXmlInputStream xml = new ServerXmlInputStream(input, port);
        } catch (Exception e) {
            success = false;
        } finally {
            closeStream(input);
        }
        Assert.assertEquals(expected, success);
    }

    @Test
    public void testReplace() throws IOException {
        int basePort = 8080;
        verifyPorts(createStream(), basePort);

        ServerXmlInputStream xml;
        basePort = 6080;
        xml = new ServerXmlInputStream(createStream(), basePort);
        verifyPorts(xml, basePort);

        basePort = 10000;
        xml = new ServerXmlInputStream(createStream(), basePort);
        verifyPorts(xml, basePort);

    }

    private void verifyPorts(InputStream inStream, int port)
        throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String xml = sb.toString();

        int start = 0;
        int index = verifyContainsPort(xml, port, start);
        index = verifyContainsPort(xml, port, index);
        index = verifyContainsPort(xml, port - 80, start);
        index = verifyContainsPort(xml, port - 80, index);
        index = verifyContainsPort(xml, port + 808, start);
        index = verifyContainsPort(xml, port - 80 + 10000, start);

        Assert.assertTrue(xml.contains("800"));
        Assert.assertTrue(xml.contains("80"));
        Assert.assertTrue(xml.contains("8"));
        Assert.assertTrue(xml.contains("80-80"));
        Assert.assertTrue(xml.contains("7777"));

        closeStream(inStream);
    }

    private int verifyContainsPort(String xml, int port, int start) {
        int index = xml.indexOf(Integer.toString(port), start);
        Assert.assertTrue(index > 0);
        return index;
    }

    private ByteArrayInputStream createStream() {
        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------------------\n");
        sb.append("-----8080----------8080---------------------------\n");
        sb.append("-----8000-----------------------------------------\n");
        sb.append("------800-----------------------------------------\n");
        sb.append("-------80-----------------------------------------\n");
        sb.append("--------8----------8000---------------------------\n");
        sb.append("----80-80-----------------------------------------\n");
        sb.append("-----7777-----------------------------------------\n");
        sb.append("-----8888-----------------------------------------\n");
        sb.append("----18000-----------------------------------------\n");
        sb.append("--------------------------------------------------\n");

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    private void closeStream(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                // do nothing.
            }
            input = null;
        }
    }

}
