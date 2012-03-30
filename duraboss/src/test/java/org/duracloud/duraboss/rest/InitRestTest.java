/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.appconfig.domain.NotificationConfig;
import org.duracloud.audit.Auditor;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.Credential;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.duraboss.rest.report.ServiceReportResource;
import org.duracloud.duraboss.rest.report.StorageReportResource;
import org.duracloud.exec.Executor;
import org.duracloud.manifest.ManifestGenerator;
import org.duracloud.reporter.notification.NotificationManager;
import org.duracloud.security.context.SecurityContextUtil;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.servicemonitor.ServiceSummarizer;
import org.duracloud.servicemonitor.ServiceSummaryDirectory;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;

import static org.junit.Assert.assertEquals;


/**
 * @author: Bill Branan
 * Date: 9/21/11
 */
public class InitRestTest {

    private StorageReportResource storageResource;
    private ServiceReportResource serviceResource;
    private RestUtil restUtil;
    private InitRest initRest;

    private ServiceSummaryDirectory serviceSummaryDirectory;
    private SecurityContextUtil securityContextUtil;
    private NotificationManager notificationManager;
    private Executor executor;
    private Auditor auditor;
    private ManifestGenerator manifestGenerator;

    private RestUtil.RequestContent requestContent;

    @Before
    public void setup() {
        storageResource = EasyMock.createMock(StorageReportResource.class);
        serviceResource = EasyMock.createMock(ServiceReportResource.class);
        restUtil = EasyMock.createMock(RestUtil.class);

        serviceSummaryDirectory = EasyMock.createMock("ServiceSummaryDirectory",
                                                      ServiceSummaryDirectory.class);
        securityContextUtil = EasyMock.createMock("SecurityContextUtil",
                                                  SecurityContextUtil.class);
        notificationManager = EasyMock.createMock("NotificationManager",
                                                  NotificationManager.class);
        executor = EasyMock.createMock("Executor", Executor.class);
        auditor = EasyMock.createMock("Auditor", Auditor.class);
        manifestGenerator = EasyMock.createMock("ManifestGenerator",
                                                ManifestGenerator.class);
        requestContent = EasyMock.createMock("RequestContent",
                                             RestUtil.RequestContent.class);
        initRest = new InitRest(storageResource,
                                serviceResource,
                                serviceSummaryDirectory,
                                securityContextUtil,
                                restUtil,
                                null,
                                notificationManager,
                                executor,
                                auditor,
                                manifestGenerator);
    }

    private void replayMocks() {
        EasyMock.replay(storageResource,
                        serviceResource,
                        restUtil,
                        serviceSummaryDirectory,
                        securityContextUtil,
                        notificationManager,
                        executor,
                        auditor,
                        manifestGenerator,
                        requestContent);
    }

    @After
    public void teardown() {
        EasyMock.verify(storageResource,
                        serviceResource,
                        restUtil,
                        serviceSummaryDirectory,
                        securityContextUtil,
                        notificationManager,
                        executor,
                        auditor,
                        manifestGenerator,
                        requestContent);
    }

    @Test
    public void testIsInitialized() {
        // Not initialized
        EasyMock.expect(storageResource.isInitialized()).andReturn(false);

        // Initialized
        EasyMock.expect(storageResource.isInitialized()).andReturn(true);
        EasyMock.expect(serviceResource.isInitialized()).andReturn(true);

        replayMocks();

        // Not initialized
        Response response = initRest.isInitialized();
        assertEquals(503, response.getStatus());

        // Initialized
        response = initRest.isInitialized();
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testInitialize() throws Exception {
        EasyMock.expect(requestContent.getContentStream()).andReturn(
            createConfigXml());
        EasyMock.expect(restUtil.getRequestContent(null, null)).andReturn(
            requestContent);

        EasyMock.expect(securityContextUtil.getCurrentUser())
                .andReturn(new Credential("user", "pass"));

        storageResource.initialize(EasyMock.<ContentStoreManager>anyObject(),
                                   EasyMock.<String>isNull());
        EasyMock.expectLastCall();

        serviceSummaryDirectory.initialize(EasyMock.<ContentStoreManager>anyObject());
        EasyMock.expectLastCall();

        serviceResource.initialize(EasyMock.<ServiceSummaryDirectory>anyObject(),
                                   EasyMock.<ServiceSummarizer>anyObject());
        EasyMock.expectLastCall();

        notificationManager.initializeNotifiers(EasyMock.<Collection<NotificationConfig>>anyObject());
        EasyMock.expectLastCall();

        executor.initialize(EasyMock.<ContentStoreManager>anyObject(),
                            EasyMock.<ServicesManager>anyObject());
        EasyMock.expectLastCall();

        auditor.initialize(EasyMock.<ContentStoreManager>anyObject());
        EasyMock.expectLastCall();

        manifestGenerator.initialize(EasyMock.<ContentStoreManager>anyObject());
        EasyMock.expectLastCall();

        replayMocks();

        Response response = initRest.initialize();
        Assert.assertNotNull(response);

        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        Assert.assertEquals("Initialization Successful", response.getEntity());
    }

    private InputStream createConfigXml() {
        String durastoreHost = "durastore-host";
        String durastorePort = "443";
        String durastoreContext = "durastore-context";
        String duraserviceHost = "duraservice-host";
        String duraservicePort = "443";
        String duraserviceContext = "duraservice-context";
        boolean reporterEnabled = true;
        boolean executorEnabled = true;
        boolean auditorEnabled = true;

        StringBuilder xml = new StringBuilder();
        xml.append("<durabossConfig>");
        xml.append("  <reporterEnabled>" + reporterEnabled);
        xml.append("</reporterEnabled>");
        xml.append("  <executorEnabled>" + executorEnabled);
        xml.append("</executorEnabled>");
        xml.append("  <auditorEnabled>" + auditorEnabled);
        xml.append("</auditorEnabled>");
        xml.append("  <durastoreHost>" + durastoreHost);
        xml.append("</durastoreHost>");
        xml.append("  <durastorePort>" + durastorePort);
        xml.append("</durastorePort>");
        xml.append("  <durastoreContext>" + durastoreContext);
        xml.append("</durastoreContext>");
        xml.append("  <duraserviceHost>" + duraserviceHost);
        xml.append("</duraserviceHost>");
        xml.append("  <duraservicePort>" + duraservicePort);
        xml.append("</duraservicePort>");
        xml.append("  <duraserviceContext>" + duraserviceContext);
        xml.append("</duraserviceContext>");

        String encUsername = encrypt("username");
        String encPassword = encrypt("password");
        String configType = "config-type";
        String configOriginator = "config-originator";

        xml.append("<notificationConfig>");
        xml.append("  <type>" + configType + "</type>");
        xml.append("  <username>" + encUsername +
                       "</username>");
        xml.append("  <password>" + encPassword +
                       "</password>");
        xml.append("  <originator>" + configOriginator +
                       "</originator>");
        xml.append("</notificationConfig>");

        xml.append("</durabossConfig>");

        return new AutoCloseInputStream(new ByteArrayInputStream(xml.toString()
                                                                    .getBytes()));
    }

    private static String encrypt(String text) {
        try {
            EncryptionUtil encryptionUtil = new EncryptionUtil();
            return encryptionUtil.encrypt(text);
        } catch (Exception e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

}
