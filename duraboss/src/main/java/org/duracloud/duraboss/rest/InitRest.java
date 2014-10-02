/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest;

import static javax.ws.rs.core.Response.Status.*;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.duracloud.appconfig.domain.DurabossConfig;
import org.duracloud.appconfig.xml.DurabossInitDocumentBinding;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.error.NoUserLoggedInException;
import org.duracloud.common.model.Credential;
import org.duracloud.common.notification.NotificationManager;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.common.util.InitUtil;
import org.duracloud.duraboss.rest.report.StorageReportResource;
import org.duracloud.security.context.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
@Path("/init")
@Component
public class InitRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(InitRest.class);

    private StorageReportResource storageResource;
    private SecurityContextUtil securityContextUtil;
    private RestUtil restUtil;
    private String reportSpaceId;
    private NotificationManager notificationManager;

    @Autowired
    public InitRest(StorageReportResource storageResource,
                    SecurityContextUtil securityContextUtil,
                    RestUtil restUtil,
                    String reportSpaceId,
                    NotificationManager notificationManager) {
        this.storageResource = storageResource;
        this.securityContextUtil = securityContextUtil;
        this.restUtil = restUtil;
        this.reportSpaceId = reportSpaceId;
        this.notificationManager = notificationManager;
    }

    /**
     * Initializes DuraBoss
     *
     * @return 200 response with text indicating success
     */
    @POST
    public Response initialize(){
        log.debug("Initializing " + APP_NAME);

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
        DurabossConfig config =
            DurabossInitDocumentBinding.createDurabossConfigFrom(xml);

        Credential credential = securityContextUtil.getCurrentUser();

        String host = config.getDurastoreHost();
        String port = config.getDurastorePort();

        String context = config.getDurastoreContext();
        ContentStoreManager storeMgr =
            new ContentStoreManagerImpl(host, port, context);
        storeMgr.login(credential);

        context = config.getDurabossContext();
       
        notificationManager.initializeNotifiers(
            config.getNotificationConfigs());

        // Only initialize the Reporter if it is enabled
        if(config.isReporterEnabled()) {
            // Initialize Storage Reporter
            storageResource.initialize(storeMgr, reportSpaceId);
        }
    }

    @GET
    public Response isInitialized() {
        log.debug("checking initialized");

        boolean initialized = storageResource.isInitialized();
        if(initialized) {
            String text = InitUtil.getInitializedText(APP_NAME);
            return responseOk(text);
        } else {
            String text = InitUtil.getNotInitializedText(APP_NAME);
            return responseBad(text, SERVICE_UNAVAILABLE);
        }
    }

}
