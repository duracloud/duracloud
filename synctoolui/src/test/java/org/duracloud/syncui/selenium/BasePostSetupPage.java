/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.selenium;

import junit.framework.Assert;
import org.apache.activemq.util.ByteArrayOutputStream;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.syncui.controller.InitController;

import java.io.File;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public abstract class BasePostSetupPage extends BaseSeleniumTest {
    protected File uploadDir = null;
    protected File workingDir = null;
    protected File configXmlLocation = null;

    @Override
    public void before() throws Exception {
        super.before();


        String tempDir = System.getProperty("java.io.tmpdir");

        this.workingDir =
            new File(tempDir
                + File.separator + "sync-working-dir-"
                + System.currentTimeMillis());
        this.workingDir.mkdirs();
        props.put("workingDirectory", workingDir.getAbsolutePath());

        this.uploadDir =
            new File(tempDir
                + File.separator + "sync-upload-dir-"
                + System.currentTimeMillis());

        this.uploadDir.mkdirs();
        props.put("uploadDirectories", uploadDir.getAbsolutePath());

        this.configXmlLocation =
            new File(tempDir
                + File.separator + "sync-config-"
                + System.currentTimeMillis() + ".xml");

        this.workingDir.mkdirs();
        props.put("configXmlLocation", configXmlLocation.getAbsolutePath());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        props.store(os, null);
        String requestContent = new String(os.toByteArray());

        RestHttpHelper helper = new RestHttpHelper();
        HttpResponse response =
            helper.post(getBaseUrl() + InitController.INIT_MAPPING,
                        requestContent,
                        null);
        Assert.assertEquals(200, response.getStatusCode());
    }

    @Override
    public void after() {
        this.workingDir.delete();
        this.uploadDir.delete();

        if (this.configXmlLocation.exists()) {
            this.configXmlLocation.delete();
        }

        super.after();
    }

}
