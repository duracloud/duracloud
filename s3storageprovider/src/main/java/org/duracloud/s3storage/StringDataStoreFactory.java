/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

/**
 * A simple factory for StringDataStore object.
 * @author Daniel Bernstein
 */
public class StringDataStoreFactory {
    public StringDataStore create(final String hiddenSpaceName, S3StorageProvider s3StorageProvider) {
        return new StringDataStore(hiddenSpaceName, s3StorageProvider);
    }
}
