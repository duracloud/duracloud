/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import static org.duracloud.client.HttpHeaders.CONTENT_ENCODING;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_SPACE_ACL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.constant.ManifestFormat;
import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.common.model.AclType;
import org.duracloud.common.retry.ExceptionHandler;
import org.duracloud.common.retry.Retriable;
import org.duracloud.common.retry.Retrier;
import org.duracloud.common.util.DateUtil.DateFormat;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.common.web.EncodeUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.domain.Content;
import org.duracloud.domain.Space;
import org.duracloud.error.ContentStateException;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.InvalidIdException;
import org.duracloud.error.NotFoundException;
import org.duracloud.error.NotImplementedException;
import org.duracloud.error.UnauthorizedException;
import org.duracloud.error.UnsupportedTaskException;
import org.duracloud.reportdata.bitintegrity.BitIntegrityReport;
import org.duracloud.reportdata.bitintegrity.BitIntegrityReportProperties;
import org.duracloud.reportdata.bitintegrity.BitIntegrityReportResult;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.ChecksumMismatchException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.IdUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to a content store
 *
 * @author Bill Branan
 */
public class ContentStoreImpl implements ContentStore {

    private String storeId = null;

    private StorageProviderType type = null;

    private boolean writable;

    private String baseURL = null;

    private RestHttpHelper restHelper;

    private static final String HEADER_PREFIX = Constants.HEADER_PREFIX;

    private int maxRetries = 3;

    private final Logger log =
        LoggerFactory.getLogger(ContentStoreImpl.class);

    private ExceptionHandler retryExceptionHandler;

    private String clientVersion;

    /**
     * Creates a ContentStore. This ContentStore uses the default number of
     * retries when a failure occurs (3).
     *
     * @param baseURL           a {@link java.lang.String} object.
     * @param type              a {@link org.duracloud.storage.domain.StorageProviderType} object.
     * @param storeId           a {@link java.lang.String} object.
     * @param writable          flag that indicates whether the content store is writable by non-root users (value
     *                          can be toggled in the Management Console)
     * @param restHelper        a {@link org.duracloud.common.web.RestHttpHelper} object
     */
    public ContentStoreImpl(String baseURL,
                            StorageProviderType type,
                            String storeId,
                            boolean writable,
                            RestHttpHelper restHelper) {
        this.baseURL = baseURL;
        this.type = type;
        this.storeId = storeId;
        this.restHelper = restHelper;
        this.writable = writable;

        this.retryExceptionHandler = new ExceptionHandler() {
            @Override
            public void handle(Exception ex) {
                if (!(ex instanceof NotFoundException)) {
                    log.warn(ex.getMessage());
                } else {
                    log.debug(ex.getMessage());
                }
            }
        };

        this.clientVersion =
            ResourceBundle.getBundle("storeclient").getString("version");

    }

