/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumUtil {

    private final Logger log = LoggerFactory.getLogger(ChecksumUtil.class);

    private final MessageDigest digest;

    public ChecksumUtil(Algorithm alg) {
        try {
            digest = MessageDigest.getInstance(alg.toString());
        } catch (NoSuchAlgorithmException e) {
            log.error("Error getting msg digest instance", e);
            throw new RuntimeException(e);
        }
    }

    public String generateChecksum(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        String checksum;
        try {
            checksum = generateChecksum(stream);
        } finally {
            stream.close();
        }
        return checksum;
    }

    /**
     * This method generates checksum of content in arg stream.
     * @param inStream Content used as target of checksum.
     * @return string representation of the generated checksum.
     */
    public String generateChecksum(InputStream inStream) {
        byte[] buf = new byte[4096];
        int numRead = 0;
        while ((numRead = readFromStream(inStream, buf)) != -1) {
            digest.update(buf, 0, numRead);
        }
        return checksumBytesToString(digest.digest());
    }

    private int readFromStream(InputStream inStream, byte[] buf) {
        int numRead = -1;
        try {
            numRead = inStream.read(buf);
        } catch (IOException e) {
            log.error("Error reading stream", e);
            throw new RuntimeException(e);
        }
        return numRead;
    }

    /**
     * Wraps an InputStream with a DigestInputStream in order to compute
     * a checksum as the stream is being read.
     *
     * @param inStream The stream to wrap
     * @param algorithm The algorithm used to compute the digest
     * @return The original stream wrapped as a DigestInputStream
     */
    public static DigestInputStream wrapStream(InputStream inStream,
                                               Algorithm algorithm) {
        MessageDigest streamDigest = null;
        try {
            streamDigest = MessageDigest.getInstance(algorithm.toString());
        } catch (NoSuchAlgorithmException e) {
            String error = "Could not create a MessageDigest because the " +
            		       "required algorithm " + algorithm.toString() +
            		       " is not supported.";
            throw new RuntimeException(error);
        }

        DigestInputStream wrappedContent =
            new DigestInputStream(inStream, streamDigest);
        return wrappedContent;
    }

    /**
     * Determines the checksum value of a DigestInputStream's underlying
     * stream after the stream has been read.
     *
     * @param digestStream
     * @return The checksum value of the stream's contents
     */
    public static String getChecksum(DigestInputStream digestStream) {
        MessageDigest digest = digestStream.getMessageDigest();
        return checksumBytesToString(digest.digest());
    }

    /**
     * Converts a message digest byte array into a String based
     * on the hex values appearing in the array.
     */
    private static String checksumBytesToString(byte[] digestBytes) {
        StringBuffer hexString = new StringBuffer();
        for (int i=0; i<digestBytes.length; i++) {
            String hex=Integer.toHexString(0xff & digestBytes[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * This class encapsulates the valid values for checksum algorithms. *
     */
    public enum Algorithm {
        MD2("MD2"), MD5("MD5"), SHA_1("SHA-1"), SHA_256("SHA-256"), SHA_384(
                "SHA-384"), SHA_512("SHA-512");

        private final String text;

        private Algorithm(String input) {
            text = input;
        }

        public static Algorithm fromString(String input) {
            for (Algorithm alg : values()) {
                if (alg.text.equalsIgnoreCase(input)) {
                    return alg;
                }
            }
            return MD5;
        }

        @Override
        public String toString() {
            return text;
        }
    }

}
