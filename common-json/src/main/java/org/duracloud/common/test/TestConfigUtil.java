/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.common.model.SimpleCredential;

/**
 * Provides access to test configuration.
 * Assumes that:
 * 1. There is a system property named: DURACLOUD_TEST_CONFIG
 * 2. The value of that system property is the full path to a valid test
 * configuration file in JSON format
 *
 * @author Bill Branan
 * Date: 7/31/13
 */
public class TestConfigUtil {

    public static final String DURACLOUD_TEST_CONFIG = "DURACLOUD_TEST_CONFIG";

    public TestConfig getTestConfig() throws IOException {
        String testConfigPath = System.getenv().get(DURACLOUD_TEST_CONFIG);
        if (null == testConfigPath || testConfigPath.isEmpty()) {
            throw new RuntimeException("In order to run integration tests, an " +
                                       "environment variable named " +
                                       DURACLOUD_TEST_CONFIG + " must be supplied.");
        }

        File configFile = new File(testConfigPath);
        if (!configFile.exists()) {
            throw new RuntimeException("No integration test configuration file could " +
                                       "be found at path " + testConfigPath);
        }

        String jsonTestConfig = FileUtils.readFileToString(configFile);

        JaxbJsonSerializer<TestConfig> serializer =
            new JaxbJsonSerializer<>(TestConfig.class);
        return serializer.deserialize(jsonTestConfig);
    }

    public SimpleCredential getCredential(StorageProviderCredential.ProviderType type)
        throws IOException {
        TestConfig config = getTestConfig();
        for (StorageProviderCredential cred : config.getProviderCredentials()) {
            if (cred.getType().equals(type)) {
                return cred.getCredential();
            }
        }
        throw new IOException("No credential available for type: " +
                              type.name());
    }

    public String getSwiftEndpoint()
        throws IOException {
        TestConfig config = getTestConfig();
        return config.getSwiftEndpoint();
    }

    public String getSwiftSignerType()
        throws IOException {
        TestConfig config = getTestConfig();
        return config.getSwiftSignerType();
    }

}
