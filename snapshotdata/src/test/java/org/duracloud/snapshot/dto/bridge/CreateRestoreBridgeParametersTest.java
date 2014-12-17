/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Daniel Bernstein
 *         Date: 7/29/14
 */
public class CreateRestoreBridgeParametersTest {
    
    private String host = "dc-host";
    private String port = "dc-port";
    private String storeId = "store-id";
    private String spaceId = "space-id";
    private String snapshotId = "snapshot-id";
    private String userEmail = "user-email";
    
    @Test
    public void testSerialize() {


        CreateRestoreBridgeParameters params =
            new CreateRestoreBridgeParameters(host, port, storeId, spaceId,
                                               snapshotId, userEmail);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult, containsString("\"host\":\""+host+"\""));
        assertThat(cleanResult, containsString("\"port\":\""+port+"\""));
        assertThat(cleanResult, containsString("\"storeId\":\""+storeId+"\""));
        assertThat(cleanResult, containsString("\"spaceId\":\""+spaceId+"\""));
        assertThat(cleanResult, containsString("\"snapshotId\":\""+snapshotId+"\""));
        assertThat(cleanResult, containsString("\"userEmail\":\""+userEmail+"\""));
    }
    

}
