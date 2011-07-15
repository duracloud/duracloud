/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.common.util.bulk.ManifestVerifier.DELIM;

/**
 * @author Andrew Woods
 *         Date: Jan 17, 2011
 */
public class FixityManifestVerifierTest {

    private FixityManifestVerifier verifier;

    @Before
    public void setUp() throws Exception {
        File file0 = File.createTempFile("not0", ".used");
        File file1 = File.createTempFile("not1", ".used");
        verifier = new FixityManifestVerifier(file0, file1);
    }

    @Test
    public void testAddEntry() {
        String line = ""; // valid, but undesirable
        verify(line, true);

        line = "a" + DELIM + "b";
        verify(line, false);

        line = "a" + DELIM + "b" + DELIM + "c";
        verify(line, true);

        line = "a" + DELIM + "b" + DELIM + "c" + DELIM + "d";
        verify(line, false);

        line = "a" + DELIM + "b" + DELIM + "c" + DELIM + "d" + DELIM + "VALID";
        verify(line, true);

        line = "a" + DELIM + "b" + DELIM + "c" + DELIM + "d" + DELIM + "OTHER";
        verify(line, false);

        line = "a" + DELIM + "b" + DELIM + "c" + DELIM + "d" + DELIM +
            "MISSING_FROM_0";
        verify(line, true);

        line = "a" + DELIM + "b" + DELIM + "c" + DELIM + "d" + DELIM +
            "MISSING_FROM_0" + DELIM + "more";
        verify(line, false);
    }

    private void verify(String line, boolean successExpected) {
        Map<String, String> entries = new HashMap<String, String>();

        boolean success = true;
        try {
            verifier.addEntry(line, entries);
        } catch (Exception e) {
            success = false;
        }

        Assert.assertEquals(line, successExpected, success);
    }
    
}
