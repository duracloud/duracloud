/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.model.AclType;
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
import org.duracloud.error.UnauthorizedException;
import org.duracloud.error.UnsupportedTaskException;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.IdUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_SPACE_ACL;

/**
 * Provides access to a content store
 *
 * @author Bill Branan
 */
public class ContentStoreImpl implements ContentStore{

    private String storeId = null;

    private StorageProviderType type = null;

    private String baseURL = null;

    private RestHttpHelper restHelper;

    private static final String HEADER_PREFIX = "x-dura-meta-";

    /**
     * Creates a ContentStore
     *
     * @param baseURL a {@link java.lang.String} object.
     * @param type a {@link org.duracloud.storage.domain.StorageProviderType} object.
     * @param storeId a {@link java.lang.String} object.
     */
    public ContentStoreImpl(String baseURL,
                            StorageProviderType type,
                            String storeId,
                            RestHttpHelper restHelper) {
        this.baseURL = baseURL;
        this.type = type;
        this.storeId = storeId;
        this.restHelper = restHelper;
    }


    public String getBaseURL() {
        return baseURL;
    }

    /**
     * {@inheritDoc}
     */
    public String getStoreId() {
        return storeId;
    }

    /**
     * {@inheritDoc}
     */
    public String getStorageProviderType() {
        return type.name();
    }

    private String addStoreIdQueryParameter(String url){
        return addStoreIdQueryParameter(url, storeId);
    }

