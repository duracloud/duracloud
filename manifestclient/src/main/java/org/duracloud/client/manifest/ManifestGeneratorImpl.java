/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.manifest;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.model.Credential;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.manifest.ManifestGenerator;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestEmptyException;
import org.duracloud.manifest.error.ManifestGeneratorException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Allows for communication with Duraboss manifest
 *
 * @author: Bill Branan
 * Date: 4/5/12
 */
public class ManifestGeneratorImpl implements ManifestGenerator {

    private static final String DEFAULT_CONTEXT = "duraboss";
    private String baseURL = null;
    private RestHttpHelper restHelper;

    public ManifestGeneratorImpl(String host,
                                 String port,
                                 Credential credential) {
        this(host, port, DEFAULT_CONTEXT, new RestHttpHelper(credential));
    }

    public ManifestGeneratorImpl(String host,
                                 String port,
                                 RestHttpHelper restHelper) {
        this(host, port, DEFAULT_CONTEXT, restHelper);
    }

    public ManifestGeneratorImpl(String host,
                                 String port,
                                 String context,
                                 Credential credential) {
        this(host, port, context, new RestHttpHelper(credential));
    }

    public ManifestGeneratorImpl(String host,
                                 String port,
                                 String context,
                                 RestHttpHelper restHelper) {
        if ((host == null) || (host.equals(""))) {
            throw new IllegalArgumentException("Host must be a valid " +
                                                "server host name");
        }

        if (context == null) {
            context = DEFAULT_CONTEXT;
        }

        if ((port == null) || (port.equals(""))) {
            this.baseURL = ("http://" + host + "/" + context);
        } else if (port.equals("443")) {
            this.baseURL = ("https://" + host + "/" + context);
        } else {
            this.baseURL = ("http://" + host + ":" + port + "/" + context);
        }

        this.restHelper = restHelper;
    }

    private String buildURL(String relativeURL) {
        String manifest = "manifest";
        if (null == relativeURL) {
            return this.baseURL + "/" + manifest;
        }
        return this.baseURL + "/" + manifest + "/" + relativeURL;
    }

    private RestHttpHelper getRestHelper() {
        if (null == this.restHelper) {
            this.restHelper = new RestHttpHelper();
        }
        return this.restHelper;
    }

    @Override
    public InputStream getManifest(String storeId,
                                   String spaceId,
                                   FORMAT format,
                                   Date asOfDate)
        throws ManifestArgumentException, ManifestEmptyException {
        String url = buildURL(spaceId);

        RestHttpHelper.HttpResponse response = runGetManifest(url);
        checkResponse(response, HttpStatus.SC_OK);
        return response.getResponseStream();
    }

    private RestHttpHelper.HttpResponse runGetManifest(String url) {
        try {
            return getRestHelper().get(url);
        } catch (Exception e) {
            String error =
                "Could not retrieve manifest due to: " + e.getMessage();
            throw new ManifestGeneratorException(error, e);
        }
    }

    protected void checkResponse(RestHttpHelper.HttpResponse response,
                                  int expectedCode)
            throws ManifestArgumentException, ManifestEmptyException {
        if (response == null) {
            String err = "Could not complete request due to error: " +
                         "Response was null.";
            throw new ManifestGeneratorException(err);
        }
        int statusCode = response.getStatusCode();
        if (statusCode != expectedCode) {
            String errorMessage;
            try {
                errorMessage = response.getResponseBody();
            } catch(IOException e) {
                errorMessage = "";
            }

            if(statusCode == HttpStatus.SC_BAD_REQUEST) {
                throw new ManifestArgumentException(errorMessage);
            } else if(statusCode == HttpStatus.SC_NOT_FOUND) {
                throw new ManifestEmptyException(errorMessage);
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("Could not complete request due to error: ");
                builder.append("Response code was ");
                builder.append(statusCode);
                builder.append(", expected value was ");
                builder.append(expectedCode);
                builder.append(". Error message: ");
                builder.append(errorMessage);
                throw new ManifestGeneratorException(builder.toString());
            }
        }
    }

}