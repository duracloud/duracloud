/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.tomcat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.common.util.IOUtil;
import org.duracloud.services.webapputil.error.WebAppDeployerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class provides utility methods for creating and removing tomcat
 * installations.
 *
 * @author Andrew Woods
 *         Date: Dec 1, 2009
 */
public class TomcatUtil {

    private final Logger log = LoggerFactory.getLogger(TomcatUtil.class);

    private File binaries;
    private File resourceDir;
    private String binariesZipName;

    /**
     * This method installs a new tomcat appserver under the arg installDir,
     * running on the arg port.
     *
     * @param installDir under which to install tomcat
     * @param port       to run tomcat on
     * @return TomcatInstance
     */
    public TomcatInstance installTomcat(File installDir, int port) {
        IOUtil.copyFileToDirectory(getBinaries(), installDir);

        ZipFile zip = getZipFile(installDir);

        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            File file = new File(installDir, name);
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                InputStream input = getInputStream(zip, entry, port);
                OutputStream output = IOUtil.getOutputStream(file);
                IOUtil.copy(input, output);

                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(output);

                setScriptsExecutable(file);
            }
        }

        return new TomcatInstance(getCatalinaHome(installDir), port);
    }

    private ZipFile getZipFile(File installDir) {
        String sep = File.separator;
        ZipFile zip;
        try {
            zip = new ZipFile(installDir + sep + getBinariesZipName());
        } catch (IOException e) {
            throw new WebAppDeployerException(e);
        }
        return zip;
    }

    private InputStream getInputStream(ZipFile zip, ZipEntry entry, int port) {
        InputStream input = getInputStream(zip, entry);
        if (entry.getName().endsWith("server.xml")) {
            input = getServerXmlInputStream(input, port);
        }
        return input;
    }

    private InputStream getInputStream(ZipFile zip, ZipEntry entry) {
        InputStream input;
        try {
            input = zip.getInputStream(entry);
        } catch (IOException e) {
            throw new WebAppDeployerException(e);
        }
        return input;
    }

    private InputStream getServerXmlInputStream(InputStream input, int port) {
        return new ServerXmlInputStream(input, port);
    }

    private void setScriptsExecutable(File file) {
        if (file.getName().endsWith(".sh")) {
            file.setExecutable(true);
        }
    }

    public File getCatalinaHome(File installDir) {
        File catalinaHome = new File(installDir, getTomcatDirName());
        if (!catalinaHome.exists()) {
            throw new WebAppDeployerException("Tomcat has not been installed.");
        }
        return catalinaHome;
    }

    private String getTomcatDirName() {
        return FilenameUtils.getBaseName(getBinariesZipName());
    }

    private String getBinariesZipName() {
        return binariesZipName;
    }

    /**
     * This method removes the tomcat installation represented by the arg
     * tomcatInstance.
     *
     * @param tomcatInstance to be un-installed.
     */
    public void unInstallTomcat(TomcatInstance tomcatInstance) {
        tomcatInstance.stop();
        try {
            FileUtils.deleteDirectory(tomcatInstance.getCatalinaHome());
        } catch (IOException e) {
            log.warn("Error deleting: " + tomcatInstance.getCatalinaHome(), e);
        }
    }

    public void setResourceDir(File resourceDir) {
        this.resourceDir = resourceDir;
    }

    public void setBinariesZipName(String binariesZipName) {
        this.binariesZipName = binariesZipName;
    }

    private File getBinaries() {
        if (null == binaries) {
            binaries = new File(resourceDir, binariesZipName);
        }

        if (!binaries.exists()) {
            throw new WebAppDeployerException(
                "Binaries not found: " + binaries.getAbsolutePath());
        }
        return binaries;
    }


}
