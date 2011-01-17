/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.util;

import org.duracloud.common.error.ManifestVerifyException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
        String line = "";
        verify(line, false);

        line = "a,b";
        verify(line, false);

        line = "a,b,c";
        verify(line, true);

        line = "a,b,c,d";
        verify(line, false);

        line = "a,b,c,d,VALID";
        verify(line, true);

        line = "a,b,c,d,OTHER";
        verify(line, false);

        line = "a,b,c,d,MISSING_FROM_0";
        verify(line, true);

        line = "a,b,c,d,MISSING_FROM_0,more";
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
