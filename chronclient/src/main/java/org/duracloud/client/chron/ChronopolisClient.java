/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.chron;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.chron.error.ChronopolisException;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.web.RestHttpHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.client.chron.ResponseKey.HTTP_STATUS;
import static org.duracloud.client.chron.ResponseKey.RETRY_AFTER;
import static org.duracloud.common.web.RestHttpHelper.HttpResponse;

/**
 * This class provides a Java client for the Chronopolis backup/restore RESTful
 * service.
 *
 * @author Andrew Woods
 *         Date: 10/25/12
 */
public class ChronopolisClient {

    private static final String DEFAULT_CONTEXT = "chron-notify";
    private static final String TEXT_PLAIN = "text/plain;charset=UTF-8";

    private String baseURL = null;
    private String acctId;
    private RestHttpHelper restHelper;

    public ChronopolisClient(String acctId,
                             String host,
                             String port,
                             Credential credential) {
        this(acctId,
             host,
             port,
             DEFAULT_CONTEXT,
             new RestHttpHelper(credential));
    }

    public ChronopolisClient(String acctId,
                             String host,
                             String port,
                             RestHttpHelper restHelper) {
        this(acctId, host, port, DEFAULT_CONTEXT, restHelper);
    }

    public ChronopolisClient(String acctId,
                             String host,
                             String port,
                             String context,
                             Credential credential) {
        this(acctId, host, port, context, new RestHttpHelper(credential));
    }

    public ChronopolisClient(String acctId,
                             String host,
                             String port,
                             String context,
                             RestHttpHelper restHelper) {
        if ((acctId == null) || (acctId.isEmpty())) {
            throw new IllegalArgumentException("AccountId must be a non empty");
        }
        this.acctId = acctId;

        if ((host == null) || (host.isEmpty())) {
            throw new IllegalArgumentException(
                "Host must be a valid server host name");
        }

        if (context == null) {
            context = DEFAULT_CONTEXT;
        }

        if ((port == null) || (port.isEmpty())) {
            this.baseURL = ("http://" + host + "/" + context);
        } else if (port.equals("443")) {
            this.baseURL = ("https://" + host + "/" + context);
        } else {
            this.baseURL = ("http://" + host + ":" + port + "/" + context);
        }

        this.restHelper = restHelper;
    }

    private String buildURL(String path) {
        return this.baseURL + "/resources/" + path;
    }

    /**
     * This method stores the contents of a given space in SDSC to Chronopolis.
     * Since the store process is long-running, this call initiates the process
     * asynchronously.
     * Monitoring the process status is achieved through the
     * 'getProcessingStatus()' method.
     *
     * @param spaceId  of space to store
     * @param manifest of contents to store in BagIt format
     * @return Response properties
     */
    public Map<String, String> putContentSpace(String spaceId,
                                               InputStream manifest) {
        String url = buildURL("notify/" + acctId + "/" + spaceId);
        String err = "Could not put content space due to: ";
        return doPutContentSpace(url, err, manifest);
    }

    private Map<String, String> doPutContentSpace(String url,
                                                  String err,
                                                  InputStream manifest) {
        File tmpFile = cacheAsTmpFile(manifest);
        InputStream tmpStream = openInputStream(tmpFile);
        String size = getSize(tmpFile);
        String md5 = getMd5(tmpFile);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(RequestKey.CONTENT_MD5.toString(), md5);

        HttpResponse response = null;
        try {
            response = restHelper.put(url,
                                      tmpStream,
                                      size,
                                      TEXT_PLAIN,
                                      headers);
            checkResponse(response, HttpStatus.SC_ACCEPTED);

        } catch (Exception e) {
            throw new ChronopolisException(err + e.getMessage(), e);
        } finally {
            tmpFile.delete();
        }

        return responseMap(response);
    }

