/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.web;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Provides helper methods for REST tests
 *
 * @author Bill Branan
 */
public class RestHttpHelper {

    protected final Logger log = LoggerFactory.getLogger(RestHttpHelper.class);

    private static final String XML_MIMETYPE = "text/xml";

    private Map<String, String> authHeaders = new HashMap<String, String>();

    public RestHttpHelper() {
        this(null);
    }

    public RestHttpHelper(Credential credential) {
        if (credential != null) {
            UsernamePasswordCredentials authCred = new UsernamePasswordCredentials(
                credential.getUsername(),
                credential.getPassword());
            String authHeaderVal = BasicScheme.authenticate(authCred, "utf-8");
            String authHeaderName = "Authorization";
            authHeaders.put(authHeaderName, authHeaderVal);
        }
    }

    private enum Method {
        GET() {

            @Override
            public HttpMethod getMethod(String url, RequestEntity re) {
                return new GetMethod(url);
            }
        },
        POST() {

            @Override
            public HttpMethod getMethod(String url, RequestEntity re) {
                EntityEnclosingMethod postMethod = new PostMethod(url);
                if (re != null) {
                    String length = String.valueOf(re.getContentLength());
                    postMethod.setRequestHeader("Content-Length", length);
                    postMethod.setRequestEntity(re);
                }
                return postMethod;
            }
        },
        PUT() {

            @Override
            public HttpMethod getMethod(String url, RequestEntity re) {
                EntityEnclosingMethod putMethod = new PutMethod(url);
                if (re != null) {
                    putMethod.setRequestEntity(re);
                }
                return putMethod;
            }
        },
        MULTPART() {

            @Override
            public HttpMethod getMethod(String url, RequestEntity re) {
                EntityEnclosingMethod postMethod = new PostMethod(url);
                if (re != null) {
                    String length = String.valueOf(re.getContentLength());
                    postMethod.setRequestHeader("Content-Length", length);
                    postMethod.setRequestEntity(re);
                }
                return postMethod;
            }
        },
        HEAD() {

            @Override
            public HttpMethod getMethod(String url, RequestEntity re) {
                return new HeadMethod(url);
            }
        },
        DELETE() {

            @Override
            public HttpMethod getMethod(String url, RequestEntity re) {
                return new DeleteMethod(url);
            }
        };

        abstract public HttpMethod getMethod(String url, RequestEntity re);
    };

    public HttpResponse get(String url) throws Exception {
        return executeRequest(url, Method.GET, null, null);
    }

    public HttpResponse head(String url) throws Exception {
        return executeRequest(url, Method.HEAD, null, null);
    }

    public HttpResponse delete(String url) throws Exception {
        return executeRequest(url, Method.DELETE, null, null);
    }

    public HttpResponse post(String url,
                             String requestContent,
                             Map<String, String> headers) throws Exception {
        String mimeType = null;
        RequestEntity requestEntity =
                buildInputStreamRequestEntity(requestContent, mimeType);
        return executeRequest(url, Method.POST, requestEntity, headers);
    }

    public HttpResponse post(String url,
                             String requestContent,
                             String mimeType,
                             Map<String, String> headers) throws Exception {
        RequestEntity requestEntity =
                buildInputStreamRequestEntity(requestContent, mimeType);
        return executeRequest(url, Method.POST, requestEntity, headers);
    }

    public HttpResponse post(String url,
                             InputStream requestContent,
                             String contentSize,
                             String mimeType,
                             Map<String, String> headers) throws Exception {
        long contentLength = Long.parseLong(contentSize);
        RequestEntity requestEntity =
                buildInputStreamRequestEntity(requestContent,
                                              contentLength,
                                              mimeType);
        return executeRequest(url, Method.POST, requestEntity, headers);
    }

    public HttpResponse put(String url,
                            String requestContent,
                            Map<String, String> headers) throws Exception {
        String mimeType = null;
        RequestEntity requestEntity =
                buildInputStreamRequestEntity(requestContent, mimeType);
        return executeRequest(url, Method.PUT, requestEntity, headers);
    }

    public HttpResponse put(String url,
                            String requestContent,
                            String mimeType,
                            Map<String, String> headers) throws Exception {
        RequestEntity requestEntity =
                buildInputStreamRequestEntity(requestContent, mimeType);
        return executeRequest(url, Method.PUT, requestEntity, headers);
    }

    public HttpResponse put(String url,
                            InputStream requestContent,
                            String contentSize,
                            String mimeType,
                            Map<String, String> headers) throws Exception {
        long contentLength = Long.parseLong(contentSize);
        RequestEntity requestEntity =
                buildInputStreamRequestEntity(requestContent,
                                              contentLength,
                                              mimeType);
        return executeRequest(url, Method.PUT, requestEntity, headers);
    }

    public HttpResponse multipartFilePost(String url, File file)
            throws Exception {
        Part[] parts = {new FilePart(file.getName(), file)};
        return multipartPost(url, parts);
    }

    public HttpResponse multipartFileStreamPost(String url,
                                                String fileName,
                                                InputStream stream,
                                                long length)
            throws Exception {
        Part[] parts = {new FilePart(fileName,
                        new StreamPart(fileName, stream, length))};
        return multipartPost(url, parts);
    }

    private class StreamPart implements PartSource {

        private String fileName;
        private InputStream stream;
        private long length;

        public StreamPart(String fileName, InputStream stream, long length) {
            this.fileName = fileName;
            this.stream = stream;
            this.length = length;
        }

        public InputStream createInputStream() throws IOException {
            return stream;
        }

