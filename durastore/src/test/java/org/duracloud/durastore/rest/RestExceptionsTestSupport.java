/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.rest.RestUtil;
import org.duracloud.durastore.error.ResourceException;
import org.duracloud.durastore.util.StorageProviderFactory;
import org.duracloud.durastore.util.TaskProviderFactory;
import org.duracloud.security.DuracloudUserDetailsService;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;

import javax.ws.rs.core.Response;

/**
 * @author Andrew Woods
 *         Date: Aug 31, 2010
 */
public class RestExceptionsTestSupport {


    protected void verifyErrorResponse(Response response) {
        Assert.assertNotNull(response);

        String entity = (String) response.getEntity();
        Assert.assertNotNull(entity);

        int status = response.getStatus();
        int expectedStatus = Response.Status
            .INTERNAL_SERVER_ERROR
            .getStatusCode();
        Assert.assertEquals(expectedStatus, status);
    }

    protected TaskProviderFactory createTaskProviderFactory() throws Exception {
        TaskProviderFactory factory = EasyMock.createMock("TaskProviderFactory",
                                                          TaskProviderFactory.class);
        EasyMock.expect(factory.getTaskProvider(null)).andThrow(
            createRuntimeException()).anyTimes();

        EasyMock.replay(factory);
        return factory;
    }

    protected StorageProviderFactory createStorageProviderFactory()
        throws Exception {
        StorageProviderFactory factory = EasyMock.createMock(
            "StorageProviderFactory",
            StorageProviderFactory.class);
        EasyMock.expect(factory.getStorageAccounts()).andThrow(
            createRuntimeException()).anyTimes();

        EasyMock.replay(factory);
        return factory;
    }

    protected SpaceResource createSpaceResource() throws ResourceException {
        SpaceResource resource = EasyMock.createMock("SpaceResource",
                                                     SpaceResource.class);
        EasyMock.expect(resource.getSpaces(null)).andThrow(
            createRuntimeException()).anyTimes();
        EasyMock.expect(resource.getSpaceContents(null, null, null, -1, null))
            .andThrow(createRuntimeException())
            .anyTimes();
        EasyMock.expect(resource.getSpaceMetadata(null, null)).andThrow(
            createRuntimeException()).anyTimes();
        resource.deleteSpace(null, null);
        EasyMock.expectLastCall().andThrow(createRuntimeException()).anyTimes();

        EasyMock.replay(resource);
        return resource;
    }

    protected DuracloudUserDetailsService createUserDetailsService() {
        DuracloudUserDetailsService details = EasyMock.createMock(
            "DetailsService",
            DuracloudUserDetailsService.class);

        EasyMock.replay(details);
        return details;
    }

    protected RestUtil createRestUtil() throws Exception {
        RestUtil util = EasyMock.createMock("RestUtil", RestUtil.class);
        EasyMock.expect(util.getRequestContent(null, null)).andThrow(
            createRuntimeException()).anyTimes();

        EasyMock.replay(util);
        return util;
    }

    protected Throwable createRuntimeException() {
        Throwable t = null;
        try {
            t.toString();
        } catch (Exception e) {
            t = e;
        }
        Assert.assertNotNull(t);
        return t;
    }


}
