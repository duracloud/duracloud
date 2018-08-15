/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import org.apache.commons.lang3.RandomStringUtils;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.IOUtil;
import org.duracloud.storage.domain.RetrievedContent;
import org.duracloud.storage.error.NotFoundException;

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

    private S3StorageProvider s3StorageProvider;
    private String hiddenSpaceName;

    public StringDataStore(String hiddenSpaceName, S3StorageProvider s3StorageProvider) {
        this.s3StorageProvider = s3StorageProvider;
        this.hiddenSpaceName = hiddenSpaceName;
    }

    /**
     * Stores string data and returns a token by which that data can be retrieved
     *
     * @param cookieData serialized cookie data
     * @return alphanumeric token value by which data can be retrieved
     */
    public String storeData(String cookieData) {
        try {
            String token = generateToken();
            ensureSpaceExists();
            s3StorageProvider.addHiddenContent(this.hiddenSpaceName, token, "application/json",
                                               IOUtil.writeStringToStream(cookieData));
            return token;
        } catch (Exception e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    private void ensureSpaceExists() {
        if (!s3StorageProvider.spaceExists(this.hiddenSpaceName)) {
            s3StorageProvider.createHiddenSpace(this.hiddenSpaceName, 1);
        }
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
        try {
            RetrievedContent data = this.s3StorageProvider.getContent(this.hiddenSpaceName, token);
            return IOUtil.readStringFromStream(data.getContentStream());
        } catch (NotFoundException ex) {
            return null;
        } catch (Exception ex) {
            throw new DuraCloudRuntimeException(ex);
        }
    }

    /*
     * Generates a random 10 digit token value
     */
    private String generateToken() {
        return RandomStringUtils.randomAlphanumeric(20);
    }

}