        public String getFileName() {
            return fileName;
        }

        public long getLength() {          
            return length;
        }
    }

    public HttpResponse multipartPost(String url, Part[] parts)
            throws Exception {
        Map<String, String> headers = null;
        RequestEntity re = buildMultipartRequestEntity(url, parts);
        return executeRequest(url, Method.MULTPART, re, headers);
    }

    private InputStreamRequestEntity buildInputStreamRequestEntity(String requestContent,
                                                                   String argMimeType)
            throws Exception {
        if (requestContent == null) {
            return null;
        }
        InputStream streamContent = IOUtil.writeStringToStream(requestContent);
        long contentLength = requestContent.length();
        return buildInputStreamRequestEntity(streamContent,
                                             contentLength,
                                             argMimeType);

    }

    private InputStreamRequestEntity buildInputStreamRequestEntity(InputStream streamContent,
                                                                   long contentLength,
                                                                   String argMimeType)
            throws Exception {
        if (streamContent == null) {
            return null;
        }
        String mimeType = (argMimeType == null ? XML_MIMETYPE : argMimeType);
        return new InputStreamRequestEntity(streamContent,
                                            contentLength,
                                            mimeType);

    }

    private RequestEntity buildMultipartRequestEntity(String url, Part[] parts) {
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("URL must be a non-empty value");
        }
        if (parts == null || parts.length == 0) {
            return null;
        }

        PostMethod postMethod = new PostMethod(url);
        return new MultipartRequestEntity(parts, postMethod.getParams());
    }

    private HttpResponse executeRequest(String url,
                                        Method method,
                                        RequestEntity requestEntity,
                                        Map<String, String> headers)
            throws Exception {
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("URL must be a non-empty value");
        }

        HttpMethod httpMethod = method.getMethod(url, requestEntity);

        if (authHeaders != null && authHeaders.size() > 0) {
            addHeaders(httpMethod, authHeaders);
        }

        if (headers != null && headers.size() > 0) {
            addHeaders(httpMethod, headers);
        }

        if (log.isDebugEnabled()) {
            log.debug(loggingRequestText(url, method, requestEntity, headers));
        }

        HttpClient client = new HttpClient();
        client.executeMethod(httpMethod);
        HttpResponse response = new HttpResponse(httpMethod);

        if (log.isDebugEnabled()) {
            log.debug(loggingResponseText(response));
        }

        return response;
    }

    private void addHeaders(HttpMethod httpMethod, Map<String, String> headers) {
        Iterator<String> headerIt = headers.keySet().iterator();
        while (headerIt.hasNext()) {
            String headerName = headerIt.next();
            String headerValue = headers.get(headerName);
            if (headerName != null && headerValue != null) {
                httpMethod.addRequestHeader(headerName, headerValue);
            }
        }
    }

    public class HttpResponse {

        private final int statusCode;

        private final InputStream responseStream;

        private final Header[] responseHeaders;

        private final Header[] responseFooters;

        public HttpResponse(int statusCode,
                     Header[] responseHeaders,
                     Header[] responseFooters,
                     InputStream responseStream) {
            this.statusCode = statusCode;
            this.responseHeaders = responseHeaders;
            this.responseFooters = responseFooters;
            this.responseStream = responseStream;
        }

        public HttpResponse(HttpMethod method)
                throws IOException {
            this(method.getStatusCode(),
                 method.getResponseHeaders(),
                 method.getResponseFooters(),
                 method.getResponseBodyAsStream());
        }

        public int getStatusCode() {
            return statusCode;
        }

        public InputStream getResponseStream() {
            return responseStream;
        }

        public String getResponseBody() throws IOException {
            if (responseStream != null) {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(responseStream));
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                responseStream.close();
                return builder.toString();
            } else {
                // No response body will be available for HEAD requests
                return null;
            }
        }

        public Header[] getResponseHeaders() {
            return responseHeaders;
        }

        public Header[] getResponseFooters() {
            return responseFooters;
        }

        public Header getResponseHeader(String headerName) {
            for (Header header : responseHeaders) {
                if (header.getName().equalsIgnoreCase(headerName)) {
                    return header;
                }
            }
            return null;
        }
    }

    private String loggingRequestText(String url,
                                      Method method,
                                      RequestEntity requestEntity,
                                      Map<String, String> headers) {
        StringBuilder sb = new StringBuilder("URL: '" + url + "'\n");
        if (method != null) {
            sb.append("METHOD: '" + method.name() + "'\n");
        }
        if (requestEntity != null) {
            sb.append("CONTENT-TYPE: '" + requestEntity.getContentType() + "'\n");
        }
        if (headers != null && headers.size() > 0) {
            sb.append("HEADERS: \n");
            for (String key : headers.keySet()) {
                sb.append("  [" + key + "|" + headers.get(key) + "]\n");
            }
        }
        return sb.toString();
    }

    private String loggingResponseText(HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("RESPONSE CODE: '" + response.getStatusCode() + "'\n");
        Header[] headers = response.getResponseHeaders();
        if (headers != null && headers.length > 0) {
            sb.append("RESPONSE HEADERS: \n");
            for (Header header : headers) {
                sb.append("  [" + header.getName() + "|" + header.getValue() + "]\n");
            }
        }
        Header[] footers = response.getResponseFooters();
        if (footers != null && footers.length > 0) {
            sb.append("RESPONSE FOOTERS: \n");
            for (Header footer : footers) {
                sb.append("  [" + footer.getName() + "|" + footer.getValue() + "]\n");
            }
        }
        return sb.toString();
    }
}