    private String addStoreIdQueryParameter(String url, String storeId){
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

    /**
     * {@inheritDoc}
     */
    public List<String> getSpaces() throws ContentStoreException {
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
        } catch(UnauthorizedException e) {
            throw new UnauthorizedException(task, "listing", e);            
        } catch (Exception e) {
            throw new ContentStoreException("Error attempting to get spaces " +
                                            "due to: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaceContents(String spaceId)
        throws ContentStoreException {
        return getSpaceContents(spaceId, null);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getSpaceContents(String spaceId, String prefix)
        throws ContentStoreException {
        return new ContentIterator(this, spaceId, prefix);
    }

    /**
     * {@inheritDoc}
     */
    public Space getSpace(String spaceId,
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
        } catch(NotFoundException e) {
            throw new NotFoundException(task, spaceId, e);
        } catch(UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createSpace(String spaceId)
            throws ContentStoreException {
        validateSpaceId(spaceId);
        String task = "create space";
        String url = buildSpaceURL(spaceId);
        try {
            HttpResponse response = restHelper.put(url, null, null);
            checkResponse(response, HttpStatus.SC_CREATED);
        } catch (InvalidIdException e) {
            throw new InvalidIdException(task, spaceId, e);
        } catch(UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteSpace(String spaceId) throws ContentStoreException {
        String task = "delete space";
        String url = buildSpaceURL(spaceId);
        try {
            HttpResponse response = restHelper.delete(url);
            checkResponse(response, HttpStatus.SC_OK);
        } catch(NotFoundException e) {
            throw new NotFoundException(task, spaceId, e);
        } catch(UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getSpaceProperties(String spaceId)
            throws ContentStoreException {
        String task = "get space properties";
        String url = buildSpaceURL(spaceId);
        try {
            HttpResponse response = restHelper.head(url);
            checkResponse(response, HttpStatus.SC_OK);
            return extractPropertiesFromHeaders(response);
        } catch(NotFoundException e) {
            throw new NotFoundException(task, spaceId, e);
        } catch(UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, e);
        }
    }

    @Override
    public Map<String, AclType> getSpaceACLs(String spaceId)
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
        Map<String, String> aclProps = extractPropertiesFromHeaders(response,
                                                                    PROPERTIES_SPACE_ACL);
        for (String key : aclProps.keySet()) {
            String val = aclProps.get(key);
            acls.put(key, AclType.valueOf(val));
        }
        return acls;
    }

    @Override
    public void setSpaceACLs(String spaceId, Map<String, AclType> spaceACLs)
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
    public String addContent(String spaceId,
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
        if(contentMimeType != null && !contentMimeType.equals("")) {
            if(contentProperties == null) {
                contentProperties = new HashMap<String, String>();
            }
            contentProperties.put(CONTENT_MIMETYPE, contentMimeType);
        }

        Map<String, String> headers =
            convertPropertiesToHeaders(contentProperties);

        // Include checksum if provided
        if(contentChecksum != null) {
            headers.put(HttpHeaders.CONTENT_MD5, contentChecksum);
        }
        
        try {
            HttpResponse response = restHelper.put(url,
                                                   content,
                                                   String.valueOf(contentSize),
                                                   contentMimeType,
                                                   headers);
            checkResponse(response, HttpStatus.SC_CREATED);
            Header checksum =
                response.getResponseHeader(HttpHeaders.CONTENT_MD5);
            if(checksum == null) {
                checksum = response.getResponseHeader(HttpHeaders.ETAG);
            }
            return checksum.getValue();
        } catch (InvalidIdException e) {
            throw new InvalidIdException(task, spaceId, contentId, e);            
        } catch(NotFoundException e) {
            throw new NotFoundException(task, spaceId, contentId, e);
        } catch(UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, contentId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, contentId, e);
        }
    }

    @Override
    public String copyContent(String srcSpaceId,
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
        String sourceValue =  srcSpaceId + "/" + srcContentId;
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

        deleteContent(srcSpaceId,srcContentId);

        return md5;
    }

    /**
     * {@inheritDoc}
     */
    public Content getContent(String spaceId, String contentId)
            throws ContentStoreException {
        String task = "get content";
        String url = buildContentURL(spaceId, contentId);
        try {
            HttpResponse response = restHelper.get(url);
            checkResponse(response, HttpStatus.SC_OK);
            Content content = new Content();
            content.setId(contentId);
            content.setStream(response.getResponseStream());
            content.setProperties(
                mergeMaps(extractPropertiesFromHeaders(response),
                          extractNonPropertiesHeaders(response)));
            return content;
        } catch(NotFoundException e) {
            throw new NotFoundException(task, spaceId, contentId, e);
        } catch(UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, contentId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, contentId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteContent(String spaceId, String contentId)
            throws ContentStoreException {
        String task = "delete content";
        String url = buildContentURL(spaceId, contentId);
        try {
            HttpResponse response = restHelper.delete(url);
            checkResponse(response, HttpStatus.SC_OK);
        } catch(NotFoundException e) {
            throw new NotFoundException(task, spaceId, contentId, e);
        } catch(UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, contentId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, contentId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setContentProperties(String spaceId,
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
        } catch(NotFoundException e) {
            throw new NotFoundException(task, spaceId, contentId, e);
        } catch(UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, contentId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, contentId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getContentProperties(String spaceId,
                                                    String contentId)
            throws ContentStoreException {
        String task = "get properties";
        String url = buildContentURL(spaceId, contentId);
        try {
            HttpResponse response = restHelper.head(url);
            checkResponse(response, HttpStatus.SC_OK);
            return mergeMaps(extractPropertiesFromHeaders(response),
                             extractNonPropertiesHeaders(response));
        } catch(NotFoundException e) {
            throw new NotFoundException(task, spaceId, contentId, e);
        } catch(UnauthorizedException e) {
            throw new UnauthorizedException(task, spaceId, contentId, e);
        } catch (Exception e) {
            throw new ContentStoreException(task, spaceId, contentId, e);
        }
    }

    private void checkResponse(HttpResponse response, int expectedCode)
            throws ContentStoreException {
        if (response == null) {
            throw new ContentStoreException("Response content was null.");
        }
        int responseCode = response.getStatusCode();
        if (responseCode != expectedCode) {
            String errMsg = "Response code was " + responseCode +
                            ", expected value was " + expectedCode + ". ";
            try {
                String responseBody = response.getResponseBody();
                errMsg += ("Response body value: " + responseBody);
            } catch(IOException e) {
                // Do not included response body in error
            }
            if(responseCode == HttpStatus.SC_NOT_FOUND) {
                throw new NotFoundException(errMsg);
            } else if (responseCode == HttpStatus.SC_BAD_REQUEST) {
                throw new InvalidIdException(errMsg);
            } else if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new UnauthorizedException(errMsg);
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
                headers.put(HEADER_PREFIX + key, properties.get(key));
            }
        }
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
                if(name.equals(HttpHeaders.CONTENT_TYPE)) {
                    headers.put(CONTENT_MIMETYPE, header.getValue());
                } else if (name.equals(HttpHeaders.CONTENT_MD5) ||
                           name.equals(HttpHeaders.ETAG)) {
                    headers.put(CONTENT_CHECKSUM, header.getValue());
                } else if (name.equals(HttpHeaders.CONTENT_LENGTH)) {
                    headers.put(CONTENT_SIZE, header.getValue());
                } else if (name.equals(HttpHeaders.LAST_MODIFIED)) {
                    headers.put(CONTENT_MODIFIED, header.getValue());
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
        while(map1Names.hasNext()) {
            String name = map1Names.next();
            map2.put(name, map1.get(name));
        }
        return map2;
    }


    public void validateStoreId(String storeId) throws InvalidIdException {
        try {
            IdUtil.validateStoreId(storeId);
        } catch(org.duracloud.storage.error.InvalidIdException e) {
            throw new InvalidIdException(e.getMessage());
        }
    }


    
    /**
     * {@inheritDoc}
     */
    public void validateSpaceId(String spaceId) throws InvalidIdException {
        try {
            IdUtil.validateSpaceId(spaceId);
        } catch(org.duracloud.storage.error.InvalidIdException e) {
            throw new InvalidIdException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void validateContentId(String contentId) throws InvalidIdException {
        try {
            IdUtil.validateContentId(contentId);
        } catch(org.duracloud.storage.error.InvalidIdException e) {
            throw new InvalidIdException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getSupportedTasks()
        throws ContentStoreException {
        String url = buildTaskURL();
        try {
            HttpResponse response = restHelper.get(url);
            checkResponse(response, HttpStatus.SC_OK);
            String reponseText = response.getResponseBody();
            return SerializationUtil.deserializeList(reponseText);
        } catch(UnauthorizedException e) {
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
    public String performTask(String taskName, String taskParameters)
        throws ContentStoreException {
        String url = buildTaskURL(taskName);
        try {
            HttpResponse response = restHelper.post(url, taskParameters, null);
            checkResponse(response, HttpStatus.SC_OK);
            return response.getResponseBody();
        } catch(InvalidIdException e) {
            throw new UnsupportedTaskException(taskName, e);
        } catch(UnauthorizedException e) {
            throw new UnauthorizedException("Not authorized to perform task: " +
                                            taskName, e);
        } catch (Exception e) {
            throw new ContentStoreException("Error performing task: " +
                                            taskName + e.getMessage(), e);
        }
    }



}
