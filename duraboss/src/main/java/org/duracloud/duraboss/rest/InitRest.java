/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest;

import org.duracloud.appconfig.domain.DurabossConfig;
import org.duracloud.appconfig.xml.DurabossInitDocumentBinding;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.client.ServicesManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.common.util.InitUtil;
import org.duracloud.duraboss.rest.report.ServiceReportResource;
import org.duracloud.duraboss.rest.report.StorageReportResource;
import org.duracloud.exec.Executor;
import org.duracloud.reporter.notification.NotificationManager;
import org.duracloud.security.context.SecurityContextUtil;
import org.duracloud.security.error.NoUserLoggedInException;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.servicemonitor.ServiceSummarizer;
import org.duracloud.servicemonitor.ServiceSummaryDirectory;
import org.duracloud.servicemonitor.impl.ServiceSummarizerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.InputStream;

import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
@Path("/init")
public class InitRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(InitRest.class);

    private StorageReportResource storageResource;
    private ServiceReportResource serviceResource;
    private ServiceSummaryDirectory summaryDirectory;
    private SecurityContextUtil securityContextUtil;
    private RestUtil restUtil;
    private String reportSpaceId;
    private NotificationManager notificationManager;
    private Executor executor;

    public InitRest(StorageReportResource storageResource,
                    ServiceReportResource serviceResource,
                    ServiceSummaryDirectory summaryDirectory,
                    SecurityContextUtil securityContextUtil,
                    RestUtil restUtil,
                    String reportSpaceId,
                    NotificationManager notificationManager,
                    Executor executor) {
        this.storageResource = storageResource;
        this.serviceResource = serviceResource;
        this.summaryDirectory = summaryDirectory;
        this.securityContextUtil = securityContextUtil;
        this.restUtil = restUtil;
        this.reportSpaceId = reportSpaceId;
        this.notificationManager = notificationManager;
        this.executor = executor;
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

        ContentStoreManager storeMgr =
            new ContentStoreManagerImpl(config.getDurastoreHost(),
                                        config.getDurastorePort(),
                                        config.getDurastoreContext());
        storeMgr.login(credential);
        storageResource.initialize(storeMgr, reportSpaceId);
        summaryDirectory.initialize(storeMgr);

        ServicesManager servicesMgr =
            new ServicesManagerImpl(config.getDuraserviceHost(),
                                    config.getDuraservicePort(),
                                    config.getDuraserviceContext());
        servicesMgr.login(credential);
        ServiceSummarizer summarizer = new ServiceSummarizerImpl(servicesMgr);
        serviceResource.initialize(summaryDirectory, summarizer);

        notificationManager.initializeNotifiers(config.getNotificationConfigs());

        executor.initialize(storeMgr, servicesMgr);
    }

    @GET
    public Response isInitialized() {
        log.debug("checking initialized");

        boolean initialized = storageResource.isInitialized() &&
                              serviceResource.isInitialized();
        if(initialized) {
            String text = InitUtil.getInitializedText(APP_NAME);
            return responseOk(text);
        } else {
            String text = InitUtil.getNotInitializedText(APP_NAME);
            return responseBad(text, SERVICE_UNAVAILABLE);
        }
    }

}
