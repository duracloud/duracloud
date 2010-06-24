/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.web;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * @author: Bill Branan
 * Date: Jan 14, 2010
 */
public class EncodeUtil {

    /**
     * Encodes characters within a string to allow them to be used within a URL.
     * Note that the entire URL should not be passed to this method as it will
     * encode characters like ':' and '/'.
     * @param toEncode String to encode
     * @return encoded string
     */
    public static String urlEncode(String toEncode) {
        String encoded;
        try {
            encoded = URLEncoder.encode(toEncode, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        // URLEncoder encodes spaces as '+', convert to hex encoding
        encoded = encoded.replaceAll("[+]", "%20");

        // Forwad slashes need not be encoded
        encoded = encoded.replaceAll("%2F", "/");

        return encoded;
    }

}