    private File cacheAsTmpFile(InputStream inputStream) {
        File tmp = null;
        OutputStream outputStream = null;
        try {
            tmp = File.createTempFile("chron-client", null);
            outputStream = new FileOutputStream(tmp);
            IOUtils.copy(inputStream, outputStream);

        } catch (IOException e) {
            throw new DuraCloudRuntimeException("Error creating tmp file!", e);

        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        return tmp;
    }

    private InputStream openInputStream(File file) {
        try {
            return new AutoCloseInputStream(FileUtils.openInputStream(file));

        } catch (IOException e) {
            throw new DuraCloudRuntimeException(
                "Unable to open file: " + file.getAbsolutePath(), e);
        }
    }

    private String getMd5(File file) {
        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        try {
            return util.generateChecksum(file);

        } catch (IOException e) {
            throw new ChronopolisException(
                "Error generating MD5 for: " + file.getAbsolutePath(), e);
        }
    }

    private String getSize(File file) {
        return Long.toString(file.length());
    }

    private Map<String, String> responseMap(HttpResponse response) {
        InputStream body = response.getResponseStream();
        if (null == body) {
            throw new ChronopolisException("Response body is null");
        }

        ObjectMapper mapper = new ObjectMapper();
        TypeReference typeRef = new TypeReference<Map<String, String>>() {
        };

        Map<String, String> map;
        try {
            map = mapper.readValue(body, typeRef);

        } catch (IOException e) {
            throw new ChronopolisException("Error deserializing response", e);
        }

        // Add http status.
        String status = Integer.toString(response.getStatusCode());
        map.put(HTTP_STATUS.toString(), status);

        // Add retry-after
        Header header = response.getResponseHeader(RETRY_AFTER.toString());
        if (null != header) {
            map.put(RETRY_AFTER.toString(), header.getValue());
        }

        return map;
    }

    /**
     * This method retrieves the status of previously requested
     * put-content-space / get-content-item / get-content-space request.
     * The polling frequency of this call is dictated by the response property:
     * ResponseKey.RETRY_AFTER (given in seconds)
     *
     * @param identifier of previous request
     * @return Response properties
     */
    public Map<String, String> getProcessingStatus(String identifier) {
        String url = buildURL("status/" + identifier);
        String err = "Could not get processing status due to: ";

        HttpResponse response = doHttpGet(url,
                                          err,
                                          HttpStatus.SC_OK,
                                          HttpStatus.SC_CREATED);
        return responseMap(response);
    }

    /**
     * This method retrieves the processing receipt.
     * The returned manifest contains the content items that have been restored.
     *
     * @param identifier of initial request
     * @return InputStream of requested manifest
     */
    public InputStream getReceiptManifest(String identifier) {
        String url = buildURL("status/" + identifier + "/receipt");
        String err = "Could not get receipt manifest due to: ";

        HttpResponse response = doHttpGet(url, err, HttpStatus.SC_OK);
        return response.getResponseStream();
    }

    /**
     * This method restores a single content item from a previously stored space
     *
     * @param spaceId   of content item to restore
     * @param contentId of content item to restore
     * @return Response properties
     */
    public Map<String, String> getContentItem(String spaceId,
                                              String contentId) {
        String url = buildURL(
            "notify/" + acctId + "/" + spaceId + "/" + contentId);
        String err = "Could not get content item due to: ";

        HttpResponse response = doHttpGet(url, err, HttpStatus.SC_ACCEPTED);
        return responseMap(response);
    }

    /**
     * This method restores a previously stored space.
     *
     * @param spaceId of space to restore
     * @return Response properties
     */
    public Map<String, String> getContentSpace(String spaceId) {
        String url = buildURL("notify/" + acctId + "/" + spaceId);
        String err = "Could not get content space due to: ";

        HttpResponse response = doHttpGet(url, err, HttpStatus.SC_ACCEPTED);
        return responseMap(response);
    }

    private HttpResponse doHttpGet(String url,
                                   String err,
                                   Integer... statusCodes) {
        HttpResponse response;
        try {
            response = restHelper.get(url);

        } catch (Exception e) {
            throw new ChronopolisException(err, e);
        }

        checkResponse(response, statusCodes);
        return response;
    }

    private void checkResponse(HttpResponse response,
                               Integer... expectedCodes) {
        if (response == null) {
            throw new ChronopolisException(
                "Could not complete request due to error: Response was null.");
        }

        int statusCode = response.getStatusCode();

        boolean success = false;
        for (int expectedCode : expectedCodes) {
            if (statusCode == expectedCode) {
                success = true;
            }
        }

        if (!success) {
            String errorMessage;
            try {
                errorMessage = response.getResponseBody();
            } catch (IOException e) {
                errorMessage = "";
            }

            StringBuilder builder = new StringBuilder();
            builder.append("Could not complete request due to error: ");
            builder.append("Response code was ");
            builder.append(statusCode);
            builder.append(", expected value was ");
            for (int code : expectedCodes) {
                builder.append(code);
                builder.append("|");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(". Error message: ");
            builder.append(errorMessage);
            throw new ChronopolisException(builder.toString());
        }
    }

}