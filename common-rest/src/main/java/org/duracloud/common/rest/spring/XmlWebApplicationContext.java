/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.rest.spring;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
/**
 * This class provides some enhancements to Spring's XmlWebApplicationContext such as support
 * for S3 Resource Urls.
 * @author Daniel Bernstein
 *
 */
public class XmlWebApplicationContext
    extends org.springframework.web.context.support.XmlWebApplicationContext {

    @Override
    protected Resource getResourceByPath(String path) {
        if(path.startsWith("s3://")){
            AmazonS3Client client = new AmazonS3Client();
            AmazonS3URI s3Uri = new AmazonS3URI(path);
            S3Object s3Obj = client.getObject(new GetObjectRequest(s3Uri.getBucket(), s3Uri.getKey()));
            s3Obj.getObjectContent();
            
            return  new InputStreamResource(s3Obj.getObjectContent());
        }
        return super.getResourceByPath(path);
    }
}
