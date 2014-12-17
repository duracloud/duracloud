/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.storage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.StorageClass;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: Aug 30, 2010
 */
public class SetStorageClassTestBase {

    protected StorageProvider s3Provider;
    protected S3StorageProvider unwrappedS3Provider;
    protected AmazonS3Client s3Client;

    @Before
    public void setUp() throws Exception {
        s3Provider = createS3ProviderMock();
        unwrappedS3Provider = createUnwrappedS3ProviderMock();
        s3Client = createS3ClientMock();
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(s3Provider, unwrappedS3Provider, s3Client);
        s3Provider = null;
        unwrappedS3Provider = null;
        s3Client = null;
    }

    private StorageProvider createS3ProviderMock() {
        StorageProvider mock = EasyMock.createMock(StorageProvider.class);

        List<String> contentItems = new ArrayList<String>();
        contentItems.add("item1");
        contentItems.add("item2");
        contentItems.add("item3");
        EasyMock
            .expect(mock.getSpaceContents(EasyMock.isA(String.class),
                                          EasyMock.<String>isNull()))
            .andReturn(contentItems.iterator())
            .times(1);

        EasyMock.replay(mock);
        return mock;
    }

    private S3StorageProvider createUnwrappedS3ProviderMock() {
        S3StorageProvider mock = EasyMock.createMock(S3StorageProvider.class);

        EasyMock
            .expect(mock.getBucketName(EasyMock.isA(String.class)))
            .andReturn("bucket-name")
            .times(1);

        EasyMock.replay(mock);
        return mock;
    }

    private AmazonS3Client createS3ClientMock() {
        AmazonS3Client mock = EasyMock.createMock(AmazonS3Client.class);

        mock.changeObjectStorageClass(EasyMock.isA(String.class),
                                      EasyMock.isA(String.class),
                                      EasyMock.isA(StorageClass.class));
        EasyMock.expectLastCall().times(3);

        EasyMock.replay(mock);
        return mock;
    }

}
