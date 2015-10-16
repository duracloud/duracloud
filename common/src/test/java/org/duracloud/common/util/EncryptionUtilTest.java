/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Tests encryption utilities.
 *
 * @author Bill Branan
 */
public class EncryptionUtilTest {

    EncryptionUtil encryptionUtil;

    @Before
    public void setUp() throws Exception {
        encryptionUtil = new EncryptionUtil();
    }

    @Test
    public void testEncryption() throws Exception {
        String text = "Test Content";
        String encryptedText = encryptionUtil.encrypt(text);
        assertFalse(text.equals(encryptedText));
        String decryptedText = encryptionUtil.decrypt(encryptedText);
        assertEquals(text, decryptedText);
    }

    @Test
    public void testEncryptionNonNullConstructorShortKey() throws Exception {
        testEncryptionNonNullConstructor("test");
    }
    
    @Test
    public void testEncryptionNonNullConstructorLongKey() throws Exception {
        testEncryptionNonNullConstructor("testslkadfjaslfdjaslfdjasldkfjas098dsaf");
    }
    
    private void testEncryptionNonNullConstructor(String key) throws Exception {
        String text = "Test Content";
        encryptionUtil = new EncryptionUtil(key);
        String encryptedText = encryptionUtil.encrypt(text);
        assertFalse(text.equals(encryptedText));
        String decryptedText = encryptionUtil.decrypt(encryptedText);
        assertEquals(text, decryptedText);
    }

    @Test
    public void testEncryptionXML() throws Exception {
        String text = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                      "<inventory>" +
                      "  <item name=\"item1\">Desk</item>" +
                      "  <item name=\"item2\">Chair</item>" +
                      "</inventory>";
        String encryptedText = encryptionUtil.encrypt(text);
        assertFalse(text.equals(encryptedText));
        String decryptedText = encryptionUtil.decrypt(encryptedText);
        assertEquals(text, decryptedText);
    }

}
