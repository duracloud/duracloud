/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.store;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.s3.S3Credentials;
import org.duracloud.common.util.ChecksumUtil;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * This class is a utility for getting an MD5 for a local file or from a file
 * stored in DuraCloud.
 *
 * @author Andrew Woods
 *         Date: Oct 14, 2010
 */
public class MD5Util {

    private final Logger log = LoggerFactory.getLogger(MD5Util.class);

    private ChecksumUtil checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);

    /**
     * This method returns the MD5 of the arg file.
     *
     * @param file for which MD5 is sought
     * @return MD5 or error message
     */
    public String getMd5(File file) {
        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(file);

        } catch (FileNotFoundException e) {
            log.warn(e.getMessage());
            return "file-not-found";
        }

        String md5 = checksumUtil.generateChecksum(fileStream);
        IOUtils.closeQuietly(fileStream);

        return md5;
    }

    /**
     * This method returns the MD5 of the arg content item.
     *
     * @param s3Credentials to connect to S3
     * @param bucketId of content item
     * @param contentId of content item
     * @return MD5 or error message
     */
    public String getMd5(S3Credentials s3Credentials,
                         String bucketId,
                         String contentId) {
        String md5 = "item-md5-not-found";
        try {
            S3Service s3Service = getS3Service(s3Credentials);
            S3Object s3Object =
                s3Service.getObjectDetails(new S3Bucket(bucketId), contentId);
            md5 = s3Object.getETag();
        } catch (S3ServiceException e) {
            log.warn("Unable to retrieve md5 for " + bucketId + "/" +
                     contentId + " due to: " + e.getMessage());
        }
        return md5;
    }

    protected S3Service getS3Service(S3Credentials s3Credentials)
        throws S3ServiceException {
        AWSCredentials awsCredentials = new AWSCredentials(
            s3Credentials.getAccessKey(),
            s3Credentials.getSecretAccessKey());
        return new RestS3Service(awsCredentials);
    }

}
