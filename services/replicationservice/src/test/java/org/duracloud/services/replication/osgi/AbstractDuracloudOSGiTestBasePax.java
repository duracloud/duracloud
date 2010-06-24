/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.replication.osgi;

import org.junit.Before;
import org.junit.runner.RunWith;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.mavenConfiguration;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.profile;
import junit.framework.Assert;

@RunWith(JUnit4TestRunner.class)
public class AbstractDuracloudOSGiTestBasePax {

    private static final String BUNDLE_HOME_PROP = "BUNDLE_HOME";
    private static final String PROJECT_VERSION_PROP = "PROJECT_VERSION";

    @Inject
    protected BundleContext bundleContext;

    @Before
    public void setUp() throws Exception {
        Thread.sleep(2000);
    }

    @Configuration
    public static Option[] configuration() {

        Option bundles =
                provision(bundle("file:target/replicationservice-"+getVersion()+".jar"));

        Option frameworks = CoreOptions.frameworks(CoreOptions.equinox());
        // Knopflerfish does not like the felix.configadmin bundle
                                                   //  CoreOptions.knopflerfish(),
        // Felix began failing 26Jan2010.
                                                   //  CoreOptions.felix());

        return options(bundles,
                       mavenConfiguration(),
                       systemProperties(),
                       frameworks,
                       profile("spring.dm"),
                       profile("log"));
    }

    private static String getVersion() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return version;
    }

    private static Option systemProperties() {
        String home = System.getProperty(BUNDLE_HOME_PROP);
        Assert.assertNotNull(home);

        return CoreOptions.systemProperties(CoreOptions.systemProperty(
            BUNDLE_HOME_PROP).value(home), CoreOptions.systemProperty(
            PROJECT_VERSION_PROP).value(getVersion()));
    }

}
