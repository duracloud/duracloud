/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.web;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author: Bill Branan
 * Date: Jan 14, 2010
 */
public class EncodeUtilTest {

    @Test
    public void testEncodeUtil() throws Exception {
        String[] chars = {"~","`","!","@","$","^","&","*","(",")","_","-",
                          "+","=","'",":",".",",","<",">","\"","[","]",
                          "{","}","#","%",";","|"," ","/"};

        String[] encoded = {"%7E", "%60", "%21", "%40", "%24", "%5E", "%26",
                            "*", "%28", "%29", "_", "-", "%2B", "%3D",
                            "%27", "%3A", ".", "%2C", "%3C", "%3E", "%22",
                            "%5B", "%5D", "%7B", "%7D", "%23", "%25", "%3B",
                            "%7C", "%20", "/"};

        assertEquals(chars.length, encoded.length);

        for(int i=0; i<chars.length; i++) {
            assertEquals("Test encoding '" + chars[i] + "'",
                         encoded[i],
                         EncodeUtil.urlEncode(chars[i]));
        }
    }
}
