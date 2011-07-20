/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.web;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author: Bill Branan
 * Date: Jan 14, 2010
 */
public class EncodeUtilTest {

    private String[] chars = {"~","`","!","@","$","^","&","*","(",")","_","-",
                              "+","=","'",":",".",",","<",">","\"","[","]",
                              "{","}","#","%",";","|"," ","/"};

    private String[] encoded = {"%7E", "%60", "%21", "%40", "%24", "%5E", "%26",
                                "*", "%28", "%29", "_", "-", "%2B", "%3D",
                                "%27", "%3A", ".", "%2C", "%3C", "%3E", "%22",
                                "%5B", "%5D", "%7B", "%7D", "%23", "%25", "%3B",
                                "%7C", "%20", "/"};

    @Before
    public void setup() {
        assertEquals(chars.length, encoded.length);
    }

    @Test
    public void testUrlEncode() throws Exception {
        for(int i=0; i<chars.length; i++) {
            assertEquals("Test encoding '" + chars[i] + "'",
                         encoded[i],
                         EncodeUtil.urlEncode(chars[i]));
        }
    }

    @Test
    public void testUrlDecode() throws Exception {
        for(int i=0; i<chars.length; i++) {
            assertEquals("Test decoding '" + chars[i] + "'",
                         chars[i],
                         EncodeUtil.urlDecode(encoded[i]));
        }
    }

    @Test
    public void testRoundTrip() throws Exception {
        StringBuilder allCharsBuilder = new StringBuilder();
        for(int i=0; i<chars.length; i++) {
            allCharsBuilder.append(chars[i]);
        }
        String allChars = allCharsBuilder.toString();

        String encoded = EncodeUtil.urlEncode(allChars);
        String decoded = EncodeUtil.urlDecode(encoded);

        assertEquals(allChars, decoded);
    }

}
