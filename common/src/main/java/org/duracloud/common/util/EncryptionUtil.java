/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * Encryption utilities.
 *
 * @author Bill Branan
 */
public class EncryptionUtil {

    private static final String DEFAULT_KEY = "7437018461906678";
    private byte[] keyBytes;
    private Cipher cipher;
    private Key key;

    /**
     * Initializes EncryptionUtil
     *
     * @throws Exception
     */
    public EncryptionUtil() throws DuraCloudRuntimeException {
        this(DEFAULT_KEY);
    }
    
    public EncryptionUtil(String key) throws DuraCloudRuntimeException {
        if(key == null){
            throw new IllegalArgumentException("'key' parameter must be non-null");
        }
        
        int keySize = DEFAULT_KEY.length();
        if(key.length() > keySize){
            key = key.substring(0,keySize);
        }
            
        key = StringUtils.leftPad(key, keySize);
        
        
        this.keyBytes = key.getBytes();
        try {
            // Create cipher
            this.cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            // Create Key
            DESKeySpec deskey = new DESKeySpec(this.keyBytes);
            this.key = new SecretKeySpec(deskey.getKey(), "DES");
        } catch(Exception e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    /**
     * Provides basic encryption on a String.
     */
    public String encrypt(String toEncrypt) throws DuraCloudRuntimeException {
        try {
            byte[] input = toEncrypt.getBytes("UTF-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherText = cipher.doFinal(input);
            return encodeBytes(cipherText);
        } catch(Exception e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    /**
     * Provides decryption of a String encrypted using encrypt()
     */
    public String decrypt(String toDecrypt) throws DuraCloudRuntimeException {
        try {
            byte[] input = decodeBytes(toDecrypt);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainText = cipher.doFinal(input);
            return new String(plainText, "UTF-8");
        } catch(Exception e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    /**
     * Encodes a byte array as a String without using a charset
     * to ensure that the exact bytes can be retrieved on decode.
     */
    private String encodeBytes(byte[] cipherText) {
        StringBuffer cipherStringBuffer = new StringBuffer();
        for (int i = 0; i < cipherText.length; i++) {
            byte b = cipherText[i];
            cipherStringBuffer.append(Byte.toString(b) + ":");
        }
        return cipherStringBuffer.toString();
    }

    /**
     * Decodes a String back into a byte array.
     */
    private byte[] decodeBytes(String cipherString) {
        String[] cipherStringBytes = cipherString.split(":");
        byte[] cipherBytes = new byte[cipherStringBytes.length];
        for (int i = 0; i < cipherStringBytes.length; i++) {
            cipherBytes[i] = Byte.parseByte(cipherStringBytes[i]);
        }
        return cipherBytes;
    }

    /**
     * This main prompts the user to input a string to be encrypted.
     *
     * @param args none
     * @throws Exception on error
     */
    public static void main(String[] args) throws Exception {
        EncryptionUtil util = new EncryptionUtil();

        System.out.println("Enter text to encrypt: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = reader.readLine();
        if (null != input && !"".equals(input)) {
            System.out.println("'" + util.encrypt(input) + "'");
        }
    }

}
