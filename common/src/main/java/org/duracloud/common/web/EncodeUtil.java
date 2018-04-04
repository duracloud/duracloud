/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author: Bill Branan
 * Date: Jan 14, 2010
 */
public class EncodeUtil {

    public static final String ENCODING = "UTF-8";

    private EncodeUtil() {
        // Ensures no instances are made of this class, as there are only static members.
    }

    /**
     * Encodes characters within a string to allow them to be used within a URL.
     * Note that the entire URL should not be passed to this method as it will
     * encode characters like ':' and '/'.
     *
     * @param toEncode String to encode
     * @return encoded string
     */
    public static String urlEncode(String toEncode) {
        String encoded;
        try {
            encoded = URLEncoder.encode(toEncode, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        // URLEncoder encodes spaces as '+', convert to hex encoding
        encoded = encoded.replaceAll("[+]", "%20");

        // Forwad slashes need not be encoded
        encoded = encoded.replaceAll("%2F", "/");

        return encoded;
    }

    public static String urlDecode(String toDecode) {
        try {
            return URLDecoder.decode(toDecode, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
