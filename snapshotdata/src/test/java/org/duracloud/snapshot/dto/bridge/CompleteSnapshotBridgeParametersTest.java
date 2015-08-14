/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 8/4/2015
 */
public class CompleteSnapshotBridgeParametersTest {

    @Test
    public void testSerialize() {
        String idOne = "alt-id-one";
        String idTwo = "alt-id-two";
        List<String> alternateIds = Arrays.asList(idOne, idTwo);

        CompleteSnapshotBridgeParameters params =
            new CompleteSnapshotBridgeParameters(alternateIds);
        String result = params.serialize();
        String cleanResult = result.replaceAll("\\s+", "");

        assertThat(cleanResult,
                   containsString("\"alternateIds\":[\""+idOne+"\",\""+idTwo+"\"]"));
    }

}
