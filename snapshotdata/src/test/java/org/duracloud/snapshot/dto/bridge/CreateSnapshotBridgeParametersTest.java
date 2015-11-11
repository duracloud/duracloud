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
 * @author Bill Branan
 *         Date: 7/28/14
 */
public class CreateSnapshotBridgeParametersTest {

    @Test
    public void testSerialize() {
        String host = "dc-host";
        String port = "dc-port";
        String storeId = "store-id";
        String spaceId = "space-id";
        String description = "description";
        String userEmail = "user-email";
        String memberId = "member-id";

        CreateSnapshotBridgeParameters params =
            new CreateSnapshotBridgeParameters(host, port, storeId, spaceId,
                                               description, userEmail, memberId);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult, containsString("\"host\":\""+host+"\""));
        assertThat(cleanResult, containsString("\"port\":\""+port+"\""));
        assertThat(cleanResult, containsString("\"storeId\":\""+storeId+"\""));
        assertThat(cleanResult, containsString("\"spaceId\":\""+spaceId+"\""));
        assertThat(cleanResult, containsString("\"description\":\""+description+"\""));
        assertThat(cleanResult, containsString("\"userEmail\":\""+userEmail+"\""));
        assertThat(cleanResult, containsString("\"memberId\":\""+memberId+"\""));

    }

}
