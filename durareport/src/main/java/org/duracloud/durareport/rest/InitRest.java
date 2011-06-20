/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.rest;

import org.duracloud.appconfig.domain.DurareportConfig;
import org.duracloud.appconfig.xml.DurareportInitDocumentBinding;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.client.ServicesManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.security.context.SecurityContextUtil;
import org.duracloud.security.error.NoUserLoggedInException;
import org.duracloud.serviceapi.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
@Path("/reports")
public class InitRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(InitRest.class);

    StorageReportResource storageResource;
    ServiceReportResource serviceResource;
    SecurityContextUtil securityContextUtil;
    private RestUtil restUtil;

    public InitRest(StorageReportResource storageResource,
                    ServiceReportResource serviceResource,
                    SecurityContextUtil securityContextUtil,
                    RestUtil restUtil) {
        this.storageResource = storageResource;
        this.serviceResource = serviceResource;
        this.securityContextUtil = securityContextUtil;
        this.restUtil = restUtil;
    }

    /**
     * Initializes DuraReport
     *
     * @return 200 response with text indicating success
     */
    @POST
    public Response initialize(){
        log.debug("Initializing DuraReport");

        RestUtil.RequestContent content = null;
        try {
            content = restUtil.getRequestContent(request, headers);
            doInitialize(content.getContentStream());
            String responseText = "Initialization Successful";
            return responseOk(responseText);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    private void doInitialize(InputStream xml) throws NoUserLoggedInException {
        DurareportConfig config =
            DurareportInitDocumentBinding.createDurareportConfigFrom(xml);

        Credential credential = securityContextUtil.getCurrentUser();

        ContentStoreManager storeMgr =
            new ContentStoreManagerImpl(config.getDurastoreHost(),
                                        config.getDurastorePort(),
                                        config.getDurastoreContext());
        storeMgr.login(credential);
        storageResource.initialize(storeMgr);

        ServicesManager servicesMgr =
            new ServicesManagerImpl(config.getDuraserviceHost(),
                                    config.getDuraservicePort(),
                                    config.getDuraserviceContext());
        servicesMgr.login(credential);
        serviceResource.initialize(servicesMgr);
    }
    
}
