/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.Securable;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    public Map<String, ContentStore> getContentStores() throws ContentStoreException {
        log.debug("getContentStores()");
        StorageAccountManager acctManager = getStorageAccounts();
        Map<String, StorageAccount> accounts = acctManager.getStorageAccounts();
        Map<String, ContentStore> contentStores =
            new HashMap<String, ContentStore>();
        Iterator<String> acctIDs = accounts.keySet().iterator();
        while (acctIDs.hasNext()) {
            String acctID = acctIDs.next();
            StorageAccount acct = accounts.get(acctID);
            contentStores.put(acctID, newContentStoreImpl(acct));
        }
        return contentStores;
    }

    /**
     * {@inheritDoc}
     */
    public ContentStore getContentStore(String storeID) throws ContentStoreException {
        StorageAccountManager acctManager = getStorageAccounts();
        StorageAccount acct = acctManager.getStorageAccount(storeID);
        return newContentStoreImpl(acct);
    }

    /**
     * {@inheritDoc}
     */
    public ContentStore getPrimaryContentStore() throws ContentStoreException {
        StorageAccountManager acctManager = getStorageAccounts();
        StorageAccount acct = acctManager.getPrimaryStorageAccount();
        return newContentStoreImpl(acct);
    }

    /**
     * {@inheritDoc}
     */
    public ContentStore getPrimaryContentStoreAsAnonymous() 
        throws ContentStoreException {
        return newAnonymousContentStoreImpl();
    }

    public void login(Credential appCred) {
        log.debug("login: " + appCred.getUsername());
        setRestHelper(new RestHttpHelper(appCred));
    }

    public void logout() {
        log.debug("logout");
        setRestHelper(new RestHttpHelper());
    }

    private StorageAccountManager getStorageAccounts() throws ContentStoreException {
        String url = baseURL + "/stores";
        HttpResponse response;
        String error = "Error retrieving content stores. ";
        try {
            response = getRestHelper().get(url);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                String storesXML = response.getResponseBody();
                if (storesXML != null) {
                    InputStream is = new ByteArrayInputStream(storesXML.getBytes());
                    return new StorageAccountManager(is, true);
                } else {
                    throw new StorageException(error + "Response content was null");
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

    private ContentStore newContentStoreImpl(StorageAccount acct) {
        return new ContentStoreImpl(baseURL,
                                    acct.getType(),
                                    acct.getId(),
                                    getRestHelper());
    }

    private ContentStore newAnonymousContentStoreImpl() {
        return new ContentStoreImpl(baseURL,
                                    StorageProviderType.UNKNOWN,
                                    null,
                                    getRestHelper());
    }


    private RestHttpHelper getRestHelper() {
        if (null == restHelper) {
            restHelper = new RestHttpHelper();
        }
        return restHelper;
    }

    private void setRestHelper(RestHttpHelper restHelper) {
        this.restHelper = restHelper;
    }
}
