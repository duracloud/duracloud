/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.util;

import org.duracloud.storage.error.InvalidIdException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author: Bill Branan
 * Date: Jan 12, 2010
 */
public class IdUtil {

    /**
     * Determines if the ID of the space to be added is valid
     * @throws InvalidIdException if not valid
     */
    public static void validateSpaceId(String spaceID)
        throws InvalidIdException {
        if(spaceID == null ||
           spaceID.trim().length() < 3 ||
           spaceID.trim().length() > 63) {
            String err = "Space ID must be between 3 and 63 characters long";
            throw new InvalidIdException(err);
        }

        if(!spaceID.matches("[a-z0-9.-]*")) {
            String err = "Only lowercase letters, numbers, periods, " +
                         "and dashes may be used in a Space ID";
            throw new InvalidIdException(err);
        }

        if(spaceID.startsWith(".") || spaceID.startsWith("-")) {
            String err = "A Space ID must begin with a lowercase " +
                         "letter or number.";
            throw new InvalidIdException(err);
        }

        if(spaceID.endsWith("-")) {
            String err = "A Space ID must end with a lowercase letter, " +
                         "number, or period";
            throw new InvalidIdException(err);
        }

        if(spaceID.contains("..") ||
           spaceID.contains("-.") ||
           spaceID.contains(".-")) {
            String err = "A Space ID must not contain '..' '-.' or '.-'";
            throw new InvalidIdException(err);
        }

        if(spaceID.matches("[0-9]+.[0-9]+.[0-9]+.[0-9]+")) {
            String err = "A Space ID must not be formatted as an IP address";
            throw new InvalidIdException(err);
        }
    }

    /**
     * Determines if the ID of the content to be added is valid
     * @throws InvalidIdException if not valid
     */
    public static void validateContentId(String contentID)
        throws InvalidIdException {
        if(contentID == null) {
            String err = "Content ID must be at least 1 character long";
            throw new InvalidIdException(err);
        }

        if(contentID.contains("?")) {
            String err = "Content ID may not include the '?' character";
            throw new InvalidIdException(err);
        }

        if(contentID.contains("\\")) {
            String err = "Content ID may not include the '\\' character";
            throw new InvalidIdException(err);
        }

        int utfLength;
        int urlLength;
        try {
            utfLength = contentID.getBytes("UTF-8").length;
            String urlEncoded = URLEncoder.encode(contentID, "UTF-8");
            urlLength = urlEncoded.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }

        if(utfLength > 1024 || urlLength > 1024) {
            String err =
                "Content ID must <= 1024 bytes after URL and UTF-8 encoding";
            throw new InvalidIdException(err);
        }
    }
}
