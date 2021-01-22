/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.duracloud.common.rest.HttpHeaders;
import org.duracloud.s3storage.StringDataStore;
import org.duracloud.s3storage.StringDataStoreFactory;
import org.duracloud.s3storageprovider.dto.SignedCookieData;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Bernstein
 */
@RunWith(EasyMockRunner.class)
public class AuxRestTest extends EasyMockSupport {
    @Mock
    private StorageAccountManager accountManager;

    @Mock
    private StorageAccount account;

    @Mock
    private StringDataStoreFactory stringDataStoreFactory;

    @Mock
    private HttpServletRequest request;

    @Mock
    private StringDataStore stringDataStore;

    private String token = "token-uuid";

    private AuxRest auxRest;

    @Before
    public void setup() {
        expect(stringDataStoreFactory.create(isA(String.class))).andReturn(
            stringDataStore);
        this.auxRest = new AuxRest();
        this.auxRest.setStringDataStoreFactory(stringDataStoreFactory);
        this.auxRest.request = request;
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testGetCookies() {
        SignedCookieData signedCookieData = new SignedCookieData();
        String myUrl = "myUrl";
        signedCookieData.setRedirectUrl(myUrl);
        Map<String, String> cookies = new HashMap<>();
        cookies.put("cookieName1", "cookieValue1");
        cookies.put("cookieName2", "cookieValue2");
        signedCookieData.setSignedCookies(cookies);
        String domain = "domain";
        signedCookieData.setStreamingHost(domain);

        String data = signedCookieData.serialize();
        expect(stringDataStore.retrieveData(token)).andReturn(data);
        expect(request.getHeader(HttpHeaders.ORIGIN)).andReturn("https://www.example.com");

        replayAll();

        Response response = this.auxRest.getCookies(token);
        assertEquals("200 response expected", 200, response.getStatus());
        assertEquals("2 cookies expected", 2, response.getCookies().size());
        Map<String, NewCookie> newCookies = response.getCookies();
        NewCookie cookie = newCookies.get("cookieName1");
        assertEquals("cookieName1", cookie.getName());
        assertEquals("cookieValue1", cookie.getValue());
        assertEquals(true, cookie.isSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(domain, cookie.getDomain());
        assertEquals("text/html", response.getHeaderString("Content-Type"));
        String html = response.getEntity().toString();
        assertTrue("response body must contain redirect url", html.contains("URL=\"" + myUrl + "\""));
        assertTrue("response must include refresh meta tag", html.contains("meta http-equiv='refresh'"));
        assertEquals(1, response.getHeaders().get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN).size());
        assertEquals("https://www.example.com",
                     response.getHeaders().get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN).get(0));
        assertEquals("true",
                     response.getHeaders().get(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS).get(0));
    }

}
