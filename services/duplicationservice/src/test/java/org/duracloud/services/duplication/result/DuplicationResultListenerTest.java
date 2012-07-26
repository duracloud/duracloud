/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.result;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.easymock.EasyMock;
import org.easymock.IExpectationSetters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.CONTENT_CREATE;
import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.SPACE_CREATE;
import static org.duracloud.services.duplication.result.DuplicationResultListenerTest.MODE.INVALID;
import static org.duracloud.services.duplication.result.DuplicationResultListenerTest.MODE.VALID;
import static org.duracloud.services.duplication.result.DuplicationResultListenerTest.MODE.VALID_REPORT;

/**
 * @author Andrew Woods
 *         Date: 9/16/11
 */
public class DuplicationResultListenerTest {

    private static final String fromStoreId = "from-store-id";
    private static final String toStoreId = "to-store-id";

    private DuplicationResultListener listener;

    private ContentStore contentStore;
    private final String spaceId = "space-id";
    private final String reportId = "report-content-id";
    private final String errorReportId = "error-report-content-id";
    private String workDir;
    private final String mime = "text/tab-separated-values";

    @Before
    public void setUp() throws Exception {
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);

        File target = new File("target");
        Assert.assertTrue("target directory must exist!", target.exists());

        File work = new File(target, "test-dup-listener");
        if (!work.exists()) {
            Assert.assertTrue("Error: " + work.getAbsolutePath(), work.mkdir());
        }

        workDir = work.getAbsolutePath();
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore);
    }

    @Test
    public void testProcessResultSuccess() throws Exception {
        testProcessResult(true);
    }

    @Test
    public void testProcessResultFail() throws Exception {
        testProcessResult(false);
    }
    
    public void testProcessResult(boolean successfulEvent) throws Exception {
        createProcessResultMocks(VALID, successfulEvent);
        replayMocks();

        listener = new DuplicationResultListener(contentStore,
                                                 spaceId,
                                                 reportId,
                                                 errorReportId,
                                                 workDir);

        DuplicationEvent event = new DuplicationEvent(fromStoreId, toStoreId,
                                                      SPACE_CREATE, spaceId);
        if(!successfulEvent){
            event.fail("fail");
        }
        listener.processResult(event);
    }

    @Test
    public void testProcessResultReportId() throws Exception {
        createProcessResultMocks(VALID_REPORT, true);
        replayMocks();

        listener = new DuplicationResultListener(contentStore,
                                                 spaceId,
                                                 reportId,
                                                 errorReportId,
                                                 workDir);

        DuplicationEvent event = new DuplicationEvent(fromStoreId, toStoreId,
                                                      CONTENT_CREATE, spaceId, reportId);
        event.fail("failed");
        listener.processResult(event);
    }

    @Test
    public void testProcessResultError() throws Exception {
        createProcessResultMocks(INVALID, true);
        replayMocks();

        listener = new DuplicationResultListener(contentStore,
                                                 spaceId,
                                                 reportId,
                                                 errorReportId,
                                                 workDir);

        DuplicationEvent event = new DuplicationEvent(fromStoreId, toStoreId,
                                                      SPACE_CREATE, spaceId);
        // catches exception and writes a log.
        listener.processResult(event);
    }

    private void createProcessResultMocks(MODE mode, boolean successfulEvent)
        throws ContentStoreException {
        switch (mode) {
            case VALID:
                expectContentStoreCall(true, reportId);
                if(!successfulEvent) {
                    expectContentStoreCall(true, errorReportId);
                }
                break;

            case VALID_REPORT:
                break;

            case INVALID:
                expectContentStoreCall(false, reportId);
                if(!successfulEvent){
                    expectContentStoreCall(false, errorReportId);
                }
                break;

            default:
                Assert.fail("unexpected mode: " + mode);
        }

    }

    private void expectContentStoreCall(boolean valid, String contentId) throws ContentStoreException {
        IExpectationSetters<String> setters = EasyMock.expect(contentStore.addContent(EasyMock.eq(spaceId),
                                                EasyMock.eq(contentId),
                                                EasyMock.<InputStream>anyObject(),
                                                EasyMock.anyInt(),
                                                EasyMock.eq(mime),
                                                EasyMock.<String>isNull(),
                                                EasyMock.<Map<String, String>>isNull()));
        
        if(valid){
            setters.andReturn("md5");
        }else{
            setters.andThrow(new ContentStoreException("canned-exception"));            
        }
    }

    protected enum MODE {
        VALID, VALID_REPORT, INVALID;
    }
}
