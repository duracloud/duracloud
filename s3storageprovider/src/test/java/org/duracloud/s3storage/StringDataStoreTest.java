/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.duracloud.common.util.IOUtil;
import org.duracloud.storage.domain.RetrievedContent;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A test class for StringDataStore
 *
 * @author Daniel Bernstein
 */

@RunWith(EasyMockRunner.class)
public class StringDataStoreTest extends EasyMockSupport {


    private StringDataStore stringDataStore;

    private String hiddenSpaceName = "hidden-space";

    private String cookieData = "cookie-data";

    private String token = "token-uuid";

    @Mock
    private S3StorageProvider storageProvider;

    @Mock
    private RetrievedContent retrievedContent;

    @Before
    public void setup() {
        this.stringDataStore = new StringDataStore(hiddenSpaceName, storageProvider);
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testStoreData() throws Exception {
        expect(storageProvider.spaceExists(hiddenSpaceName)).andReturn(false);
        expect(storageProvider.createHiddenSpace(eq(hiddenSpaceName), eq(1))).andReturn(hiddenSpaceName);
        expect(storageProvider
                   .addHiddenContent(eq(hiddenSpaceName), isA(String.class), isA(String.class), isA(InputStream.class)))
            .andReturn("etag");
        replayAll();
        String token = this.stringDataStore.storeData(cookieData);
        assertNotNull("token must not be null", token);
    }

    @Test
    public void testRetrieveData() throws Exception {
        InputStream is = IOUtil.writeStringToStream("test");
        expect(retrievedContent.getContentStream()).andReturn(is);
        expect(storageProvider.getContent(eq(hiddenSpaceName), eq(token))).andReturn(retrievedContent);
        replayAll();
        String data = this.stringDataStore.retrieveData(token);
        assertEquals("The retrieved data did not equal the expected value", "test", data);
    }

}
