/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import static org.junit.Assert.*;

import javax.ws.rs.core.Response;

import org.apache.commons.dbcp.BasicDataSource;
import org.duracloud.audit.reader.AuditLogReader;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.storage.util.StorageProviderFactory;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author: Bill Branan
 * Date: 9/21/11
 */
public class InitRestTest {

    private StorageProviderFactory storageProviderFactory;
    private RestUtil restUtil;
    private InitRest initRest;
    private BasicDataSource datasource;
    private AuditLogReader reader;
    private ManifestRest manifestRest;

    @Before
    public void setup() {
        storageProviderFactory =
            EasyMock.createMock(StorageProviderFactory.class);
        restUtil = EasyMock.createMock(RestUtil.class);
        datasource = EasyMock.createMock(BasicDataSource.class);
        reader =  EasyMock.createMock(AuditLogReader.class);
        manifestRest = EasyMock.createMock(ManifestRest.class);
        initRest = new InitRest(storageProviderFactory, restUtil, datasource, reader, manifestRest);
    }

    private void replayMocks() {
        EasyMock.replay(storageProviderFactory, restUtil, datasource, reader, manifestRest);
    }

    @After
    public void teardown() {
        EasyMock.verify(storageProviderFactory, restUtil, datasource, reader, manifestRest);
    }

    @Test
    public void testIsInitialized() {
        // Not initialized
        EasyMock.expect(storageProviderFactory.isInitialized())
            .andReturn(false)
            .times(1);

        // Initialized
        EasyMock.expect(storageProviderFactory.isInitialized())
            .andReturn(true)
            .times(1);
        
        replayMocks();

        // Not initialized
        Response response = initRest.isInitialized();
        assertEquals(503, response.getStatus());

        // Initialized
        response = initRest.isInitialized();
        assertEquals(200, response.getStatus());
    }

}
