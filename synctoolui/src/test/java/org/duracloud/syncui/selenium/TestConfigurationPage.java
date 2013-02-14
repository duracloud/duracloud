/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.selenium;

import java.io.File;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
/**
 * 
 * @author Daniel Bernstein
 *
 */
public class TestConfigurationPage extends BasePostSetupPage {
    @Test
    public void testGet(){
        openConfigurationPage();
        Assert.assertTrue(isElementPresent("css=#watched-directories"));
        Assert.assertTrue(isTextPresent(uploadDir.getAbsolutePath()));

        Assert.assertTrue(isElementPresent("css=#duracloud-configuration"));
        Assert.assertTrue(isTextPresent(props.getProperty("username")));

    }
    
    @Test
    public void testAddCancel() throws Exception{
        openConfigurationPage();
        Assert.assertTrue(isElementPresent("css=#add"));
        sc.click("css=#add");
        Thread.sleep(2000);
        Assert.assertTrue(sc.isVisible("css=#directoryConfigForm"));
        sc.click("css=#cancel");
        Thread.sleep(500);
        Assert.assertFalse(sc.isVisible("css=#directoryConfigForm"));
    }

    @Test
    public void testAddAndRemoveDirectory() throws Exception{
        
        File testDir =
            new File(System.getProperty("java.io.tmpdir")
                + File.separator + System.currentTimeMillis());
        
        testDir.mkdirs();
        testDir.deleteOnExit();
        
        openConfigurationPage();
        Assert.assertTrue(isElementPresent("css=#add"));
        sc.click("css=#add");
        Thread.sleep(2000);
        Assert.assertTrue(sc.isVisible("css=#directoryConfigForm"));

        String[] list = testDir.getAbsolutePath().split(File.separator);
        String path = "/";
        
        for(String dir : list){
            if("".equals(dir)) continue;
            
            path += dir + "/";
             String pathSelector = "css=a[rel='"+path+"']";
             log.debug("checking if " + pathSelector + " is present");
             Assert.assertTrue(isElementPresent(pathSelector));
             sc.click(pathSelector);
             Thread.sleep(2000);
        }
        sc.click("css=#directoryConfigForm #add");
        Thread.sleep(1000);
        Assert.assertFalse(sc.isElementPresent("css=#directoryConfigForm"));
        Assert.assertTrue(sc.isTextPresent(testDir.getAbsolutePath()));
        String removeButton = "css=#" + testDir.getName() + "-remove";
        Assert.assertTrue(sc.isElementPresent(removeButton));
        clickAndWait(removeButton);
        Assert.assertFalse(sc.isElementPresent(removeButton));

    }
    

    @Test
    public void testEditEnterInvalidDataCancelDuracloudConfig() throws Exception{
        openConfigurationPage();
        Assert.assertTrue(isElementPresent("css=#edit"));
        sc.click("css=#edit");
        Thread.sleep(2000);
        Assert.assertTrue(sc.isVisible("css=#duracloudCredentialsForm"));

        sc.type("css=#username", "");
        sc.click("css=#next");
        Thread.sleep(2000);
        Assert.assertTrue(sc.isVisible("css=#duracloudCredentialsForm #username.error"));

        sc.click("css=#cancel");
        Thread.sleep(500);
        Assert.assertFalse(sc.isVisible("css=#duracloudCredentialsForm"));

    }
    
    
    @Test
    public void testEditSaveDuracloudConfig() throws Exception{
        Properties properties = getProperties();
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        String host = properties.getProperty("host");
        String port = properties.getProperty("port");
        String spaceId = properties.getProperty("spaceId");

        openConfigurationPage();
        Assert.assertTrue(isElementPresent("css=#edit"));
        sc.click("css=#edit");
        Thread.sleep(3000);
        Assert.assertTrue(sc.isVisible("css=#duracloudCredentialsForm"));

        sc.type("css=#username", username);
        sc.type("css=#password", password);
        sc.type("css=#host", host);
        sc.type("css=#port", port);
        sc.click("css=#next");

        int second = 0;
        while(true){
            if(sc.isElementPresent("css=#spaceForm") || second > 20){
                break;
            }

            Thread.sleep(1000);
            second++;

        }

        Assert.assertTrue(sc.isElementPresent("css=#spaceForm"));
        //ensure that system spaces are not showing up.
        Assert.assertFalse(sc.isElementPresent("css=#spaceId option[value='x-duracloud-admin']"));

        sc.select("css=#spaceId", "value=" + spaceId);
        sc.click("css=#next");
        Thread.sleep(5000);
        String endButton = "css=#end";
        Assert.assertTrue(sc.isVisible(endButton));
       
        clickAndWait("css=#end");
        Assert.assertFalse(sc.isVisible("css=#edit-dialog"));

    }

    protected void openConfigurationPage() {
        sc.open(getAppRoot()+"/configuration");
    }
}
