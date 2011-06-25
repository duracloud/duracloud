/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.serviceconfig.ServiceSummariesDocument;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.servicemonitor.ServiceSummaryDirectory;
import org.duracloud.servicemonitor.error.ServiceSummaryNotFoundException;
import org.duracloud.error.NotFoundException;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.duracloud.servicemonitor.impl.ServiceSummaryDirectoryImplTest.MODE.SUCCESS;
import static org.duracloud.servicemonitor.impl.ServiceSummaryDirectoryImplTest.MODE.EMPTY;
import static org.duracloud.servicemonitor.impl.ServiceSummaryDirectoryImplTest.MODE.ERROR;

/**
 * @author Andrew Woods
 *         Date: 6/24/11
 */
public class ServiceSummaryDirectoryImplTest {

    private ServiceSummaryDirectory directory;

    private ContentStore contentStore;
    private String spaceId = "space-id";
    private String contentId = "content-id-" + ServiceSummaryDirectory.DATE_VAR;

    private static final int NUM_SUMMARIES = 3;
    private List<ServiceSummary> summaries = new ArrayList<ServiceSummary>();

    protected enum MODE {
        SUCCESS, ERROR, EMPTY;
    }

    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < NUM_SUMMARIES; ++i) {
            ServiceSummary summary = new ServiceSummary();
            summary.setId(i);
            summary.setDeploymentId(i);
            summaries.add(summary);
        }

        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);

        directory = new ServiceSummaryDirectoryImpl(spaceId,
                                                    contentId,
                                                    contentStore);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore);
    }

    @Test
    public void testGetCurrentServiceSummaries() throws ContentStoreException {
        doTestGetCurrentServiceSummaries(SUCCESS);
    }

    @Test
    public void testGetCurrentServiceSummariesError()
        throws ContentStoreException {
        doTestGetCurrentServiceSummaries(ERROR);
    }

    @Test
    public void testGetCurrentServiceSummariesEmpty()
        throws ContentStoreException {
        doTestGetCurrentServiceSummaries(EMPTY);
    }

    private void doTestGetCurrentServiceSummaries(MODE mode)
        throws ContentStoreException {
        createMocksGetCurrentServiceSummaries(mode);
        replayMocks();

        List<ServiceSummary> summaries = null;

        boolean error = false;
        boolean empty = false;
        try {
            summaries = directory.getCurrentServiceSummaries();
            Assert.assertTrue("exception expected", mode.equals(SUCCESS));

        } catch (DuraCloudRuntimeException e) {
            error = true;

        } catch (ServiceSummaryNotFoundException e) {
            empty = true;
        }
        Assert.assertEquals(mode.equals(ERROR), error);
        Assert.assertEquals(mode.equals(EMPTY), empty);

        if (mode.equals(SUCCESS)) {
            Assert.assertNotNull(summaries);
            Assert.assertEquals(NUM_SUMMARIES, summaries.size());
        }
    }

    @Test
    public void testGetCurrentServiceSummariesStream()
        throws ContentStoreException {
        doTestGetCurrentServiceSummariesStream(SUCCESS);
    }

    @Test
    public void testGetCurrentServiceSummariesStreamEmpty()
        throws ContentStoreException {
        doTestGetCurrentServiceSummariesStream(EMPTY);
    }

    @Test
    public void testGetCurrentServiceSummariesStreamError()
        throws ContentStoreException {
        doTestGetCurrentServiceSummariesStream(ERROR);
    }

    private void doTestGetCurrentServiceSummariesStream(MODE mode)
        throws ContentStoreException {
        createMocksGetCurrentServiceSummaries(mode);
        replayMocks();

        List<ServiceSummary> summaries = null;

        boolean error = false;
        boolean empty = false;
        try {
            InputStream is = directory.getCurrentServiceSummariesStream();
            Assert.assertTrue("exception expected", mode.equals(SUCCESS));

            summaries = ServiceSummariesDocument.getServiceSummaryList(is);

        } catch (DuraCloudRuntimeException e) {
            error = true;

        } catch (ServiceSummaryNotFoundException e) {
            empty = true;
        }
        Assert.assertEquals(mode.equals(ERROR), error);
        Assert.assertEquals(mode.equals(EMPTY), empty);

        if (mode.equals(SUCCESS)) {
            Assert.assertNotNull(summaries);
            Assert.assertEquals(NUM_SUMMARIES, summaries.size());
        }
    }

    @Test
    public void testGetServiceSummariesById() throws ContentStoreException {
        doTestServiceSummariesById(SUCCESS);
    }

    @Test
    public void testGetServiceSummariesByIdEmpty()
        throws ContentStoreException {
        doTestServiceSummariesById(EMPTY);
    }

    @Test
    public void testGetServiceSummariesByIdError()
        throws ContentStoreException {
        doTestServiceSummariesById(ERROR);
    }

    private void doTestServiceSummariesById(MODE mode)
        throws ContentStoreException {
        createMocksGetCurrentServiceSummaries(mode);
        replayMocks();

        List<ServiceSummary> summaries = null;

        boolean error = false;
        boolean empty = false;
        try {
            summaries = directory.getServiceSummariesById("id");
            Assert.assertTrue("exception expected", mode.equals(SUCCESS));

        } catch (DuraCloudRuntimeException e) {
            error = true;

        } catch (ServiceSummaryNotFoundException e) {
            empty = true;
        }
        Assert.assertEquals(mode.equals(ERROR), error);
        Assert.assertEquals(mode.equals(EMPTY), empty);

        if (mode.equals(SUCCESS)) {
            Assert.assertNotNull(summaries);
            Assert.assertEquals(NUM_SUMMARIES, summaries.size());
        }
    }

    @Test
    public void testGetServiceSummariesStreamById()
        throws ContentStoreException {
        doTestServiceSummariesStreamById(SUCCESS);
    }

    @Test
    public void testGetServiceSummariesStreamByIdError()
        throws ContentStoreException {
        doTestServiceSummariesStreamById(ERROR);
    }

    @Test
    public void testGetServiceSummariesStreamByIdEmpty()
        throws ContentStoreException {
        doTestServiceSummariesStreamById(EMPTY);
    }

    private void doTestServiceSummariesStreamById(MODE mode)
        throws ContentStoreException {
        createMocksGetCurrentServiceSummaries(mode);
        replayMocks();

        List<ServiceSummary> summaries = null;

        boolean error = false;
        boolean empty = false;
        try {
            InputStream is = directory.getServiceSummariesStreamById("id");
            Assert.assertTrue("exception expected", mode.equals(SUCCESS));

            summaries = ServiceSummariesDocument.getServiceSummaryList(is);

        } catch (DuraCloudRuntimeException e) {
            error = true;

        } catch (ServiceSummaryNotFoundException e) {
            empty = true;
        }
        Assert.assertEquals(mode.equals(ERROR), error);
        Assert.assertEquals(mode.equals(EMPTY), empty);

        if (mode.equals(SUCCESS)) {
            Assert.assertNotNull(summaries);
            Assert.assertEquals(NUM_SUMMARIES, summaries.size());
        }
    }

    @Test
    public void testGetServiceSummaryIds() throws Exception {
        doTestGetServiceSummaryIds(SUCCESS);
    }

    @Test
    public void testGetServiceSummaryIdsError() throws Exception {
        doTestGetServiceSummaryIds(ERROR);
    }

    private void doTestGetServiceSummaryIds(MODE mode)
        throws ContentStoreException {
        createMocksGetServiceSummaryIds(mode);
        replayMocks();

        List<String> ids = null;

        boolean error = false;
        try {
            ids = directory.getServiceSummaryIds();
            Assert.assertTrue("exception expected", mode.equals(SUCCESS));

        } catch (DuraCloudRuntimeException e) {
            error = true;
        }

        Assert.assertEquals(mode.equals(ERROR), error);

        if (mode.equals(SUCCESS)) {
            Assert.assertNotNull(ids);
            Assert.assertEquals(NUM_SUMMARIES, ids.size());
        }
    }

    @Test
    public void testAddServiceSummary() throws Exception {
        doTestAddServiceSummary(SUCCESS);
    }

    @Test
    public void testAddServiceSummaryError() throws Exception {
        doTestAddServiceSummary(ERROR);
    }

    private void doTestAddServiceSummary(MODE mode)
        throws ContentStoreException {
        createMocksAddServiceSummary(mode);
        replayMocks();

        ServiceSummary summary = new ServiceSummary();
        summary.setId(1);
        summary.setDeploymentId(2);

        boolean error = false;
        try {
            directory.addServiceSummary(summary);
            Assert.assertTrue("exception expected", mode.equals(SUCCESS));

        } catch (DuraCloudRuntimeException e) {
            error = true;
        }

        Assert.assertEquals(mode.equals(ERROR), error);
    }

    //---
    // Mock set-up below
    //---

    private void createMocksGetCurrentServiceSummaries(MODE mode)
        throws ContentStoreException {
        if (mode.equals(SUCCESS)) {
            String summariesXml = ServiceSummariesDocument.getServiceSummaryListAsXML(
                summaries);
            Content content = EasyMock.createMock("Content", Content.class);

            EasyMock.expect(content.getStream())
                .andReturn(new ByteArrayInputStream(summariesXml.getBytes()));
            EasyMock.replay(content);

            EasyMock.expect(contentStore.getContent(EasyMock.eq(spaceId),
                                                    EasyMock.<String>anyObject()))
                .andReturn(content);

        } else if (mode.equals(ERROR)) {
            EasyMock.expect(contentStore.getContent(EasyMock.eq(spaceId),
                                                    EasyMock.<String>anyObject()))
                .andThrow(new ContentStoreException("canned-excepton"));

        } else {
            EasyMock.expect(contentStore.getContent(EasyMock.eq(spaceId),
                                                    EasyMock.<String>anyObject()))
                .andThrow(new NotFoundException("canned-excepton"));
        }
    }

    private void createMocksGetServiceSummaryIds(MODE mode)
        throws ContentStoreException {
        if (mode.equals(SUCCESS)) {
            List<String> ids = new ArrayList<String>();
            for (ServiceSummary summary : summaries) {
                ids.add(summary.getName());
            }

            EasyMock.expect(contentStore.getSpaceContents(EasyMock.eq(spaceId),
                                                          EasyMock.<String>anyObject()))
                .andReturn(ids.iterator());

        } else if (mode.equals(ERROR)) {
            EasyMock.expect(contentStore.getSpaceContents(EasyMock.eq(spaceId),
                                                          EasyMock.<String>anyObject()))
                .andThrow(new ContentStoreException("canned-excepton"));
        }
    }

    private void createMocksAddServiceSummary(MODE mode)
        throws ContentStoreException {
        createMocksGetCurrentServiceSummaries(mode);
        if (mode.equals(SUCCESS)) {
            EasyMock.expect(contentStore.addContent(EasyMock.eq(spaceId),
                                                    EasyMock.<String>anyObject(),
                                                    EasyMock.<InputStream>anyObject(),
                                                    EasyMock.anyLong(),
                                                    EasyMock.<String>isNull(),
                                                    EasyMock.<String>isNull(),
                                                    EasyMock.<Map<String, String>>isNull()))
                .andReturn(null);
        }
    }

}
