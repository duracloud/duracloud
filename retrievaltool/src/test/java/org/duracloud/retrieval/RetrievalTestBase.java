/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.duracloud.common.model.ContentItem;
import org.duracloud.retrieval.mgmt.OutputWriter;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;

/**
 * @author: Bill Branan
 * Date: Oct 15, 2010
 */
public abstract class RetrievalTestBase {

    protected File tempDir;

    @Before
    public void setUp() throws Exception {
        tempDir = new File("target/" + this.getClass().getName());
        tempDir.mkdir();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(tempDir);
    }

    protected OutputWriter createMockOutputWriter() {
        OutputWriter outWriter = EasyMock.createMock(OutputWriter.class);
        outWriter.writeSuccess(EasyMock.isA(ContentItem.class),
                               EasyMock.isA(String.class),
                               EasyMock.anyInt());
        EasyMock.expectLastCall().anyTimes();
        outWriter.writeFailure(EasyMock.isA(ContentItem.class),
                               EasyMock.isA(String.class),
                               EasyMock.anyInt());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(outWriter);
        return outWriter;
    }
}
