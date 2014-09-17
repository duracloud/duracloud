/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;

import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.appconfig.domain.NotificationConfig;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.Credential;
import org.duracloud.common.notification.NotificationManager;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.duraboss.rest.report.StorageReportResource;
import org.duracloud.security.context.SecurityContextUtil;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author: Bill Branan
 * Date: 9/21/11
 */
public class InitRestTest {

    private StorageReportResource storageResource;
    private RestUtil restUtil;
    private InitRest initRest;

    private SecurityContextUtil securityContextUtil;
    private NotificationManager notificationManager;

    private RestUtil.RequestContent requestContent;

    @Before
    public void setup() {
        storageResource = EasyMock.createMock(StorageReportResource.class);
        restUtil = EasyMock.createMock(RestUtil.class);

        securityContextUtil = EasyMock.createMock("SecurityContextUtil",
                                                  SecurityContextUtil.class);
        notificationManager = EasyMock.createMock("NotificationManager",
                                                  NotificationManager.class);
        requestContent = EasyMock.createMock("RequestContent",
                                             RestUtil.RequestContent.class);
        initRest = new InitRest(storageResource,
                                securityContextUtil,
                                restUtil,
                                null,
                                notificationManager);
    }

    private void replayMocks() {
        EasyMock.replay(storageResource,
                        restUtil,
                        securityContextUtil,
                        notificationManager,
                        requestContent);
    }

    @After
    public void teardown() {
        EasyMock.verify(storageResource,
                        restUtil,
                        securityContextUtil,
                        notificationManager,
                        requestContent);
    }

    @Test
    public void testIsInitialized() {
        // Not initialized
        EasyMock.expect(storageResource.isInitialized()).andReturn(false);

        // Initialized
        EasyMock.expect(storageResource.isInitialized()).andReturn(true);

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

        notificationManager.initializeNotifiers(EasyMock.<Collection<NotificationConfig>>anyObject());
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
        boolean reporterEnabled = true;
        boolean auditorEnabled = true;

        StringBuilder xml = new StringBuilder();
        xml.append("<durabossConfig>");
        xml.append("  <reporterEnabled>" + reporterEnabled);
        xml.append("</reporterEnabled>");
        xml.append("  <auditorEnabled>" + auditorEnabled);
        xml.append("</auditorEnabled>");
        xml.append("  <durastoreHost>" + durastoreHost);
        xml.append("</durastoreHost>");
        xml.append("  <durastorePort>" + durastorePort);
        xml.append("</durastorePort>");
        xml.append("  <durastoreContext>" + durastoreContext);
        xml.append("</durastoreContext>");

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
