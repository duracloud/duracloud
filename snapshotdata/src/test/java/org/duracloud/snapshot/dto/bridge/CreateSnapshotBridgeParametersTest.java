/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 7/28/14
 */
public class CreateSnapshotBridgeParametersTest {

    private String host = "dc-host";
    private String port = "dc-port";
    private String storeId = "store-id";
    private String spaceId = "space-id";
    private String userEmail = "user-email";
    private String memberId = "member-id";

    @Test
    public void testSerialize() {
        String description = "description";

        CreateSnapshotBridgeParameters params =
            new CreateSnapshotBridgeParameters(host, port, storeId, spaceId,
                                               description, userEmail, memberId);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult, containsString("\"host\":\"" + host + "\""));
        assertThat(cleanResult, containsString("\"port\":\"" + port + "\""));
        assertThat(cleanResult, containsString("\"storeId\":\"" + storeId + "\""));
        assertThat(cleanResult, containsString("\"spaceId\":\"" + spaceId + "\""));
        assertThat(cleanResult, containsString("\"description\":\"" + description + "\""));
        assertThat(cleanResult, containsString("\"userEmail\":\"" + userEmail + "\""));
        assertThat(cleanResult, containsString("\"memberId\":\"" + memberId + "\""));

    }

    @Test
    public void testEmptyDescription() {
        String description = "";
        CreateSnapshotBridgeParameters params =
            new CreateSnapshotBridgeParameters(host, port, storeId, spaceId,
                                               description, userEmail, memberId);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"description\":\"" + description + "\""));

    }

    @Test
    public void testNullDescription() {
        String description = null;
        CreateSnapshotBridgeParameters params =
            new CreateSnapshotBridgeParameters(host, port, storeId, spaceId,
                                               description, userEmail, memberId);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"description\":null"));
    }

}
