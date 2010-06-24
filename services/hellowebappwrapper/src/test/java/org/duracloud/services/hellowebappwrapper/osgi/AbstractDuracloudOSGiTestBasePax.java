/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hellowebappwrapper.osgi;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import static org.ops4j.pax.exam.CoreOptions.mavenConfiguration;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.profile;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;

/**
 * @author Andrew Woods
 *         Date: Dec 10, 2009
 */
@RunWith(JUnit4TestRunner.class)
public class AbstractDuracloudOSGiTestBasePax {

    @Inject
    private BundleContext bundleContext;

    protected static final String BASE_DIR_PROP = "base.dir";
    private static final String PROJECT_VERSION_PROP = "PROJECT_VERSION";

    @Before
    public void setUp() throws Exception {
        Thread.sleep(2000);
    }

    @Configuration
    public static Option[] configuration() {

        Option bundles = bundle("file:target/hellowebappwrapper-"+getVersion()+".jar");

        Option frameworks = CoreOptions.frameworks(CoreOptions.equinox(),
                                                   // Knops does not work for this service.
                                                   // CoreOptions.knopflerfish(),
                                                   CoreOptions.felix());

        return options(bundles,
                       mavenConfiguration(),
                       systemProperties(),
                       frameworks,
                       profile("spring.dm"));
    }

    private static Option systemProperties() {
        String baseDir = System.getProperty(BASE_DIR_PROP);
        Assert.assertNotNull(baseDir);

        return CoreOptions.systemProperties(CoreOptions.systemProperty(
            BASE_DIR_PROP).value(baseDir), CoreOptions.systemProperty(
            PROJECT_VERSION_PROP).value(getVersion()));
    }

    private static String getVersion() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return version;
    }

    protected BundleContext getBundleContext() {
        Assert.assertNotNull(bundleContext);
        return bundleContext;
    }

}