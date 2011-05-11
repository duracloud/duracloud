/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.rest.RestUtil;
import org.duracloud.durastore.util.StorageProviderFactory;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.xml.StorageAccountsDocumentBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Provides interaction with storage providers accounts via REST
 *
 * @author Bill Branan
 */
@Path("/stores")
public class StoreRest extends BaseRest {
    private final Logger log = LoggerFactory.getLogger(StoreRest.class);

    private StorageProviderFactory storageProviderFactory;
    private RestUtil restUtil;
    private StorageAccountsDocumentBinding documentBinding;

    public StoreRest(StorageProviderFactory storageProviderFactory, RestUtil restUtil) {
        this.storageProviderFactory = storageProviderFactory;
        this.restUtil = restUtil;
        this.documentBinding = new StorageAccountsDocumentBinding();
    }

    /**
     * Initializes the instance. Expects as POST data
     * an XML file which includes credentials for all
     * available storage providers accounts.
     *
     * @return 200 on success
     */
    @POST
    public Response initializeStores(){
        String msg = "initializing stores.";

        RestUtil.RequestContent content = null;
        try {
            content = restUtil.getRequestContent(request, headers);
            storageProviderFactory.initialize(content.getContentStream());
            String responseText = "Initialization Successful";
            return responseOk(msg, responseText);

        } catch (Exception e) {
            return responseBad(msg, e);
        }
    }

    /**
     * Provides a listing of all available storage provider accounts
     *
     * @return 200 response with XML file listing stores
     */
    @GET
    public Response getStores(){
        try {
            return doGetStores();

        } catch (StorageException se) {
            return responseBad("getting stores.", se);

        } catch (Exception e) {
            return responseBad("getting stores.", e);
        }
    }

    private Response doGetStores() {
        List<StorageAccount> accts = storageProviderFactory.getStorageAccounts();
        boolean includeCredentials = false;
        String xml = documentBinding.createDocumentFrom(accts,
                                                        includeCredentials);

        return Response.ok(xml, TEXT_XML).build();
    }

    private Response responseOk(String msg, String text) {
        log.debug(msg);
        return Response.ok(text, TEXT_PLAIN).build();
    }

    private Response responseBad(String msg, Exception e) {
        log.error("Error: " + msg, e);
        String entity = e.getMessage() == null ? "null" : e.getMessage();
        return Response.serverError().entity(entity).build();
    }

}
