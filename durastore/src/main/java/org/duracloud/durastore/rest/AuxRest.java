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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.duracloud.common.data.StringDataStore;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.s3storageprovider.dto.StoreSignedCookieTaskParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private RestUtil restUtil;

    @Autowired
    public AuxRest(RestUtil restUtil) {
        this.restUtil = restUtil;
    }

    /**
     * Provides a listing of all available storage provider accounts
     *
     * @return 200 response with XML file listing stores
     */
    @Path("/cookies")
    @GET
    public Response getCookies(@QueryParam("token") String token) {
        try {
            StringDataStore dataStore = StringDataStore.getInstance();
            String cookiesData = dataStore.retrieveData(token);

            if (null == cookiesData) {
                return responseNotFound("Token not found");
            }

            StoreSignedCookieTaskParameters cookieTaskParams =
                StoreSignedCookieTaskParameters.deserialize(cookiesData);

            String streamingHost = cookieTaskParams.getStreamingHost();

            // Build set of cookies
            Map<String, String> cookies = cookieTaskParams.getSignedCookies();
            ArrayList<NewCookie> responseCookies = new ArrayList<>();
            for (String cookieKey : cookies.keySet()) {
                responseCookies.add(new NewCookie(cookieKey,
                                                  cookies.get(cookieKey),
                                                  "/",
                                                  streamingHost,
                                                  "Supports HLS",
                                                  -1,
                                                  true));
            }

            // Build redirect HTML
            String redirectUrl = cookieTaskParams.getRedirectUrl();
            String html = "<html><head><meta http-equiv='refresh' content='0;URL=\"" +
                          redirectUrl + "\"' /></head></html>";

            return Response.ok(html, MediaType.TEXT_HTML_TYPE)
                           .cookie(responseCookies.toArray(new NewCookie[responseCookies.size()])).build();

        } catch (Exception e) {
            return responseBad(e);
        }
    }

}
