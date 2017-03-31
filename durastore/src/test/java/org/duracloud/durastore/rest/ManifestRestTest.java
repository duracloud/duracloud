/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.constant.ManifestFormat;
import org.duracloud.common.util.IOUtil;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestNotFoundException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.easymock.IExpectationSetters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Bernstein 
 *         Date: 03/31/2017
 */

public class ManifestRestTest extends EasyMockSupport {

    private ManifestRest rest;

    private StorageProviderFactory storageProviderFactory;
    private StorageProvider provider;
    private ManifestResource resource;
    private HttpServletRequest request; 
    
    private String spaceId = "space-id";
    private String storeId = "store-id";
    private String account = "account";
    private String testContent = "test-manifest-contents";
    
    
    @Before
    public void setUp() throws Exception {
        resource = createMock("ManifestResource", ManifestResource.class);
        storageProviderFactory =
            createMock("StorageProviderFactory", StorageProviderFactory.class);
        provider = createMock("StorageProvider", StorageProvider.class);
        request =
            createMock("HttpServletRequest", HttpServletRequest.class);

        rest = new ManifestRest(resource, storageProviderFactory);
        rest.request = request;

    }

    @After
    public void tearDown() throws Exception {
        verifyAll();
    }

    @Test
    public void getManifestSync() throws Exception {
        String format = ManifestFormat.TSV.name();
        expectGetManifest(format);
        setupAccountId();
        replayAll();
        Response response = rest.getManifest(spaceId, format, storeId, null);
        InputStream is = (InputStream)response.getEntity();
        assertEquals(testContent, IOUtil.readStringFromStream(is));
    }

    @Test
    public void generateAsync() throws Exception {
        String format = ManifestFormat.TSV.name();

        CountDownLatch latch = new CountDownLatch(1);

        setupAccountId();
        expect(request.getAttribute(Constants.SERVER_HOST)).andReturn("host");
        expect(request.getAttribute(Constants.SERVER_PORT)).andReturn(443);
        expect(request.getContextPath()).andReturn("/context");


        expectGetManifest(format);

        StorageAccount sa = createMock("StorageAccount", StorageAccount.class);
        expect(sa.getId()).andReturn(storeId);
        expect(sa.getType()).andReturn(StorageProviderType.AMAZON_S3);
        
        expect(storageProviderFactory.getStorageAccounts()).andReturn(Arrays.asList(sa));

        expect(storageProviderFactory.getStorageProvider()).andReturn(provider);

        expect(provider.addContent(isA(String.class),
                                   isA(String.class),
                                   eq(ManifestFormat.TSV.getMimeType()),
                                   EasyMock.<Map<String,String>>isNull(),
                                   anyLong(),
                                   isA(String.class),
                                   isA(InputStream.class))).andAnswer(new IAnswer<String>() {
                                       @Override
                                       public String answer() throws Throwable {
                                           latch.countDown();
                                           return "checksum";
                                       }
                                   });

        replayAll();
        Response response = rest.getManifest(spaceId, format, storeId, "generate");
        URI uri = (URI) response.getLocation();
        assertEquals(HttpStatus.SC_ACCEPTED, response.getStatus());

        assertNotNull(uri);

        assertTrue("async generate and upload did not complete",
                   latch.await(10000, TimeUnit.MILLISECONDS));
    }

    protected IExpectationSetters<InputStream> expectGetManifest(String format)
        throws ManifestArgumentException,
            ManifestNotFoundException {
        return expect(resource.getManifest(account,
                                    storeId,
                                    spaceId,
                                    format)).andAnswer(new IAnswer<InputStream>() {
                                        @Override
                                        public InputStream answer()
                                            throws Throwable {
                                            return createManifestInputStream();
                                        }

                                    });
    }

    protected void setupAccountId() {
        expect(request.getAttribute(Constants.ACCOUNT_ID_ATTRIBUTE)).andReturn(account);
    }

    private InputStream createManifestInputStream() {
        return new ByteArrayInputStream(testContent.getBytes());
    }

}