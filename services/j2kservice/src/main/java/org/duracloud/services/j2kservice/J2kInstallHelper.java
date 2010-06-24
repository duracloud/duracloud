/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.j2kservice;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.common.util.IOUtil;
import org.duracloud.services.j2kservice.error.J2kWrapperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Andrew Woods
 *         Date: Jan 25, 2010
 */
public class J2kInstallHelper {

    private final Logger log = LoggerFactory.getLogger(J2kInstallHelper.class);

    private File djatokaHome;

    public J2kInstallHelper(File installDir, String zipName) {
        File zipFile = getFile(installDir, zipName);
        ZipFile zip = getZipFile(zipFile);

        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            File file = new File(installDir, name);
            if (entry.isDirectory()) {
                file.mkdirs();

            } else {
                InputStream input = getInputStream(zip, entry);
                OutputStream output = IOUtil.getOutputStream(file);
                IOUtil.copy(input, output);

                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(output);

                setScriptsExecutable(file);
            }
        }

        String zipNoExt = FilenameUtils.getBaseName(zipFile.getName());
        this.djatokaHome = getFile(installDir, zipNoExt);
    }

    private void setScriptsExecutable(File file) {
        String name = file.getName();
        if (name.endsWith(".sh") || name.equals("kdu_compress") || name.equals(
            "kdu_expand")) {
            file.setExecutable(true);
        }
    }

    private File getFile(File installDir, String zipName) {
        File zipFile = new File(installDir, zipName);
        if (!zipFile.exists()) {
            String msg = "File does not exist: " + zipFile.getAbsolutePath();
            log.error(msg);
            throw new J2kWrapperException(msg);
        }
        return zipFile;
    }

    private ZipFile getZipFile(File zipFile) {
        ZipFile zip;
        try {
            zip = new ZipFile(zipFile);
        } catch (IOException e) {
            throw new J2kWrapperException(e);
        }
        return zip;
    }

    private InputStream getInputStream(ZipFile zip, ZipEntry entry) {
        InputStream input;
        try {
            input = zip.getInputStream(entry);
        } catch (IOException e) {
            throw new J2kWrapperException(e);
        }
        return input;
    }

    protected Map<String, String> getInstallEnv(String platform) {
        Map<String, String> env = new HashMap<String, String>();

        String sep = File.separator;
        String j2kHome = djatokaHome.getAbsolutePath();
        String libPath = j2kHome + sep + "lib";
        String ldLibPath = libPath + sep + platform;
        String kakaduLibPath = "-DLD_LIBRARY_PATH=" + ldLibPath;
        String kakaduHome = j2kHome + sep + "bin" + sep + platform;
        String javaOpts =
            "-Djava.awt.headless=true -Xmx512M -Xms64M -Dkakadu.home=" +
                kakaduHome + " -Djava.library.path=" + ldLibPath +
                " -DLD_LIBRARY_PATH=" + ldLibPath;

        env.put("DJATOKA_HOME", j2kHome);
        env.put("LIBPATH", libPath);
        env.put("PLATFORM", platform);
        env.put("LD_LIBRARY_PATH", ldLibPath);
        env.put("KAKADU_LIBRARY_PATH", kakaduLibPath);
        env.put("KAKADU_HOME", kakaduHome);
        env.put("CLASSPATH", getClasspath());
        env.put("JAVA_OPTS", javaOpts);
        return env;
    }

    protected String getClasspath() {
        File lib = getLib();
        File[] jars = lib.listFiles(getFilter());
        if (null == jars || jars.length == 0) {
            String msg = "No jars found: " + lib.getName();
            log.error(msg);
            throw new J2kWrapperException(msg);
        }

        return join(jars, File.pathSeparator);
    }

    private File getLib() {
        File lib = new File(djatokaHome, "lib");
        if (!lib.exists() || !lib.isDirectory()) {
            String msg = lib.getPath() + " does not exist.";
            log.error(msg);
            throw new J2kWrapperException(msg);
        }
        return lib;
    }

    private FilenameFilter getFilter() {
        return new FilenameFilter() {
            public boolean accept(File file, String s) {
                return s.endsWith(".jar");
            }
        };
    }

    private String join(File[] elems, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (File elem : elems) {
            sb.append(elem.getAbsolutePath());
            sb.append(delimiter);
        }

        int len = sb.length();
        if (len > delimiter.length()) {
            sb.delete(len - delimiter.length(), len);
        }

        return sb.toString();
    }

    protected File getWarFile(String warName) {
        File distDir = new File(djatokaHome, "dist");
        File warFile = new File(distDir, warName);
        if (!warFile.exists()) {
            String msg = "Warfile does not exist: " + warFile.getAbsolutePath();
            log.error(msg);
            throw new J2kWrapperException(msg);
        }
        return warFile;
    }

}
