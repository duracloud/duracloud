/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.j2kservice;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Jan 25, 2010
 */
public class J2kInstallHelperTest {

    private static String zipName = "adore-djatoka-1.1.zip";
    private static String warName = "adore-djatoka.war";

    private static File resourceDir = new File("src/test/resources");
    private static File installDir = new File("target/helper-test");

    @BeforeClass
    public static void beforeClass() {
        File src = new File(resourceDir, zipName);
        Assert.assertTrue(src.exists());

        try {
            FileUtils.copyFileToDirectory(src, installDir);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Before
    public void setUp() throws IOException {
        if (getJ2kHome().exists()) {
            FileUtils.deleteDirectory(getJ2kHome());
        }
    }

    private File getJ2kHome() {
        return new File(installDir, FilenameUtils.getBaseName(zipName));
    }

    @Test
    public void testInstall() {
        verifyInstallation(false);

        J2kInstallHelper helper = getHelper();
        Assert.assertNotNull(helper);

        verifyInstallation(true);
    }

    private J2kInstallHelper getHelper() {
        J2kInstallHelper helper = new J2kInstallHelper(installDir, zipName);
        Assert.assertNotNull(helper);
        return helper;
    }

    private void verifyInstallation(boolean expected) {
        File home = getJ2kHome();
        Assert.assertEquals(expected, home.exists());

        if (expected) {
            File lib = new File(home, "lib");
            File dist = new File(home, "dist");
            Assert.assertTrue(lib.exists());
            Assert.assertTrue(dist.exists());

            File[] jars = lib.listFiles(getFilter());
            Assert.assertNotNull(jars);

            Assert.assertEquals(17, jars.length);
        }
    }

    private FilenameFilter getFilter() {
        return new FilenameFilter() {
            public boolean accept(File file, String s) {
                return s.endsWith(".jar");
            }
        };
    }

    @Test
    public void testEnv() {
        J2kInstallHelper helper = getHelper();
        Map<String, String> env = helper.getInstallEnv(getPlatform());
        Assert.assertNotNull(env);

        String j2kHome = "DJATOKA_HOME";
        String libPath = "LIBPATH";
        String platform = "PLATFORM";
        String ldLibPath = "LD_LIBRARY_PATH";
        String kakaduLibPath = "KAKADU_LIBRARY_PATH";
        String kakaduHome = "KAKADU_HOME";
        String classpath = "CLASSPATH";
        String javaOpts = "JAVA_OPTS";

        Assert.assertEquals(8, env.size());
        Assert.assertNotNull(j2kHome, env.get(j2kHome));
        Assert.assertNotNull(libPath, env.get(libPath));
        Assert.assertNotNull(platform, env.get(platform));
        Assert.assertNotNull(ldLibPath, env.get(ldLibPath));
        Assert.assertNotNull(kakaduLibPath, env.get(kakaduLibPath));
        Assert.assertNotNull(kakaduHome, env.get(kakaduHome));
        Assert.assertNotNull(classpath, env.get(classpath));
        Assert.assertNotNull(javaOpts, env.get(javaOpts));

        Assert.assertTrue(null == env.get("junk"));

    }

    private String getPlatform() {
        String os = System.getProperty("os.name");
        return os.equalsIgnoreCase("windows") ? "Win32" : "Linux-x86-32";
    }

    @Test
    public void testWar() {
        J2kInstallHelper helper = getHelper();
        File war = helper.getWarFile(warName);
        Assert.assertNotNull(war);

        Assert.assertTrue(war.exists());
    }

    @Test
    public void testClasspath() {
        J2kInstallHelper helper = getHelper();
        String classpath = helper.getClasspath();
        Assert.assertNotNull(classpath);

        Assert.assertTrue(classpath.length() > 0);
        Assert.assertTrue(classpath.contains("jar"));
    }

}
