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
 *         Date: 7/31/14
 */
public class GetSnapshotContentBridgeParametersTest {

    @Test
    public void testSerialize() {
        int page = 0;
        int pageSize = 1000;
        String prefix = "test";
        GetSnapshotContentBridgeParameters params = new GetSnapshotContentBridgeParameters(page, pageSize, prefix);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"page\":"+page));
        assertThat(cleanResult, containsString("\"pageSize\":"+pageSize));
        assertThat(cleanResult, containsString("\"prefix\":\""+prefix+"\""));
    }

}
