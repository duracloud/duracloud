/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.json;

import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.test.StorageProviderCredential;
import org.duracloud.common.test.TestConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Bill Branan
 *         Date: 7/29/13
 */
public class JaxbJsonSerializerTest {

    private String jsonTestConfig;

    public JaxbJsonSerializerTest () {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("  \"providerCredentials\" : [ {");
        sb.append("    \"type\" : \"AMAZON_S3\",");
        sb.append("    \"credential\" : {");
        sb.append("      \"username\" : \"amazon-user\",");
        sb.append("      \"password\" : \"amazon-pass\"");
        sb.append("    }");
        sb.append("  }, {");
        sb.append("    \"type\" : \"RACKSPACE\",");
        sb.append("    \"credential\" : {");
        sb.append("      \"username\" : \"rack-user\",");
        sb.append("      \"password\" : \"rack-pass\"");
        sb.append("    }");
        sb.append("  } ],");
        sb.append("  \"queueName\": \"queue-name\"");
        sb.append("}");
        jsonTestConfig = sb.toString();
    }

    @Test
    public void testDeSerialize() throws Exception {
        JaxbJsonSerializer<TestConfig> serializer =
            new JaxbJsonSerializer<>(TestConfig.class);

        TestConfig desCreds = serializer.deserialize(jsonTestConfig);
        List<StorageProviderCredential> credList =
            desCreds.getProviderCredentials();
        Assert.assertEquals(2, credList.size());
        Assert.assertEquals("amazon-user",
                            credList.get(0).getCredential().getUsername());
        Assert.assertEquals("rack-user",
                            credList.get(1).getCredential().getUsername());
        Assert.assertEquals("queue-name", desCreds.getQueueName());
  }

    @Test
    public void testSerialize() throws Exception {
        JaxbJsonSerializer<TestConfig> serializer =
            new JaxbJsonSerializer<>(TestConfig.class);

        StorageProviderCredential s3Cred = new StorageProviderCredential(
            StorageProviderCredential.ProviderType.AMAZON_S3,
            new SimpleCredential("amazon-user", "amazon-pass"));

        StorageProviderCredential rackCred = new StorageProviderCredential(
            StorageProviderCredential.ProviderType.RACKSPACE,
            new SimpleCredential("rack-user", "rack-pass"));

        TestConfig testConfig = new TestConfig();
        testConfig.addProviderCredential(s3Cred);
        testConfig.addProviderCredential(rackCred);
        testConfig.setQueueName("queue-name");

        String json = serializer.serialize(testConfig);
        // Verify that the resulting json matches the expected value, ignoring
        // whitespace.
        Assert.assertEquals(jsonTestConfig.replaceAll("\\s",""),
                            json.replaceAll("\\s",""));
  }

}
