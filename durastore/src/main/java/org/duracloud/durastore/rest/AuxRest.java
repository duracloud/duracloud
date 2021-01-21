/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import java.util.ArrayList;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.rest.HttpHeaders;
import org.duracloud.s3storage.StringDataStore;
import org.duracloud.s3storage.StringDataStoreFactory;
import org.duracloud.s3storageprovider.dto.SignedCookieData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Provides auxilliary functions via REST
 *
 * @author Bill Branan
 */
@Path("/aux")
@Component
public class AuxRest extends BaseRest {
    private final Logger log = LoggerFactory.getLogger(AuxRest.class);

    @Inject
    private StringDataStoreFactory stringDataStoreFactory;

    @VisibleForTesting
    protected void setStringDataStoreFactory(StringDataStoreFactory stringDataStoreFactory) {
        this.stringDataStoreFactory = stringDataStoreFactory;
    }

    /**
     * Adds new cookies to the response headers based on the provided token.  The response body
     * contains an HTML based redirect pointing to the redirect URL passed to the /durastore/task/get-signed-cookies
     * (GetHlsSingedCookiesUrlTaskRunner) endpoint.
     *
     * @return 200 response with XML file listing stores
     */
    @Path("/cookies")
    @GET
    public Response getCookies(@QueryParam("token") String token) {
        try {
            StringDataStore dataStore = this.stringDataStoreFactory.create(Constants.HIDDEN_COOKIE_SPACE);
            String cookiesData = dataStore.retrieveData(token);

            if (null == cookiesData) {
                return responseNotFound("Token not found");
            }

            SignedCookieData cookieData = SignedCookieData.deserialize(cookiesData);
            String streamingHost = cookieData.getStreamingHost();

            // Build set of cookies
            Map<String, String> cookies = cookieData.getSignedCookies();
            ArrayList<NewCookie> responseCookies = new ArrayList<>();
            for (String cookieKey : cookies.keySet()) {
                responseCookies.add(new NewCookie(cookieKey,
                                                  cookies.get(cookieKey),
                                                  "/;SameSite=None",
                                                  streamingHost,
                                                  "Supports HLS",
                                                  -1,
                                                  true));
            }

            // Build redirect HTML
            String redirectUrl = cookieData.getRedirectUrl();
            String html = "<html><head><meta http-equiv='refresh' content='0;URL=\"" +
                          redirectUrl + "\"' /></head></html>";

            // Capture the originating host from the request, pass it back in
            // the CORS header in the response. Defaults to "*".
            String origin = "*";
            String requestingOrigin = request.getHeader(HttpHeaders.ORIGIN);
            if (!StringUtils.isEmpty(requestingOrigin)) {
                origin = requestingOrigin;
            }

            return Response.ok(html, MediaType.TEXT_HTML_TYPE)
                           .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin)
                           .header(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                           .header(HttpHeaders.VARY, HttpHeaders.ORIGIN)
                           .cookie(responseCookies.toArray(new NewCookie[responseCookies.size()])).build();
        } catch (Exception e) {
            return responseBad(e);
        }
    }
}
