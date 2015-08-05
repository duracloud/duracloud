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
 *         Date: 8/4/2015
 */
public class UpdateSnapshotHistoryBridgeParametersTest {

    @Test
    public void testSerialize() {
        Boolean alternate = true;
        String history = "history";

        UpdateSnapshotHistoryBridgeParameters params =
            new UpdateSnapshotHistoryBridgeParameters(alternate, history);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult, containsString("\"alternate\":"+alternate+""));
        assertThat(cleanResult, containsString("\"history\":\""+history+"\""));
    }

}
