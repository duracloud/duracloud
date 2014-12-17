/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.storage.util.StorageProviderFactory;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.xml.StorageAccountsDocumentBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Provides interaction with storage providers accounts via REST
 *
 * @author Bill Branan
 */
@Path("/stores")
@Component
public class StoreRest extends BaseRest {
    private final Logger log = LoggerFactory.getLogger(StoreRest.class);

    private StorageProviderFactory storageProviderFactory;
    private StorageAccountsDocumentBinding documentBinding;

    @Autowired
    public StoreRest(StorageProviderFactory storageProviderFactory,
                     StorageAccountsDocumentBinding documentBinding) {
        this.storageProviderFactory = storageProviderFactory;
        this.documentBinding = documentBinding;
    }

    /**
     * Provides a listing of all available storage provider accounts
     *
     * @return 200 response with XML file listing stores
     */
    @GET
    public Response getStores(){
        String msg = "getting stores.";
        try {
            return doGetStores(msg);

        } catch (StorageException se) {
            return responseBad(msg, se);

        } catch (Exception e) {
            return responseBad(msg, e);
        }
    }

    private Response doGetStores(String msg) {
        List<StorageAccount> accts = storageProviderFactory.getStorageAccounts();
        boolean includeCredentials = false;
        boolean includeOptions = false;
        String xml = documentBinding.createXmlFrom(accts,
                                                   includeCredentials,
                                                   includeOptions);
        return responseOkXml(msg, xml);
    }

    private Response responseOkXml(String msg, String xml) {
        log.debug(msg);
        return Response.ok(xml, APPLICATION_XML).build();
    }

    private Response responseBad(String msg, Exception e) {
        log.error("Error: " + msg, e);
        String entity = e.getMessage() == null ? "null" : e.getMessage();
        return Response.serverError().entity(entity).build();
    }

}
