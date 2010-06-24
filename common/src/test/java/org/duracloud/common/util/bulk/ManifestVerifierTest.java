/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util.bulk;

import org.duracloud.common.util.ExceptionUtil;
import org.duracloud.common.error.ManifestVerifyException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.io.File;

/**
 * @author Andrew Woods
 *         Date: Oct 24, 2009
 */
public class ManifestVerifierTest {

    private ManifestVerifier verifier;

    private File dir = new File("src/test/resources/");
    private File file0 = new File(dir, "manifest-md5.txt");
    private File file1 = new File(dir, "manifest-md5-sameContent.txt");
    private File file2 = new File(dir, "manifest-md5-lessContent.txt");
    private File file3 = new File(dir, "manifest-md5-mismatchCksum.txt");
    private File file4 = new File(dir, "manifest-md5-mismatchName.txt");
    private File file5 = new File(dir, "manifest-md5-lessContentMismatch.txt");
    private File file6 = new File(dir, "log4j.properties");

    @Test
    public void testVerifyGood() {
        verifier = new ManifestVerifier(file0, file1);
        try {
            verifier.verify();
        } catch (ManifestVerifyException e) {
            StringBuilder sb = new StringBuilder("No exception expected.\n\n");
            sb.append(e.getFormattedMessage()+"\n\n");
            sb.append(ExceptionUtil.getStackTraceAsString(e));
            fail(sb.toString());
        }
    }

    @Test
    public void testVerifyUneven() {
        verifier = new ManifestVerifier(file0, file2);
        try {
            verifier.verify();
            fail("Exception expected.");
        } catch (ManifestVerifyException e) {
            assertEquals(ManifestVerifyException.ErrorType.INVALID,
                         e.getErrorType());
        }
    }

    @Test
    public void testVerifyUnevenAgain() {
        verifier = new ManifestVerifier(file2, file0);
        try {
            verifier.verify();
            fail("Exception expected.");
        } catch (ManifestVerifyException e) {
            assertEquals(ManifestVerifyException.ErrorType.INVALID,
                         e.getErrorType());
        }
    }

    @Test
    public void testVerifyMismatchCksum() {
        verifier = new ManifestVerifier(file0, file3);
        try {
            verifier.verify();
            fail("Exception expected.");
        } catch (ManifestVerifyException e) {
            assertEquals(ManifestVerifyException.ErrorType.INVALID,
                         e.getErrorType());
        }
    }

    @Test
    public void testVerifyMismatchName() {
        verifier = new ManifestVerifier(file0, file4);
        try {
            verifier.verify();
            fail("Exception expected.");
        } catch (ManifestVerifyException e) {
            assertEquals(ManifestVerifyException.ErrorType.INVALID,
                         e.getErrorType());
        }
    }

    @Test
    public void testVerifyUnevenBad() {
        verifier = new ManifestVerifier(file0, file5);
        try {
            verifier.verify();
            fail("Exception expected.");
        } catch (ManifestVerifyException e) {
            assertEquals(ManifestVerifyException.ErrorType.INVALID,
                         e.getErrorType());
        }
    }

    @Test
    public void testVerifyBad() {
        boolean runtimeThrown = false;
        verifier = new ManifestVerifier(file0, file6);
        try {
            verifier.verify();
            fail("Exception expected.");
        } catch (ManifestVerifyException e) {
            fail("ManifestVerifyException not expected.\n" +
                e.getFormattedMessage());
        } catch (RuntimeException re) {
            runtimeThrown = true;
        }

        assertTrue(runtimeThrown);

    }
}