    /**
     * Creates a ContentStore with a specific number of retries.
     */
    public ContentStoreImpl(String baseURL,
                            StorageProviderType type,
                            String storeId,
                            boolean writable,
                            RestHttpHelper restHelper,
                            int maxRetries) {
        this(baseURL, type, storeId, writable, restHelper);
        if (maxRetries >= 0) {
            this.maxRetries = maxRetries;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRetryExceptionHandler(ExceptionHandler retryExceptionHandler) {
        this.retryExceptionHandler = retryExceptionHandler;
    }

    public String getBaseURL() {
        return baseURL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStoreId() {
        return storeId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWritable() {
        return writable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStorageProviderType() {
        return type.name();
    }

    private String addStoreIdQueryParameter(String url) {
        return addStoreIdQueryParameter(url, storeId);
    }

    private String addStoreIdQueryParameter(String url, String storeId) {
        return addQueryParameter(url, "storeID", storeId);
    }

    private String buildURL(String relativeURL) {
        String url = baseURL + relativeURL;
        return url;
    }

    private String buildSpaceURL(String spaceId) {
        String url = buildURL("/" + spaceId);
        return addStoreIdQueryParameter(url);
    }

    private String buildContentURL(String spaceId, String contentId) {
        return buildContentURL(storeId, spaceId, contentId);
    }

    private String buildContentURL(String storeId, String spaceId, String contentId) {
        contentId = EncodeUtil.urlEncode(contentId);
        String url = buildURL("/" + spaceId + "/" + contentId);
        return addStoreIdQueryParameter(url, storeId);
    }

    private String buildAclURL(String spaceId) {
        String url = buildURL("/acl/" + spaceId);
        return addStoreIdQueryParameter(url);
    }

    private String buildTaskURL() {
        String url = buildURL("/task");
        return addStoreIdQueryParameter(url);
    }

    private String buildTaskURL(String taskName) {
        String url = buildURL("/task/" + taskName);
        return addStoreIdQueryParameter(url);
    }

    private String buildSpaceURL(String spaceId,
                                 String prefix,
                                 long maxResults,
                                 String marker) {
        String url = buildURL("/" + spaceId);
        url = addQueryParameter(url, "prefix", prefix);
        String max = null;
        if (maxResults > 0) {
            max = String.valueOf(maxResults);
        }
        url = addQueryParameter(url, "maxResults", max);
        url = addQueryParameter(url, "marker", marker);
        return addStoreIdQueryParameter(url);
    }

    private String addQueryParameter(String url, String name, String value) {
        if (value != null && !value.equals("")) {
            if (url.contains("?")) {
                url += "&";
            } else {
                url += "?";
            }

            url += (name + "=" + EncodeUtil.urlEncode(value));
        }
        return url;
    }

    protected <T extends Object> T execute(Retriable retriable)
        throws ContentStoreException {
        try {
            Retrier retrier = new Retrier(maxRetries);
            return retrier.execute(retriable, retryExceptionHandler);
        } catch (Exception e) {
            throw (ContentStoreException) e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSpaces() throws ContentStoreException {
        return execute(new Retriable() {
            @Override
            public List<String> retry() throws ContentStoreException {
                // The actual method being executed
                return doGetSpaces();
            }
        });
    }

    private List<String> doGetSpaces() throws ContentStoreException {
        String task = "get spaces";
        String url = buildURL("/spaces");
        url = addStoreIdQueryParameter(url);

        try {
            HttpResponse response = restHelper.get(url);
            checkResponse(response, HttpStatus.SC_OK);
            String responseText = response.getResponseBody();
            if (responseText != null) {
                List<String> spaceIds = new ArrayList<String>();
                InputStream is =
                    new ByteArrayInputStream(responseText.getBytes("UTF-8"));
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(is);
                Element spacesElem = doc.getRootElement();
                Iterator<?> spaceList = spacesElem.getChildren().iterator();
                while (spaceList.hasNext()) {
                    Element spaceElem = (Element) spaceList.next();
                    spaceIds.add((spaceElem.getAttributeValue("id")));
                }
                return spaceIds;
            } else {
                throw new ContentStoreException("Response body is empty");
            }
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, "listing", e);
        } catch (Exception e) {
            throw new ContentStoreException("Error attempting to get spaces " +
                                                "due to: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> getSpaceContents(String spaceId)
        throws ContentStoreException {
        return getSpaceContents(spaceId, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> getSpaceContents(final String spaceId,
                                             final String prefix)
        throws ContentStoreException {
        final ContentStore store = this;
        return execute(new Retriable() {
            @Override
            public Iterator<String> retry() throws ContentStoreException {
                // The actual method being executed
                return new ContentIterator(store, spaceId, prefix);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Space getSpace(final String spaceId,
                          final String prefix,
                          final long maxResults,
                          final String marker)
        throws ContentStoreException {
        return execute(new Retriable() {
            @Override
            public Space retry() throws ContentStoreException {
                // The actual method being executed
                return doGetSpace(spaceId, prefix, maxResults, marker);
            }
        });
    }

    private Space doGetSpace(String spaceId,
                             String prefix,
                             long maxResults,
                             String marker)
        throws ContentStoreException {
        String task = "get space";
        String url = buildSpaceURL(spaceId, prefix, maxResults, marker);
        try {
            HttpResponse response = restHelper.get(url);
            checkResponse(response, HttpStatus.SC_OK);
            Space space = new Space();
            space.setProperties(extractPropertiesFromHeaders(response));

            String responseText = response.getResponseBody();
            if (responseText != null) {
                InputStream is =
                    new ByteArrayInputStream(responseText.getBytes("UTF-8"));
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(is);
                Element spaceElem = doc.getRootElement();

                space.setId(spaceElem.getAttributeValue("id"));
                Iterator<?> spaceContents = spaceElem.getChildren().iterator();
                while (spaceContents.hasNext()) {
                    Element contentElem = (Element) spaceContents.next();
                    space.addContentId(contentElem.getText());
                }
            } else {
                throw new ContentStoreException("Response body is empty");
            }

            return space;
        } catch (NotFoundException e) {
            throw new NotFoundException(task, spaceId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createSpace(final String spaceId)
        throws ContentStoreException {
        execute(new Retriable() {
            @Override
            public Boolean retry() throws ContentStoreException {
                // The actual method being executed
                doCreateSpace(spaceId);
                return true;
            }
        });
    }

    private void doCreateSpace(String spaceId)
        throws ContentStoreException {
        validateSpaceId(spaceId);
        String task = "create space";
        String url = buildSpaceURL(spaceId);
        try {
            HttpResponse response = restHelper.put(url, null, null);
            checkResponse(response, HttpStatus.SC_CREATED);
        } catch (InvalidIdException e) {
            throw new InvalidIdException(task, spaceId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteSpace(final String spaceId) throws ContentStoreException {
        execute(new Retriable() {
            @Override
            public Boolean retry() throws ContentStoreException {
                // The actual method being executed
                doDeleteSpace(spaceId);
                return true;
            }
        });
    }

    private void doDeleteSpace(String spaceId) throws ContentStoreException {
        String task = "delete space";
        String url = buildSpaceURL(spaceId);
        try {
            HttpResponse response = restHelper.delete(url);
            checkResponse(response, HttpStatus.SC_OK);
        } catch (NotFoundException e) {
            throw new NotFoundException(task, spaceId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getSpaceProperties(final String spaceId)
        throws ContentStoreException {
        return execute(new Retriable() {
            @Override
            public Map<String, String> retry() throws ContentStoreException {
                // The actual method being executed
                return doGetSpaceProperties(spaceId);
            }
        });
    }

    private Map<String, String> doGetSpaceProperties(String spaceId)
        throws ContentStoreException {
        String task = "get space properties";
        String url = buildSpaceURL(spaceId);
        try {
            HttpResponse response = restHelper.head(url);
            checkResponse(response, HttpStatus.SC_OK);
            return extractPropertiesFromHeaders(response);
        } catch (NotFoundException e) {
            throw new NotFoundException(task, spaceId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, AclType> getSpaceACLs(final String spaceId)
        throws ContentStoreException {
        return execute(new Retriable() {
            @Override
            public Map<String, AclType> retry() throws ContentStoreException {
                // The actual method being executed
                return doGetSpaceACLs(spaceId);
            }
        });
    }

    private Map<String, AclType> doGetSpaceACLs(String spaceId)
        throws ContentStoreException {
        String task = "get space ACLs";
        String url = buildAclURL(spaceId);
        try {
            HttpResponse response = restHelper.head(url);
            checkResponse(response, HttpStatus.SC_OK);
            return doGetSpaceACLs(response);

        } catch (NotFoundException e) {
            throw new NotFoundException(task, spaceId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    private Map<String, AclType> doGetSpaceACLs(HttpResponse response) {
        Map<String, AclType> acls = new HashMap<String, AclType>();
        Map<String, String> aclProps =
            extractPropertiesFromHeaders(response, PROPERTIES_SPACE_ACL);
        for (String key : aclProps.keySet()) {
            String val = aclProps.get(key);
            acls.put(key, AclType.valueOf(val));
        }
        return acls;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpaceACLs(final String spaceId,
                             final Map<String, AclType> spaceACLs)
        throws ContentStoreException {
        execute(new Retriable() {
            @Override
            public Boolean retry() throws ContentStoreException {
                // The actual method being executed
                doSetSpaceACLs(spaceId, spaceACLs);
                return true;
            }
        });
    }

    private void doSetSpaceACLs(String spaceId, Map<String, AclType> spaceACLs)
        throws ContentStoreException {
        String task = "set space ACLs";
        String url = buildAclURL(spaceId);
        Map<String, String> headers = convertAclsToHeaders(spaceACLs);
        try {
            HttpResponse response = restHelper.post(url, null, headers);
            checkResponse(response, HttpStatus.SC_OK);

        } catch (NotFoundException e) {
            throw new NotFoundException(task, spaceId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    private Map<String, String> convertAclsToHeaders(Map<String, AclType> acls) {
        Map<String, String> headers = new HashMap<String, String>();
        if (acls != null) {
            for (String key : acls.keySet()) {
                AclType acl = acls.get(key);
                headers.put(HEADER_PREFIX + PROPERTIES_SPACE_ACL + key,
                            acl.name());
            }
        }
        return headers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean spaceExists(String spaceId) throws ContentStoreException {
        List<String> spaces = getSpaces();
        return spaces.contains(spaceId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String addContent(final String spaceId,
                             final String contentId,
                             final InputStream content,
                             final long contentSize,
                             final String contentMimeType,
                             final String contentChecksum,
                             final Map<String, String> contentProperties)
        throws ContentStoreException {
        //unlike other ContentStore methods, addContent() should not be retried since the stream
        //must be reset by the caller in order to produce valid results.
        return doAddContent(spaceId,
                            contentId,
                            content,
                            contentSize,
                            contentMimeType,
                            contentChecksum,
                            contentProperties);
    }

    private String doAddContent(String spaceId,
                                String contentId,
                                InputStream content,
                                long contentSize,
                                String contentMimeType,
                                String contentChecksum,
                                Map<String, String> contentProperties)
        throws ContentStoreException {
        validateContentId(contentId);
        String task = "add content";
        String url = buildContentURL(spaceId, contentId);

        // Include mimetype as properties
        if (contentMimeType != null && !contentMimeType.equals("")) {
            if (contentProperties == null) {
                contentProperties = new HashMap<String, String>();
            }
            contentProperties.put(CONTENT_MIMETYPE, contentMimeType);
        }

        Map<String, String> headers =
            convertPropertiesToHeaders(contentProperties);

        // Include checksum if provided
        if (contentChecksum != null) {
            headers.put(HttpHeaders.CONTENT_MD5, contentChecksum);
        }

        try {

            HttpResponse response = restHelper.put(url,
                                                   content,
                                                   contentMimeType,
                                                   contentSize,
                                                   headers);
            checkResponse(response, HttpStatus.SC_CREATED);
            Header checksum =
                response.getResponseHeader(HttpHeaders.CONTENT_MD5);
            if (checksum == null) {
                checksum = response.getResponseHeader(HttpHeaders.ETAG);
            }
            String returnedChecksum = checksum.getValue();

            if (contentChecksum != null && !returnedChecksum.equals(contentChecksum)) {
                String message = MessageFormat.format("checksum returned from durastore ({0}) " +
                                                          "does not match the checksum that was sent ({1}): " +
                                                          "task={2}, spaceId={3}, contentId={4}",
                                                      returnedChecksum,
                                                      contentChecksum,
                                                      task,
                                                      spaceId,
                                                      contentId);
                log.error(message);
                throw new ChecksumMismatchException(message,
                                                    false);
            }

            return returnedChecksum;

        } catch (InvalidIdException e) {
            throw new InvalidIdException(task, spaceId, contentId, e);
        } catch (NotFoundException e) {
            throw new NotFoundException(task, spaceId, contentId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, contentId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, contentId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String copyContent(final String srcSpaceId,
                              final String srcContentId,
                              final String destStoreId,
                              final String destSpaceId,
                              final String destContentId)
        throws ContentStoreException {
        return execute(new Retriable() {
            @Override
            public String retry() throws ContentStoreException {
                // The actual method being executed
                return doCopyContent(srcSpaceId,
                                     srcContentId,
                                     destStoreId,
                                     destSpaceId,
                                     destContentId);
            }
        });
    }

    private String doCopyContent(String srcSpaceId,
                                 String srcContentId,
                                 String destStoreId,
                                 String destSpaceId,
                                 String destContentId)
        throws ContentStoreException {
        validateStoreId(destStoreId);
        validateSpaceId(srcSpaceId);
        validateSpaceId(destSpaceId);
        validateContentId(srcContentId);
        validateContentId(destContentId);

        String task = "copy content";

        srcContentId = EncodeUtil.urlEncode(srcContentId);
        String url = buildContentURL(destStoreId, destSpaceId, destContentId);

        Map<String, String> headers = new HashMap<String, String>();

        String sourceHeader = HEADER_PREFIX + StorageProvider.PROPERTIES_COPY_SOURCE;
        String sourceValue = srcSpaceId + "/" + srcContentId;
        headers.put(sourceHeader, sourceValue);

        String storeHeader = HEADER_PREFIX + StorageProvider.PROPERTIES_COPY_SOURCE_STORE;
        headers.put(storeHeader, storeId);

        try {
            HttpResponse response = restHelper.put(url, null, headers);
            checkResponse(response, HttpStatus.SC_CREATED);
            Header checksum =
                response.getResponseHeader(HttpHeaders.CONTENT_MD5);
            if (checksum == null) {
                checksum = response.getResponseHeader(HttpHeaders.ETAG);
            }
            return checksum.getValue();

        } catch (InvalidIdException e) {
            throw new InvalidIdException(task,
                                         srcSpaceId,
                                         srcContentId,
                                         destSpaceId,
                                         destContentId,
                                         e);
        } catch (NotFoundException e) {
            throw new NotFoundException(task,
                                        srcSpaceId,
                                        srcContentId,
                                        destSpaceId,
                                        destContentId,
                                        e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task,
                                            srcSpaceId,
                                            srcContentId,
                                            destSpaceId,
                                            destContentId,
                                            e);
        } catch (Exception e) {
            throw new ContentStoreException(task,
                                            srcSpaceId,
                                            srcContentId,
                                            destSpaceId,
                                            destContentId,
                                            e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String copyContent(String srcSpaceId,
                              String srcContentId,
                              String destSpaceId,
                              String destContentId)
        throws ContentStoreException {
        return copyContent(srcSpaceId,
                           srcContentId,
                           getStoreId(),
                           destSpaceId,
                           destContentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String moveContent(String srcSpaceId,
                              String srcContentId,
                              String destSpaceId,
                              String destContentId)
        throws ContentStoreException {
        return moveContent(srcSpaceId,
                           srcContentId,
                           getStoreId(),
                           destSpaceId,
                           destContentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String moveContent(String srcSpaceId,
                              String srcContentId,
                              String destStoreId,
                              String destSpaceId,
                              String destContentId) throws ContentStoreException {

        String md5 = copyContent(srcSpaceId,
                                 srcContentId,
                                 destStoreId,
                                 destSpaceId,
                                 destContentId);

        deleteContent(srcSpaceId, srcContentId);

        return md5;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Content getContent(String spaceId, String contentId, Long startByte, Long endByte)
        throws ContentStoreException {

        //validate args
        if (startByte == null || startByte < 0) {
            throw new IllegalArgumentException("startByte must be equal to or greater than zero.");
        } else if (endByte != null && endByte <= startByte) {
            throw new IllegalArgumentException("endByte must be null or greater than the startByte.");
        }

        return execute(() -> {
            try {
                final HttpResponse response = doGetContent(spaceId, contentId, startByte, endByte);
                return toContent(response, spaceId, contentId, startByte, endByte);
            } catch (IOException ex) {
                throw new ContentStoreException(ex.getMessage(), ex);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Content getContent(final String spaceId, final String contentId)
        throws ContentStoreException {
        return getContent(spaceId, contentId, 0l, null);
    }

    private Content toContent(HttpResponse response, String spaceId, String contentId, Long startByte, Long endByte)
        throws IOException {
        Content content = new Content();
        content.setId(contentId);
        content.setStream(
            new PartialContentRetryInputStream(this, spaceId, contentId, response.getResponseStream(), startByte,
                                               endByte));
        content.setProperties(
            mergeMaps(extractPropertiesFromHeaders(response),
                      extractNonPropertiesHeaders(response)));
        return content;
    }

    protected HttpResponse doGetContent(String spaceId, String contentId, Long startByte, Long endByte)
        throws ContentStoreException {
        String task = "get content";
        String url = buildContentURL(spaceId, contentId);
        try {
            final boolean hasRange = !(startByte == 0l && endByte == null);
            final HttpResponse response;
            if (!hasRange) {
                response = restHelper.get(url);
            } else {
                Map<String, String> headers = new HashMap<>();
                headers.put("Range", "bytes=" + startByte + "-" + (endByte != null ? endByte : ""));
                response = restHelper.get(url, headers);
            }
            checkResponse(response, hasRange ? HttpStatus.SC_PARTIAL_CONTENT : HttpStatus.SC_OK);
            return response;
        } catch (NotFoundException e) {
            throw new NotFoundException(task, spaceId, contentId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, contentId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, contentId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteContent(final String spaceId, final String contentId)
        throws ContentStoreException {
        execute(new Retriable() {
            @Override
            public Boolean retry() throws ContentStoreException {
                // The actual method being executed
                doDeleteContent(spaceId, contentId);
                return true;
            }
        });
    }

    private void doDeleteContent(String spaceId, String contentId)
        throws ContentStoreException {
        String task = "delete content";
        String url = buildContentURL(spaceId, contentId);
        try {
            HttpResponse response = restHelper.delete(url);
            checkResponse(response, HttpStatus.SC_OK);
        } catch (NotFoundException e) {
            throw new NotFoundException(task, spaceId, contentId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, contentId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, contentId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setContentProperties(final String spaceId,
                                     final String contentId,
                                     final Map<String, String> contentProperties)
        throws ContentStoreException {
        execute(new Retriable() {
            @Override
            public Boolean retry() throws ContentStoreException {
                // The actual method being executed
                doSetContentProperties(spaceId, contentId, contentProperties);
                return true;
            }
        });
    }

    private void doSetContentProperties(String spaceId,
                                        String contentId,
                                        Map<String, String> contentProperties)
        throws ContentStoreException {
        String task = "update content properties";
        String url = buildContentURL(spaceId, contentId);
        Map<String, String> headers =
            convertPropertiesToHeaders(contentProperties);
        try {
            HttpResponse response = restHelper.post(url,
                                                    null,
                                                    headers);
            checkResponse(response, HttpStatus.SC_OK);
        } catch (NotFoundException e) {
            throw new NotFoundException(task, spaceId, contentId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, contentId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, contentId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getContentProperties(final String spaceId,
                                                    final String contentId)
        throws ContentStoreException {
        return execute(new Retriable() {
            @Override
            public Map<String, String> retry() throws ContentStoreException {
                // The actual method being executed
                return doGetContentProperties(spaceId, contentId);
            }
        });
    }

    private Map<String, String> doGetContentProperties(String spaceId,
                                                       String contentId)
        throws ContentStoreException {
        String task = "get properties";
        String url = buildContentURL(spaceId, contentId);
        try {
            HttpResponse response = restHelper.head(url);
            checkResponse(response, HttpStatus.SC_OK);
            return mergeMaps(extractPropertiesFromHeaders(response),
                             extractNonPropertiesHeaders(response));
        } catch (NotFoundException e) {
            throw new NotFoundException(task, spaceId, contentId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, contentId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, contentId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contentExists(String spaceId, String contentId)
        throws ContentStoreException {
        try {
            doGetContentProperties(spaceId, contentId);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    private void checkResponse(HttpResponse response, int expectedCode)
        throws ContentStoreException {
        if (response == null) {
            throw new ContentStoreException("Response content was null.");
        }
        int responseCode = response.getStatusCode();
        if (responseCode != expectedCode) {
            String errMsg = "";
            try {
                errMsg += response.getResponseBody();
            } catch (IOException e) {
                // Do not included response body in error
            }

            log.debug("Return code: {}; expected code: {}; responseBody={}",
                     responseCode, expectedCode, errMsg);

            if (responseCode == HttpStatus.SC_NOT_FOUND) {
                throw new NotFoundException(errMsg);
            } else if (responseCode == HttpStatus.SC_BAD_REQUEST) {
                throw new InvalidIdException(errMsg);
            } else if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new UnauthorizedException(errMsg);
            } else if (responseCode == HttpStatus.SC_NOT_IMPLEMENTED) {
                throw new NotImplementedException(errMsg);
            } else if (responseCode == HttpStatus.SC_FORBIDDEN) {
                throw new UnauthorizedException(
                    "User is not authorized to perform the requested function");
            } else if (responseCode == HttpStatus.SC_CONFLICT) {
                throw new ContentStateException(errMsg);
            } else {
                throw new ContentStoreException(errMsg);
            }
        }
    }

    private Map<String, String> convertPropertiesToHeaders(Map<String, String> properties) {
        Map<String, String> headers = new HashMap<String, String>();
        if (properties != null) {
            for (String key : properties.keySet()) {
                String value = properties.get(key);
                if (key.equalsIgnoreCase(CONTENT_ENCODING)) {
                    headers.put(CONTENT_ENCODING, value);
                } else {
                    headers.put(HEADER_PREFIX + key, value);
                }
            }
        }

        headers.put(Constants.CLIENT_VERSION_HEADER, clientVersion);
        return headers;
    }

    private Map<String, String> extractPropertiesFromHeaders(HttpResponse response) {
        return extractPropertiesFromHeaders(response, null);
    }

    private Map<String, String> extractPropertiesFromHeaders(HttpResponse response, String keyPrefix) {
        Map<String, String> properties = new HashMap<String, String>();
        String prefix = HEADER_PREFIX + (keyPrefix != null ? keyPrefix : "");
        for (Header header : response.getResponseHeaders()) {
            String name = header.getName();
            if (name.startsWith(prefix)) {
                properties.put(name.substring(prefix.length()),
                               header.getValue());
            }
        }
        return properties;
    }

    private Map<String, String> extractNonPropertiesHeaders(HttpResponse response) {
        Map<String, String> headers = new HashMap<String, String>();
        for (Header header : response.getResponseHeaders()) {
            String name = header.getName();
            if (!name.startsWith(HEADER_PREFIX)) {
                if (name.equals(HttpHeaders.CONTENT_TYPE)) {
                    headers.put(CONTENT_MIMETYPE, header.getValue());
                } else if (name.equals(HttpHeaders.CONTENT_MD5) ||
                    name.equals(HttpHeaders.ETAG)) {
                    headers.put(CONTENT_CHECKSUM, header.getValue());
                } else if (name.equals(HttpHeaders.CONTENT_LENGTH)) {
                    headers.put(CONTENT_SIZE, header.getValue());
                } else if (name.equals(HttpHeaders.LAST_MODIFIED)) {
                    headers.put(CONTENT_MODIFIED, header.getValue());
                } else if (name.equals(CONTENT_ENCODING)) {
                    headers.put(CONTENT_ENCODING, header.getValue());
                }
            }
        }
        return headers;
    }

    /*
     * Adds all mappings from map1 into map2. In the case of a conflict the
     * values from map1 will win.
     */
    private Map<String, String> mergeMaps(Map<String, String> map1, Map<String, String> map2) {
        Iterator<String> map1Names = map1.keySet().iterator();
        while (map1Names.hasNext()) {
            String name = map1Names.next();
            map2.put(name, map1.get(name));
        }
        return map2;
    }

    public void validateStoreId(String storeId) throws InvalidIdException {
        try {
            IdUtil.validateStoreId(storeId);
        } catch (org.duracloud.storage.error.InvalidIdException e) {
            throw new InvalidIdException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateSpaceId(String spaceId) throws InvalidIdException {
        try {
            IdUtil.validateSpaceId(spaceId);
        } catch (org.duracloud.storage.error.InvalidIdException e) {
            throw new InvalidIdException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateContentId(String contentId) throws InvalidIdException {
        try {
            IdUtil.validateContentId(contentId);
        } catch (org.duracloud.storage.error.InvalidIdException e) {
            throw new InvalidIdException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSupportedTasks() throws ContentStoreException {
        return execute(new Retriable() {
            @Override
            public List<String> retry() throws ContentStoreException {
                // The actual method being executed
                return doGetSupportedTasks();
            }
        });
    }

    private List<String> doGetSupportedTasks() throws ContentStoreException {
        String url = buildTaskURL();
        try {
            HttpResponse response = restHelper.get(url);
            checkResponse(response, HttpStatus.SC_OK);
            String reponseText = response.getResponseBody();
            return SerializationUtil.deserializeList(reponseText);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException("Not authorized to get supported " +
                                                "tasks", e);
        } catch (Exception e) {
            throw new ContentStoreException("Error getting supported tasks: " +
                                                e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String performTask(final String taskName,
                              final String taskParameters)
        throws ContentStoreException {
        return execute(new Retriable() {
            @Override
            public String retry() throws ContentStoreException {
                // The actual method being executed
                return doPerformTask(taskName, taskParameters);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String performTaskWithNoRetries(final String taskName,
                                           final String taskParameters)
        throws ContentStoreException {

        return doPerformTask(taskName, taskParameters);
    }

    private String doPerformTask(String taskName, String taskParameters)
        throws ContentStoreException {
        String url = buildTaskURL(taskName);
        try {
            HttpResponse response = restHelper.post(url, taskParameters, null);
            checkResponse(response, HttpStatus.SC_OK);
            return response.getResponseBody();
        } catch (InvalidIdException e) {
            throw new UnsupportedTaskException(taskName, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException("Not authorized to perform task: " +
                                                taskName, e);
        } catch (ContentStateException e) {
            throw e;
        } catch (Exception e) {
            throw new ContentStoreException("Error performing task (" +
                                                taskName + "):  " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getManifest(String spaceId, ManifestFormat format)
        throws ContentStoreException {
        String task = "get manifest";
        String url = buildManifestURL(spaceId, format);
        try {
            HttpResponse response = restHelper.get(url);
            checkResponse(response, HttpStatus.SC_OK);
            return response.getResponseStream();
        } catch (NotFoundException e) {
            throw new NotFoundException(task, spaceId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    private String buildManifestURL(String spaceId, ManifestFormat format) {
        String url = buildURL("/manifest/" + spaceId);
        url = addStoreIdQueryParameter(url);

        if (format != null) {
            url += "&format=" + format.name();
        }

        return url;
    }

    @Override
    public InputStream getAuditLog(String spaceId)
        throws ContentStoreException {
        String task = "get manifest";
        String url = buildAuditLogURL(spaceId);
        try {
            HttpResponse response = restHelper.get(url);
            checkResponse(response, HttpStatus.SC_OK);
            return response.getResponseStream();
        } catch (NotFoundException e) {
            throw new NotFoundException(task, spaceId, e);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    private String buildAuditLogURL(String spaceId) {
        String url = buildURL("/audit/" + spaceId);
        url = addStoreIdQueryParameter(url);
        return url;
    }

    @Override
    public BitIntegrityReport getBitIntegrityReport(String spaceId)
        throws ContentStoreException {
        String task = "get bit integrity report";
        String url = buildBitIntegrityReportURL(spaceId);
        try {
            HttpResponse response = restHelper.get(url);
            if (hasNoContent(response)) {
                return null;
            }
            checkResponse(response, HttpStatus.SC_OK);
            //This is a workaround for duracloud-1137
            //for reasons that are not clear, in the beanstalk environment
            //the httpclient is swallowing the Content-Length header
            //leading the BitIntegrityReportProperties.getSize() method to
            //return 0.  This appears to be the case only on the GET call.
            //To work around it,  we are loading the properties with a separate
            //call that uses the HEAD path.
            BitIntegrityReportProperties properties = getBitIntegrityReportProperties(spaceId);
            BitIntegrityReport report =
                new BitIntegrityReport(response.getResponseStream(), properties);
            return report;
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    private boolean hasNoContent(HttpResponse response) {
        return (response.getStatusCode() == HttpStatus.SC_NO_CONTENT);
    }

    @Override
    public BitIntegrityReportProperties getBitIntegrityReportProperties(String spaceId)
        throws ContentStoreException {
        String task = "get bit integrity report properties";
        String url = buildBitIntegrityReportURL(spaceId);
        try {
            HttpResponse response = restHelper.head(url);
            if (hasNoContent(response)) {
                return null;
            }
            checkResponse(response, HttpStatus.SC_OK);
            return extractBitIntegrityProperties(response);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    private String buildBitIntegrityReportURL(String spaceId) {
        String url = buildURL("/bit-integrity/" + spaceId);
        url = addStoreIdQueryParameter(url);
        return url;
    }

    private BitIntegrityReportProperties extractBitIntegrityProperties(HttpResponse response)
        throws ParseException {
        BitIntegrityReportProperties properties =
            new BitIntegrityReportProperties();
        for (Header header : response.getResponseHeaders()) {
            String name = header.getName();
            if (name.equals(HttpHeaders.BIT_INTEGRITY_REPORT_RESULT)) {
                properties.setResult(BitIntegrityReportResult.valueOf(header.getValue()));
            } else if (name.equals(HttpHeaders.BIT_INTEGRITY_REPORT_COMPLETION_DATE)) {
                SimpleDateFormat format =
                    new SimpleDateFormat(DateFormat.DEFAULT_FORMAT.getPattern());
                properties.setCompletionDate(format.parse(header.getValue()));
            } else if (name.equals(HttpHeaders.CONTENT_LENGTH)) {
                properties.setSize(Long.valueOf(header.getValue()));
            }
        }

        return properties;
    }

    @Override
    public SpaceStatsDTOList getSpaceStats(String spaceId, Date start, Date end)
        throws ContentStoreException {
        String url = buildSpaceStatsURL(spaceId, start, end);
        try {
            HttpResponse response = restHelper.get(url);
            checkResponse(response, HttpStatus.SC_OK);
            String body = response.getResponseBody();
            JaxbJsonSerializer<SpaceStatsDTOList> serializer = new JaxbJsonSerializer<>(SpaceStatsDTOList.class);
            return serializer.deserialize(body);
        } catch (NotFoundException e) {
            throw new NotFoundException("spaceId = " + spaceId);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException("failed to retrieve space stats for " + spaceId, e);
        }
    }

    private String buildSpaceStatsURL(String spaceId, Date start, Date end) {
        String url = buildURL("/report/space/" + spaceId);

        if (start != null) {
            url = addQueryParameter(url, "start", start.getTime() + "");
        }

        if (end != null) {
            url = addQueryParameter(url, "end", end.getTime() + "");
        }

        url = addStoreIdQueryParameter(url);
        return url;
    }

    @Override
    public SpaceStatsDTOList getStorageProviderStats(Date start, Date end)
        throws ContentStoreException {
        String url = buildStorageProviderStatsURL(start, end);
        try {
            HttpResponse response = restHelper.get(url);
            checkResponse(response, HttpStatus.SC_OK);
            String body = response.getResponseBody();
            JaxbJsonSerializer<SpaceStatsDTOList> serializer = new JaxbJsonSerializer<>(SpaceStatsDTOList.class);
            return serializer.deserialize(body);
        } catch (NotFoundException e) {
            throw new NotFoundException("storeId = " + getStoreId());
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(getStoreId(), e);
        } catch (Exception e) {
            throw new ContentStoreException("failed to retrieve storage provider stats for " + getStoreId(), e);
        }
    }

    private String buildStorageProviderStatsURL(Date start, Date end) {
        String url = buildURL("/report/store");

        if (start != null) {
            url = addQueryParameter(url, "start", start.getTime() + "");
        }

        if (end != null) {
            url = addQueryParameter(url, "end", end.getTime() + "");
        }

        url = addStoreIdQueryParameter(url);
        return url;
    }

    @Override
    public SpaceStatsDTOList getStorageProviderStatsByDay(Date date)
        throws ContentStoreException {
        String url = buildStorageProviderStatsURL(date);
        try {
            HttpResponse response = restHelper.get(url);
            checkResponse(response, HttpStatus.SC_OK);
            String body = response.getResponseBody();
            JaxbJsonSerializer<SpaceStatsDTOList> serializer = new JaxbJsonSerializer<>(SpaceStatsDTOList.class);
            return serializer.deserialize(body);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(storeId, e);
        } catch (Exception e) {
            throw new ContentStoreException("failed to retrieve storage provider stats for " + storeId, e);
        }
    }

    private String buildStorageProviderStatsURL(Date date) {
        String url = buildURL("/report/store/" + date.getTime());
        url = addStoreIdQueryParameter(url);
        return url;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
