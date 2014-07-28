/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 7/25/14
 */
public class CreateSnapshotTaskResultTest {

    @Test
    public void testSerialize() {
        String snapshotId = "snapshot-id";
        String result = new CreateSnapshotTaskResult(snapshotId).serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult,
                   containsString("\"snapshotId\":\""+snapshotId+"\""));
    }

}
