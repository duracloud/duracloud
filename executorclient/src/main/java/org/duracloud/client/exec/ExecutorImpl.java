/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.exec;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.exec.error.ExecutorException;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.exec.Executor;
import org.duracloud.exec.error.InvalidActionRequestException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * Allows for communication with Duraboss executor
 *
 * @author: Bill Branan
 * Date: 4/4/12
 */
public class ExecutorImpl implements Executor {

    private static final String DEFAULT_CONTEXT = "duraboss";
    private String baseURL = null;
    private RestHttpHelper restHelper;

    public ExecutorImpl(String host, String port, Credential credential) {
        this(host, port, DEFAULT_CONTEXT, new RestHttpHelper(credential));
    }

    public ExecutorImpl(String host, String port, RestHttpHelper restHelper) {
        this(host, port, DEFAULT_CONTEXT, restHelper);
    }

    public ExecutorImpl(String host,
                        String port,
                        String context,
                        Credential credential) {
        this(host, port, context, new RestHttpHelper(credential));
    }

    public ExecutorImpl(String host,
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
        String exec = "exec";
        if (null == relativeURL) {
            return this.baseURL + "/" + exec;
        }
        return this.baseURL + "/" + exec + "/" + relativeURL;
    }

    private RestHttpHelper getRestHelper() {
        if (null == this.restHelper) {
            this.restHelper = new RestHttpHelper();
        }
        return this.restHelper;
    }

    @Override
    public Set<String> getSupportedActions() {
        String url = buildURL("action");
        String err = "Could not get supported executor actions due to: ";
        return runGetSupportedActions(url, err);
    }

    private Set<String> runGetSupportedActions(String url, String err) {
        try {
            RestHttpHelper.HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);

            InputStream actionsStream = response.getResponseStream();
            String actions = IOUtil.readStringFromStream(actionsStream);
            return SerializationUtil.deserializeSet(actions);
        } catch (Exception e) {
            String error = err + e.getMessage();
            throw new ExecutorException(error, e);
        }
    }

    @Override
    public void performAction(String actionName, String actionParameters)
        throws InvalidActionRequestException {
        String url = buildURL(actionName);
        String err = "Could not perform action " + actionName + " due to: ";
        runPerformAction(url, actionParameters, err);
    }

    private void runPerformAction(String url,
                                  String actionParameters,
                                  String err) {
        try {
            RestHttpHelper.HttpResponse response =
                getRestHelper().post(url, actionParameters, null);
            checkResponse(response, HttpStatus.SC_OK);
        } catch (Exception e) {
            String error = err + e.getMessage();
            throw new ExecutorException(error, e);
        }
    }

    @Override
    public Map<String, String> getStatus() {
        String url = buildURL(null);
        String err = "Could not get executor status due to: ";
        return runGetStatus(url, err);
    }

    private Map<String, String> runGetStatus(String url, String err) {
        try {
            RestHttpHelper.HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);

            InputStream actionsStream = response.getResponseStream();
            String status = IOUtil.readStringFromStream(actionsStream);
            return SerializationUtil.deserializeMap(status);
        } catch (Exception e) {
            String error = err + e.getMessage();
            throw new ExecutorException(error, e);
        }
    }

    @Override
    public void stop() {
        String url = buildURL(null);
        String err = "Could not stop executor due to: ";
        runStop(url, err);
    }

    private void runStop(String url, String err) {
        try {
            RestHttpHelper.HttpResponse response = getRestHelper().delete(url);
            checkResponse(response, HttpStatus.SC_OK);
        } catch (Exception e) {
            String error = err + e.getMessage();
            throw new ExecutorException(error, e);
        }
    }

    protected void checkResponse(RestHttpHelper.HttpResponse response,
                                 int expectedCode)
        throws ExecutorException, InvalidActionRequestException {
        if (response == null) {
            throw new ExecutorException("Could not complete request due to " +
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

            if(statusCode == HttpStatus.SC_BAD_REQUEST) {
                throw new InvalidActionRequestException(errorMessage);
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("Could not complete request due to error: ");
                builder.append("Response code was ");
                builder.append(statusCode);
                builder.append(", expected value was ");
                builder.append(expectedCode);
                builder.append(". Error message: ");
                builder.append(errorMessage);
                throw new DuraCloudRuntimeException(builder.toString());
            }
        }
    }

}