/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import org.duracloud.s3storage.S3StorageProvider;
import org.easymock.classextension.EasyMock;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.S3Service;
import org.junit.After;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: Jun 4, 2010
 */
public class StreamingTaskRunnerTestBase {

    protected S3StorageProvider s3Provider;
    protected S3Service s3Service;
    protected CloudFrontService cfService;

    @After
    public void tearDown() {
        EasyMock.verify(s3Provider);
        s3Provider = null;

        EasyMock.verify(s3Service);
        s3Service = null;

        EasyMock.verify(cfService);
        cfService = null;
    }

    protected S3StorageProvider createMockS3StorageProvider() {
        S3StorageProvider provider =
            EasyMock.createMock(S3StorageProvider.class);

        EasyMock
            .expect(provider.getBucketName(EasyMock.isA(String.class)))
            .andReturn("bucketName")
            .anyTimes();

        List<String> contents = new ArrayList<String>();
        contents.add("item1");
        contents.add("item2");
        contents.add("item3");        

        EasyMock
            .expect(provider.getSpaceContents(EasyMock.isA(String.class),
                                              EasyMock.<String>isNull()))
            .andReturn(contents.iterator())
            .anyTimes();

        EasyMock.replay(provider);
        return provider;
    }

    protected S3Service createMockS3ServiceV1() throws Exception {
        S3Service service = EasyMock.createMock(S3Service.class);

        EasyMock.replay(service);
        return service;
    }
    
    protected CloudFrontService createMockCFServiceV1() throws Exception {
        CloudFrontService service =
            EasyMock.createMock(CloudFrontService.class);
        EasyMock.replay(service);
        return service;
    }
}
