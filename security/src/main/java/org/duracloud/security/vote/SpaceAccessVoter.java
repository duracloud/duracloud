/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import static org.duracloud.security.vote.VoterUtil.debugText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.intercept.web.FilterInvocation;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import org.springframework.security.vote.AccessDecisionVoter;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Mar 19, 2010
 */
public abstract class SpaceAccessVoter implements AccessDecisionVoter {
    private final Logger log = LoggerFactory.getLogger(SpaceAccessVoter.class);

    private Map<String, ContentStore.AccessType> spaceCache = new HashMap<String, ContentStore.AccessType>();

    /**
     * This method checks the access state of the arg resource
     * (space and provider) and makes denies access to anonymous principals if
     * the space is closed.
     *
     * @param auth     principal seeking AuthZ
     * @param resource that is under protection
     * @param config   access-attributes defined on resource
     * @return vote (AccessDecisionVoter.ACCESS_GRANTED, ACCESS_DENIED, ACCESS_ABSTAIN)
     */
    public int vote(Authentication auth,
                    Object resource,
                    ConfigAttributeDefinition config) {
        String label = "SpaceAccessVoterImpl";
        if (resource != null && !supports(resource.getClass())) {
            log.debug(debugText(label, auth, config, resource, ACCESS_ABSTAIN));
            return ACCESS_ABSTAIN;
        }

        HttpServletRequest httpRequest = getHttpServletRequest(resource);
        if (null == httpRequest) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        // If space is 'open' or 'closed' only matters if user is anonymous.
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }

        ContentStore.AccessType access = getSpaceAccess(httpRequest);
        int grant = ACCESS_DENIED;
        if (access.equals(ContentStore.AccessType.OPEN)) {
            grant = ACCESS_GRANTED;
        }

        log.debug(debugText(label, auth, config, resource, grant));
        return grant;
    }


    private ContentStore.AccessType getSpaceAccess(HttpServletRequest request) {
        String host = "localhost";
        String port = Integer.toString(request.getLocalPort());

        String storeId = getStoreId(request);
        String spaceId = getSpaceId(request);
        if (null == spaceId) {
            return ContentStore.AccessType.CLOSED;
        }

        if (spaceId.equals("spaces") || spaceId.equals("stores")) {
            return ContentStore.AccessType.OPEN;
        }

        ContentStore.AccessType access = getAccessFromCache(storeId, spaceId);
        if (access != null) {
            return access;
        }

        ContentStore store = getContentStore(host, port, storeId);
        if (null == store) {
            return ContentStore.AccessType.CLOSED;
        }

        access = ContentStore.AccessType.CLOSED;
        try {
            access = store.getSpaceAccess(spaceId);
        } catch (ContentStoreException e) {

        }
        return access;
    }

    private String getStoreId(HttpServletRequest httpRequest) {
        String storeId = null;
        String query = httpRequest.getQueryString();
        if (null == query) {
            return null;
        }

        query = query.toLowerCase();

        String name = "storeid";
        int storeIdIndex = query.indexOf(name);
        if (storeIdIndex > -1) {
            int idIndex = query.indexOf("=", storeIdIndex) + 1;
            if (idIndex == storeIdIndex + name.length() + 1) {
                int nextParamIndex = query.indexOf("&", idIndex);
                int end = nextParamIndex > -1 ? nextParamIndex : query.length();
                storeId = query.substring(idIndex, end);
            }
        }
        return storeId;
    }

    private String getSpaceId(HttpServletRequest httpRequest) {
        String spaceId = httpRequest.getPathInfo();
        if (null == spaceId) {
            return null;
        }

        if (spaceId.startsWith("/")) {
            spaceId = spaceId.substring(1);
        }

        int slashIndex = spaceId.indexOf("/");
        if (slashIndex > 0) {
            spaceId = spaceId.substring(0, slashIndex);
        }
        return spaceId;
    }

    private ContentStore.AccessType getAccessFromCache(String storeId,
                                                       String spaceId) {
        // TODO: implement cache
        return null;
    }

    /**
     * This method is abstract to provide entry-point for alternate
     * implementations of ContentStore.
     */
    abstract protected ContentStore getContentStore(String host,
                                                    String port,
                                                    String storeId);


    private HttpServletRequest getHttpServletRequest(Object resource) {
        FilterInvocation invocation = (FilterInvocation) resource;
        HttpServletRequest request = invocation.getHttpRequest();

        if (null == request) {
            String msg = "null request: '" + resource + "'";
            log.warn("HttpServletRequest was null!  " + msg);
        }
        return request;
    }

    /**
     * This method always returns true because all configAttributes are able
     * to be handled by this voter.
     *
     * @param configAttribute any att
     * @return true
     */
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    /**
     * This methods returns true if the arg class is an instance of or
     * subclass of FilterInvocation.
     * No other classes can be handled by this voter.
     *
     * @param aClass to be analyized for an AuthZ vote.
     * @return true if is an instance of or subclass of FilterInvocation
     */
    public boolean supports(Class aClass) {
        return FilterInvocation.class.isAssignableFrom(aClass);
    }


}
