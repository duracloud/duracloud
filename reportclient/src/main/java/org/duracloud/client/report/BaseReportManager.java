/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.report;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.client.report.error.UnexpectedResponseException;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.Securable;
import org.duracloud.common.web.RestHttpHelper;

import java.io.IOException;

/**
 * @author: Bill Branan
 * Date: 6/30/11
 */
public abstract class BaseReportManager implements Securable {

    protected RestHttpHelper restHelper;

    private static final String DEFAULT_CONTEXT = "duraboss";
    private String baseURL = null;

    public BaseReportManager(String host, String port) {
        this(host, port, DEFAULT_CONTEXT);
    }

    public BaseReportManager(String host, String port, String context) {
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
    }

    public String getBaseURL() {
        return this.baseURL;
    }

    @Override
    public void login(Credential credential) {
        setRestHelper(new RestHttpHelper(credential));
    }

    @Override
    public void logout() {
        setRestHelper(new RestHttpHelper());
    }

    protected RestHttpHelper getRestHelper() {
        if (null == this.restHelper) {
            this.restHelper = new RestHttpHelper();
        }
        return this.restHelper;
    }

    protected void setRestHelper(RestHttpHelper restHelper) {
        this.restHelper = restHelper;
    }

    protected void checkResponse(RestHttpHelper.HttpResponse response,
                                 int expectedCode)
            throws NotFoundException, ReportException {
        if (response == null) {
            throw new ReportException("Could not complete request due to " +
                                      "error: Response was null.");
        }
        int statusCode = response.getStatusCode();
        if (statusCode != expectedCode) {
            String errorMessage;
            try {
                errorMessage = response.getResponseBody();
            } catch(IOException e) {
                errorMessage = "";
            }

            if(statusCode == HttpStatus.SC_NOT_FOUND) {
                throw new NotFoundException(errorMessage);
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("Could not complete request due to error: ");
                builder.append("Response code was ");
                builder.append(statusCode);
                builder.append(", expected value was ");
                builder.append(expectedCode);
                builder.append(". Error message: ");
                builder.append(errorMessage);
                throw new UnexpectedResponseException(builder.toString(),
                                                      statusCode,
                                                      expectedCode,
                                                      errorMessage);
            }
        }
    }

}
