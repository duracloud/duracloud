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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.duracloud.client.HttpHeaders;
import org.duracloud.mill.db.model.BitIntegrityReport;
import org.duracloud.mill.db.repo.JpaBitIntegrityReportRepo;
import org.duracloud.reportdata.bitintegrity.BitIntegrityReportResult;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
/**
 * 
 * @author Daniel Bernstein
 *          Date: Sep 2, 2014
 *
 */
@RunWith(EasyMockRunner.class)
public class BitIntegrityReportRestTest extends EasyMockSupport {
    private String storeId = "store-id";
    private String spaceId = "space-id";
    private String reportSpaceId = "report-space-id";
    private String reportContentId = "report-content-id";

    @Mock
    private JpaBitIntegrityReportRepo repo;
    @Mock
    private StorageProviderFactory storageProviderFactory;
    @Mock
    private StorageProvider store;
    @Mock
    private HttpServletRequest request;

    @Mock
    private Page page;
    @Mock
    private Map<String, String> contentProperties;

    private BitIntegrityReportRest rest;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        verifyAll();
    }

    @Test
    public void testGetReportHead() {
        setupHeader();
        setupBitIntegrityReportRepo();
        setupStorageProviderFactory();
        setupContentProperties();
        replayAll();
        createRest();
        Response response = rest.getReportHead(spaceId, storeId);
        verifyHeaders(response);
        assertNull(response.getEntity());
    }

    protected void verifyHeaders(Response response) {
        assertNotNull(response.getMetadata()
                              .getFirst(HttpHeaders.BIT_INTEGRITY_REPORT_COMPLETION_DATE));
        assertNotNull(response.getMetadata()
                              .getFirst(HttpHeaders.BIT_INTEGRITY_REPORT_RESULT));
        assertNotNull(response.getMetadata()
                              .getFirst(HttpHeaders.CONTENT_LENGTH));
    }

    @Test
    public void testGetReport() {
        setupHeader();
        setupBitIntegrityReportRepo();
        setupStorageProviderFactory();
        setupContentProperties();
        setupGetContent();
        replayAll();
        createRest();
        Response response = rest.getReport(spaceId, storeId);
        verifyHeaders(response);
        assertTrue(response.getEntity() instanceof InputStream);
    }
    
    @Test
    public void testGetReportNoBitReportFound() {
        setupHeader();
        expect(repo.findByStoreIdAndSpaceIdAndDisplayTrueOrderByCompletionDateDesc(eq(storeId),
                                                                     eq(spaceId),
                                                                     isA(PageRequest.class))).andReturn(null);
        replayAll();
        createRest();
        Response response = rest.getReport(spaceId, storeId);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        
    }

    @Test
    public void testGetReportError() {
        setupHeader();
        expect(repo.findByStoreIdAndSpaceIdAndDisplayTrueOrderByCompletionDateDesc(eq(storeId),
                                                                     eq(spaceId),
                                                                     isA(PageRequest.class))).andThrow(new RuntimeException("failure"));
        replayAll();
        createRest();
        Response response = rest.getReport(spaceId, storeId);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        
    }

    protected void setupHeader() {
        expect(request.getHeader(org.duracloud.common.rest.HttpHeaders.X_FORWARDED_HOST)).andReturn("test.duracloud.org");
    }

    protected void setupContentProperties() {
        expect(contentProperties.get(eq(StorageProvider.PROPERTIES_CONTENT_SIZE))).andReturn("100");
        expect(store.getContentProperties(eq(reportSpaceId),
                                          eq(reportContentId))).andReturn(contentProperties);
    }

    protected void setupStorageProviderFactory() {
        expect(storageProviderFactory.getStorageProvider()).andReturn(store);
    }

    protected void setupGetContent() {
        InputStream is = new ByteArrayInputStream("test".getBytes());
        expect(store.getContent(eq(reportSpaceId), eq(reportContentId))).andReturn(is);
    }

    protected void setupBitIntegrityReportRepo() {
        List<BitIntegrityReport> list = new LinkedList<>();
        BitIntegrityReport report = createMock(BitIntegrityReport.class);
        expect(report.getReportSpaceId()).andReturn(reportSpaceId);
        expect(report.getReportContentId()).andReturn(reportContentId);
        expect(report.getResult()).andReturn(BitIntegrityReportResult.SUCCESS);
        expect(report.getCompletionDate()).andReturn(new Date());
        list.add(report);

        expect(page.getContent()).andReturn(list).times(2);
        expect(repo.findByStoreIdAndSpaceIdAndDisplayTrueOrderByCompletionDateDesc(eq(storeId),
                                                                     eq(spaceId),
                                                                     isA(PageRequest.class))).andReturn(page);
    }

    protected void createRest() {
        rest = new BitIntegrityReportRest(repo, storageProviderFactory);
        rest.request = request;
    }

}
