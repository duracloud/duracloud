/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.Securable;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.xml.StorageAccountsDocumentBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides facilities for connecting to a set of content stores
 *
 * @author Bill Branan
 */
public class ContentStoreManagerImpl implements ContentStoreManager, Securable {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String DEFAULT_CONTEXT = "durastore";

    private String baseURL = null;

    private RestHttpHelper restHelper;

    /**
     * <p>Constructor for ContentStoreManagerImpl.</p>
     *
     * @param host a {@link java.lang.String} object.
     * @param port a {@link java.lang.String} object.
     */
    public ContentStoreManagerImpl(String host, String port) {
        this(host, port, DEFAULT_CONTEXT);
    }

    /**
     * <p>Constructor for ContentStoreManagerImpl.</p>
     *
     * @param host the host name on which DuraStore can be accessed
     * @param port the port on which DuraStore can be accessed
     * @param context the application context by which DuraStore can be accessed
     */
    public ContentStoreManagerImpl(String host, String port, String context) {
        init(host, port, context);
    }

    private void init(String host, String port, String context) {
        if (host == null || host.equals("")) {
            throw new IllegalArgumentException("Host must be a valid server host name");
        }

        if (context == null) {
            context = DEFAULT_CONTEXT;
        }

        if (port == null || port.equals("")) {
            baseURL = "http://" + host + "/" + context;
        } else if (port.equals("443")) {
            baseURL = "https://" + host + "/" + context;
        } else {
            baseURL = "http://" + host + ":" + port + "/" + context;
        }
    }

    public void reinitialize(String host, String port, String context)
        throws ContentStoreException {
        init(host, port, context);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, ContentStore> getContentStores()
        throws ContentStoreException {
        return getContentStores(-1); // Use default retries
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, ContentStore> getContentStores(int maxRetries)
        throws ContentStoreException {
        log.debug("getContentStores()");
        StorageAccountManager acctManager = getStorageAccounts();
        Map<String, StorageAccount> accounts = acctManager.getStorageAccounts();
        Map<String, ContentStore> contentStores =
            new HashMap<String, ContentStore>();
        Iterator<String> acctIDs = accounts.keySet().iterator();
        while (acctIDs.hasNext()) {
            String acctID = acctIDs.next();
            StorageAccount acct = accounts.get(acctID);
            contentStores.put(acctID, newContentStoreImpl(acct, maxRetries));
        }
        return contentStores;
    }

    /**
     * {@inheritDoc}
     */
    public ContentStore getContentStore(String storeID)
        throws ContentStoreException {
        return getContentStore(storeID, -1); // Use default retries
    }

    /**
     * {@inheritDoc}
     */
    public ContentStore getContentStore(String storeID, int maxRetries)
        throws ContentStoreException {
        StorageAccountManager acctManager = getStorageAccounts();
        StorageAccount acct = acctManager.getStorageAccount(storeID);
        return newContentStoreImpl(acct, maxRetries);
    }

    /**
     * {@inheritDoc}
     */
    public ContentStore getPrimaryContentStore() throws ContentStoreException {
        return getPrimaryContentStore(-1); // Use default retries
    }

    /**
     * {@inheritDoc}
     */
    public ContentStore getPrimaryContentStore(int maxRetries)
        throws ContentStoreException {
        StorageAccountManager acctManager = getStorageAccounts();
        StorageAccount acct = acctManager.getPrimaryStorageAccount();
        return newContentStoreImpl(acct, maxRetries);
    }

    /**
     * {@inheritDoc}
     */
    public ContentStore getPrimaryContentStoreAsAnonymous()
        throws ContentStoreException {
        return getPrimaryContentStoreAsAnonymous(-1);
    }

    /**
     * {@inheritDoc}
     */
    public ContentStore getPrimaryContentStoreAsAnonymous(int maxRetries)
        throws ContentStoreException {
        return newAnonymousContentStoreImpl(maxRetries);
    }

    public void login(Credential appCred) {
        log.debug("login: " + appCred.getUsername());
        setRestHelper(new RestHttpHelper(appCred));
    }

    public void logout() {
        log.debug("logout");
        setRestHelper(new RestHttpHelper());
    }

    private StorageAccountManager getStorageAccounts()
        throws ContentStoreException {
        String url = baseURL + "/stores";
        HttpResponse response;
        String error = "Error retrieving content stores. ";
        try {
            response = getRestHelper().get(url);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                String storesXML = response.getResponseBody();
                if (storesXML != null) {
                    InputStream xmlStream =
                        new ByteArrayInputStream(storesXML.getBytes("UTF-8"));
                    StorageAccountsDocumentBinding binding =
                        new StorageAccountsDocumentBinding();
                    List<StorageAccount> accts =
                        binding.createStorageAccountsFromXml(xmlStream);
                    StorageAccountManager storageAccountManager =
                        new StorageAccountManager();
                    storageAccountManager.initialize(accts);
                    return storageAccountManager;
                } else {
                    throw new StorageException(error +
                                               "Response content was null");
                }
            } else {
                error += "Response code was " + response.getStatusCode() +
                    ", expected value was " + HttpStatus.SC_OK +
                    ". Response Body: " + response.getResponseBody();
                throw new StorageException(error);
            }
        } catch (Exception e) {
            throw new ContentStoreException(error + e.getMessage(), e);
        }
    }

    protected ContentStore newContentStoreImpl(StorageAccount acct) {
        return new ContentStoreImpl(baseURL,
                                    acct.getType(),
                                    acct.getId(),
                                    getRestHelper());
    }

    protected ContentStore newContentStoreImpl(StorageAccount acct,
                                               int maxRetries) {
        return new ContentStoreImpl(baseURL,
                                    acct.getType(),
                                    acct.getId(),
                                    getRestHelper(),
                                    maxRetries);
    }

    private ContentStore newAnonymousContentStoreImpl() {
        return new ContentStoreImpl(baseURL,
                                    StorageProviderType.UNKNOWN,
                                    null,
                                    getRestHelper());
    }

    private ContentStore newAnonymousContentStoreImpl(int maxRetries) {
        return new ContentStoreImpl(baseURL,
                                    StorageProviderType.UNKNOWN,
                                    null,
                                    getRestHelper(),
                                    maxRetries);
    }

    protected String getBaseURL() {
        return baseURL;
    }

    protected RestHttpHelper getRestHelper() {
        if (null == restHelper) {
            restHelper = new RestHttpHelper();
        }
        return restHelper;
    }

    protected void setRestHelper(RestHttpHelper restHelper) {
        this.restHelper = restHelper;
    }
}
