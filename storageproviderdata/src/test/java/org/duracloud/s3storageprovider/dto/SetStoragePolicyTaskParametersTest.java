/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storageprovider.dto;

import org.duracloud.error.TaskDataException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 09/25/2015
 */
public class SetStoragePolicyTaskParametersTest {

    private final String spaceId = "space-id";
    private final String storageClass = "storage-class";
    private final Integer daysToTransition = 42;

    @Test
    public void testSerialize() {
        SetStoragePolicyTaskParameters taskParams = new SetStoragePolicyTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setStorageClass(storageClass);
        taskParams.setDaysToTransition(daysToTransition);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"spaceId\":\""+spaceId +"\""));
        assertThat(cleanResult, containsString("\"storageClass\":\""+storageClass +"\""));
        assertThat(cleanResult, containsString("\"daysToTransition\":"+daysToTransition));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized = "{\"spaceId\" : \""+spaceId+"\"," +
                                       "\"storageClass\" : \""+storageClass+"\"," +
                                       "\"daysToTransition\" : "+daysToTransition+"}";

        SetStoragePolicyTaskParameters taskParams =
            SetStoragePolicyTaskParameters.deserialize(taskParamsSerialized);
        assertEquals(spaceId, taskParams.getSpaceId());
        assertEquals(storageClass, taskParams.getStorageClass());
        assertEquals(daysToTransition, taskParams.getDaysToTransition());

        // Verify that empty params throw
        taskParamsSerialized = "{\"spaceId\" : \"\", " +
                                "\"storageClass\" : \"\", " +
                                "\"daysToTransition\" : -1}";

        try {
            SetStoragePolicyTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(TaskDataException e) {
        }

        // Verify that empty params throw
        try {
            SetStoragePolicyTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(TaskDataException e) {
        }
    }

}
