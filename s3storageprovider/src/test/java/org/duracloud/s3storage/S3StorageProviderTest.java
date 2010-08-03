package org.duracloud.s3storage;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author: Bill Branan
 * Date: Aug 3, 2010
 */
public class S3StorageProviderTest {

    @Test
    public void testGetSpaceCount() {
        MockS3StorageProvider provider = new MockS3StorageProvider();

        String count = provider.getSpaceCount("spaceId", 1000);
        assertEquals("1000+", count);

        count = provider.getSpaceCount("spaceId", 1500);
        assertEquals("2000+", count);

        count = provider.getSpaceCount("spaceId", 10000);
        assertEquals("10000+", count);        
    }

    private class MockS3StorageProvider extends S3StorageProvider {

        public MockS3StorageProvider() {
            super("accessKey", "secretKey");
        }

        @Override
        public List<String> getSpaceContentsChunked(String spaceId,
                                                    String prefix,
                                                    long maxResults,
                                                    String marker) {
            List<String> contents = new ArrayList<String>();
            for(int i=0; i<maxResults; i++) {
                contents.add("contentID" + i);
            }
            return contents;
        }

    }

}
