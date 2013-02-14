/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.selenium;

import java.util.List;
import java.util.Properties;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 *
 */
public abstract class BaseSeleniumTest {

    // initialize sync with test config
    protected Properties props = null;
    
    protected static Logger log =
        LoggerFactory.getLogger(BaseSeleniumTest.class);

    protected Selenium sc;

    public static String DEFAULT_PAGE_LOAD_WAIT_IN_MS = "60000";

    private static String DEFAULT_PORT = "8888";

    protected String getAppRoot() {
        return "/sync";
    }

    private String getPort() throws Exception {
        String port = System.getProperty("jetty.port");
        return port != null ? port : DEFAULT_PORT;
    }



    protected String getBaseUrl() throws Exception {
        return "http://localhost:" + getPort() + getAppRoot();
    }
    @Before
    public void before() throws Exception {
        String url = getBaseUrl() + "/";
        sc = createSeleniumClient(url);
        sc.start();
        log.info("started selenium client on " + url);
        props = getProperties();
    }
    
    protected static Properties getProperties() throws Exception{
        Properties p = new Properties();
        p.load(BaseSeleniumTest.class.getClassLoader()
                   .getResourceAsStream("test-init.properties"));

        return p;
    }


    @BeforeClass
    public static void beforeClass() throws Exception {

        // This is a temporary solution to making this code work with the
        // current DuraStore under Java 7.  The base issue is discussed in
        // https://jira.duraspace.org/browse/DURACLOUD-717   DWD
        System.setProperty("jsse.enableSNIExtension", "false");

       Properties properties = getProperties();
       String username = properties.getProperty("username");
       String password = properties.getProperty("password");
       String host = properties.getProperty("host");
       String port = properties.getProperty("port");
       String spaceId = properties.getProperty("spaceId");
       
       ContentStoreManager csm = new ContentStoreManagerImpl(host, port);
       
       csm.login(new Credential(username, password));
       
       ContentStore cs = csm.getPrimaryContentStore();
       List<String> spaces = cs.getSpaces();
       
       if(!spaces.contains(spaceId)){
           cs.createSpace(spaceId);
       }
    }


    @After
    public void after() {
        sc.stop();
        sc = null;
        log.info("stopped selenium client");
    }

    protected boolean isTextPresent(String pattern) {
        return sc.isTextPresent(pattern);
    }

    protected boolean isElementPresent(String locator) {
        return sc.isElementPresent(locator);
    }

    protected Selenium createSeleniumClient(String url) {

        return new DefaultSelenium("localhost", 4444, "*firefox", url);
    }


    /**
     * @param sc
     */
    protected void waitForPage() {
        log.debug("waiting for page to load...");
        sc.waitForPageToLoad(DEFAULT_PAGE_LOAD_WAIT_IN_MS);
        log.debug("body=" + sc.getBodyText());
    }

    protected void clickAndWait(String locator) {
        sc.click(locator);
        log.debug("clicked " + locator);
        waitForPage(sc);
    }

    

    /**
     * @param sc
     */
    public static void waitForPage(Selenium sc) {
        log.debug("waiting for page to load...");
        sc.waitForPageToLoad(DEFAULT_PAGE_LOAD_WAIT_IN_MS);
        log.debug("body=" + sc.getBodyText());
    }

    public static void clickAndWait(Selenium sc, String locator) {
        sc.click(locator);
        log.debug("clicked " + locator);
        waitForPage(sc);
    }

}
