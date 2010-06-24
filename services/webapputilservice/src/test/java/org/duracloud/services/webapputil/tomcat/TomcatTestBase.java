/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.tomcat;

import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author Andrew Woods
 *         Date: Dec 2, 2009
 */
public class TomcatTestBase {

    private final String installBase = "target/tomcat-test";
    private File resourceDir = new File("src/test/resources");

    protected void verifyInstall(TomcatInstance instance, int numFiles)
        throws IOException {
        int totalFiles = 0;
        File catalinaHome = instance.getCatalinaHome();

        if (numFiles > 0) {
            Assert.assertTrue(catalinaHome.exists());
            File[] files = catalinaHome.listFiles();

            for (File file : files) {
                totalFiles += verifyFile(file, instance.getPort());
            }
        }

        Assert.assertEquals(numFiles, totalFiles);
    }

    private int verifyFile(File file, int port) throws IOException {
        int numFiles = 1;
        if (file.isDirectory()) {
            verifyExcecutable(file, true);

            File[] files = file.listFiles();
            for (File f : files) {
                numFiles += verifyFile(f, port);
            }
        } else if (file.getName().endsWith(".sh")) {
            verifyExcecutable(file, true);
        }

        if (file.getName().endsWith("server.xml")) {
            verifyPort(file, port);
        }
        return numFiles;
    }

    private void verifyExcecutable(File file, boolean expected) {
        Assert.assertEquals(file.getName(), expected, file.canExecute());
    }

    private void verifyPort(File file, int port) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        while (null != line) {
            sb.append(line);
            line = reader.readLine();
        }

        String text = sb.toString();
        Assert.assertTrue(text.length() > 0);
        Assert.assertTrue(text.contains(new Integer(port).toString()));
        Assert.assertTrue(!text.contains("8080"));
    }

    protected File getInstallDir(String name) {
        File dir = new File(installBase, name);
        if (!dir.mkdirs() && !dir.exists()) {
            throw new RuntimeException("Error creating: " + dir);
        }
        return dir;
    }

    protected File getResourceDir() {
        return resourceDir;
    }
}
