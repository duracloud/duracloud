/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * Manages string data in a persistent store. When data is stored, a token
 * is returned which can be used later to retrieve the data.
 *
 * Data is expected to be removed from the store based on an expiration schedule
 *
 * @author bbranan
 * Date: Aug 10, 2018
 */
public class StringDataStore {

    // Temporary in-memory data store for testing purposes
    private static Map<String, String> inMemoryStore = new HashMap<>();

    private static StringDataStore instance;

    private StringDataStore() {
        // Private to ensure singleton
    }

    public static StringDataStore getInstance() {
        if (instance == null) {
            instance = new StringDataStore();
        }
        return instance;
    }

    /**
     * Stores string data and returns a token by which that data can be retrieved
     *
     * @param cookieData serialized cookie data
     * @return alphanumeric token value by which data can be retrieved
     */
    public String storeData(String cookieData) {
        String token = generateToken();
        inMemoryStore.put(token, cookieData);
        return token;
    }

    /**
     * Retrieves string data given its token.
     *
     * If no data is associated with the provided token, returns null
     *
     * @param token
     * @return
     */
    public String retrieveData(String token) {
        return inMemoryStore.get(token);
    }

    /*
     * Generates a random 10 digit token value
     */
    private String generateToken() {
        return RandomStringUtils.randomAlphanumeric(20);
    }

}
