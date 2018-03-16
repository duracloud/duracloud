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
 * Date: 9/17/2015
 */
public class SnapshotErrorBridgeParametersTest {

    @Test
    public void testSerialize() {
        String error = "an-error-description";

        SnapshotErrorBridgeParameters params = new SnapshotErrorBridgeParameters(error);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult,
                   containsString("\"error\":\"" + error + "\""));
    }

}
