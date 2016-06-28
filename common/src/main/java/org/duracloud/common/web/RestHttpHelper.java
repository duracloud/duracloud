/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides helper methods for REST tests
 *
 * @author Bill Branan
 */
public class RestHttpHelper {

    protected final Logger log = LoggerFactory.getLogger(RestHttpHelper.class);

    private CredentialsProvider credsProvider;

    public RestHttpHelper() {
        this(null);
    }

    public RestHttpHelper(Credential credential) {
        if (credential != null) {
            credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(credential.getUsername(),
                                                credential.getPassword()));
        }
    }

    private enum Method {
        GET() {

            @Override
            public HttpRequestBase getMethod(String url, HttpEntity entity) {
                return new HttpGet(url);
            }
        },
        POST() {

            @Override
            public HttpRequestBase getMethod(String url, HttpEntity entity) {
                HttpPost postMethod = new HttpPost(url);
                if (entity != null) {
                    postMethod.setEntity(entity);
                }
                return postMethod;
            }
        },
        PUT() {

            @Override
            public HttpRequestBase getMethod(String url, HttpEntity entity) {
                HttpPut putMethod = new HttpPut(url);
                if (entity != null) {
                    putMethod.setEntity(entity);
                }
                return putMethod;
            }
        },
        MULTPART() {

            @Override
            public HttpRequestBase getMethod(String url, HttpEntity entity) {
                HttpPost postMethod = new HttpPost(url);
                if (entity != null) {
                    postMethod.setEntity(entity);
                }
                return postMethod;
            }
        },
        HEAD() {

            @Override
            public HttpRequestBase getMethod(String url, HttpEntity entity) {
                return new HttpHead(url);
            }
        },
        DELETE() {

            @Override
            public HttpRequestBase getMethod(String url, HttpEntity entity) {
                return new HttpDelete(url);
            }
        };

        abstract public HttpRequestBase getMethod(String url, HttpEntity entity);
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
        HttpEntity requestEntity = buildInputStreamEntity(requestContent, mimeType);
        return executeRequest(url, Method.POST, requestEntity, headers);
    }

    public HttpResponse post(String url,
                             String requestContent,
                             String mimeType,
                             Map<String, String> headers) throws Exception {
        HttpEntity requestEntity = buildInputStreamEntity(requestContent, mimeType);
        return executeRequest(url, Method.POST, requestEntity, headers);
    }

    public HttpResponse post(String url,
                             InputStream requestContent,
                             String mimeType,
                             long contentLength,
                             Map<String, String> headers) throws Exception {
        HttpEntity requestEntity =
                buildInputStreamEntity(requestContent, mimeType, contentLength);
        return executeRequest(url, Method.POST, requestEntity, headers);
    }

    public HttpResponse put(String url,
                            String requestContent,
                            Map<String, String> headers) throws Exception {
        String mimeType = null;
        HttpEntity requestEntity = buildInputStreamEntity(requestContent, mimeType);
        return executeRequest(url, Method.PUT, requestEntity, headers);
    }

    public HttpResponse put(String url,
                            String requestContent,
                            String mimeType,
                            Map<String, String> headers) throws Exception {
        HttpEntity requestEntity = buildInputStreamEntity(requestContent, mimeType);
        return executeRequest(url, Method.PUT, requestEntity, headers);
    }

    public HttpResponse put(String url,
                            InputStream requestContent,
                            String mimeType,
                            long contentLength,
                            Map<String, String> headers) throws Exception {
        HttpEntity requestEntity =
                buildInputStreamEntity(requestContent, mimeType, contentLength);
        return executeRequest(url, Method.PUT, requestEntity, headers);
    }

    public HttpResponse multipartFilePost(String url, File file)
            throws Exception {
        ContentType contentType = ContentType.MULTIPART_FORM_DATA;
        HttpEntity reqEntity =
            MultipartEntityBuilder.create()
                .addBinaryBody(file.getName(), file, contentType, file.getName()).build();
        return multipartPost(url, reqEntity);
    }

    public HttpResponse multipartFileStreamPost(String url,
                                                String fileName,
                                                InputStream stream)
            throws Exception {
        ContentType contentType = ContentType.MULTIPART_FORM_DATA;
        HttpEntity reqEntity =
            MultipartEntityBuilder.create()
                .addBinaryBody(fileName, stream, contentType, fileName).build();
        return multipartPost(url, reqEntity);
    }

    public HttpResponse multipartPost(String url, HttpEntity reqEntity)
            throws Exception {
        Map<String, String> headers = null;
        return executeRequest(url, Method.MULTPART, reqEntity, headers);
    }

    private InputStreamEntity buildInputStreamEntity(String requestContent,
                                                     String mimeType)
            throws Exception {
        if (requestContent == null) {
            return null;
        }
        InputStream streamContent = IOUtil.writeStringToStream(requestContent);
        return buildInputStreamEntity(streamContent,
                                      mimeType,
                                      requestContent.getBytes(Charsets.UTF_8).length);
    }

    private InputStreamEntity buildInputStreamEntity(InputStream streamContent,
                                                     String mimeType,
                                                     long contentLength)
            throws Exception {
        if (streamContent == null) {
            return null;
        }

        ContentType contentType = buildContentType(mimeType);
        return new InputStreamEntity(streamContent, contentLength, contentType);
    }

    private ContentType buildContentType(String mimeType) {
        ContentType contentType;
        if(null == mimeType) {
            contentType = ContentType.TEXT_XML;
        } else {
            contentType = ContentType.create(mimeType, StandardCharsets.UTF_8);
        }
        return contentType;
    }

    private HttpResponse executeRequest(String url,
                                        Method method,
                                        HttpEntity requestEntity,
                                        Map<String, String> headers)
            throws IOException {
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("URL must be a non-empty value");
        }

        HttpRequestBase httpRequest = method.getMethod(url, requestEntity);

        if (headers != null && headers.size() > 0) {
            addHeaders(httpRequest, headers);
        }

        if (log.isDebugEnabled()) {
            log.debug(loggingRequestText(url, method, requestEntity, headers));
        }

        org.apache.http.HttpResponse response;
        if (null != credsProvider) {
            CloseableHttpClient httpClient =
                HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

            // Use preemptive basic auth
            URI requestUri = httpRequest.getURI();
            HttpHost target = new HttpHost(requestUri.getHost(),
                                           requestUri.getPort(),
                                           requestUri.getScheme());
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(target, basicAuth);
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            response = httpClient.execute(httpRequest, localContext);
        } else {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            response = httpClient.execute(httpRequest);
        }

        HttpResponse httpResponse = new HttpResponse(response);

        if (log.isDebugEnabled()) {
            log.debug(loggingResponseText(httpResponse));
        }

        return httpResponse;
    }

    private void addHeaders(HttpRequestBase httpRequest, Map<String, String> headers) {
        Iterator<String> headerIt = headers.keySet().iterator();
        while (headerIt.hasNext()) {
            String headerName = headerIt.next();
            String headerValue = headers.get(headerName);
            if (headerName != null && headerValue != null) {
                httpRequest.addHeader(headerName, headerValue);
            }
        }
    }

    public static class HttpResponse {

        protected final org.apache.http.HttpResponse response;

        public HttpResponse(org.apache.http.HttpResponse response) {
            this.response = response;
        }

        public int getStatusCode() {
            return response.getStatusLine().getStatusCode();
        }

        public InputStream getResponseStream() throws IOException {
            return response.getEntity().getContent();
        }

        public String getResponseBody() throws IOException {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity, "UTF-8");
            } else {
                // No response body will be available for HEAD requests
                return null;
            }
        }

        public Header[] getResponseHeaders() {
            return response.getAllHeaders();
        }

        public Header getResponseHeader(String headerName) {
            for (Header header : response.getAllHeaders()) {
                if (header.getName().equalsIgnoreCase(headerName)) {
                    return header;
                }
            }
            return null;
        }

        /**
         * Provided for testing of Http responses
         */
        public static HttpResponse buildMock(int statusCode,
                                             Header[] responseHeaders,
                                             InputStream responseStream) {
            ProtocolVersion pVersion = new ProtocolVersion("HTTP", 1, 1);
            StatusLine statusLine = new BasicStatusLine(pVersion, statusCode, "");

            org.apache.http.HttpResponse response =
                new DefaultHttpResponseFactory().newHttpResponse(statusLine, null);
            response.setHeaders(responseHeaders);
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(responseStream);
            response.setEntity(entity);

            return new HttpResponse(response);
        }
    }

    private String loggingRequestText(String url,
                                      Method method,
                                      HttpEntity requestEntity,
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
        return sb.toString();
    }
}
