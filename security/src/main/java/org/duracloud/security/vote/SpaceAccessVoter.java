/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.duracloud.common.model.AclType;
import org.duracloud.common.model.RootUserCredential;
import org.duracloud.security.domain.HttpVerb;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.FilterInvocation;

/**
 * @author Andrew Woods
 *         Date: Mar 19, 2010
 */
public abstract class SpaceAccessVoter implements AccessDecisionVoter {

    private final Logger log = LoggerFactory.getLogger(SpaceAccessVoter.class);

    private StorageProviderFactory storageProviderFactory;
    private UserDetailsService userDetailsService;
   
    
    public SpaceAccessVoter(StorageProviderFactory storageProviderFactory,
                            UserDetailsService userDetailsService) {
        this.storageProviderFactory = storageProviderFactory;
        this.userDetailsService = userDetailsService;
    }

    protected StorageProviderFactory getStorageProviderFactory(){
        return this.storageProviderFactory;
    }
    protected boolean isOpenResource(HttpServletRequest httpRequest) {
        String spaceId = getSpaceId(httpRequest);
        if (null == spaceId) {
            return false;
        }

        return spaceId.equals("spaces")
            || spaceId.equals("stores")
            || spaceId.equals("acl")
            || spaceId.equals("init");
    }

    protected String getStoreId(HttpServletRequest httpRequest) {
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

    protected String getSpaceId(HttpServletRequest httpRequest) {
        String spaceId = httpRequest.getPathInfo();
        if (null == spaceId) {
            return null;
        }

        if (spaceId.startsWith("/")) {
            spaceId = spaceId.substring(1);
        }

        if (spaceId.startsWith("acl/")) {
            spaceId = spaceId.substring("acl/".length());
        }

        int slashIndex = spaceId.indexOf("/");
        if (slashIndex > 0) {
            spaceId = spaceId.substring(0, slashIndex);
        }
        return spaceId;
    }

    protected boolean hasContentId(HttpServletRequest httpRequest) {
        String spaceId = getSpaceId(httpRequest);
        if (null != spaceId) {
            String path = httpRequest.getPathInfo();
            return !path.endsWith(spaceId);
        }
        return false;
    }

    /**
     * This method returns the ACLs of the requested space, or an empty-map if
     * there is an error or for certain 'keyword' spaces, or null if the space
     * does not exist.
     *
     * @param request containing spaceId and storeId
     * @return ACLs, empty-map, or null
     */
    protected Map<String, AclType> getSpaceACLs(HttpServletRequest request) {
        Map<String, AclType> emptyACLs = new HashMap<String, AclType>();
        String storeId = getStoreId(request);
        String spaceId = getSpaceId(request);
        if (null == spaceId) {
            return emptyACLs;
        }

        if (spaceId.equals("security") || spaceId.equals("init")) {
            return emptyACLs;
        }

        StorageProvider store = getStorageProvider(storeId);
        if (null == store) {
            return emptyACLs;
        }

        try {
            return store.getSpaceACLs(spaceId);

        } catch (NotFoundException nfe) {
            log.info("Space !exist: {}, exception: {}", spaceId, nfe);
            return emptyACLs;

        } catch (StorageException e) {
            log.warn("Error getting space ACLs: {}, exception: {}", spaceId, e);
            return emptyACLs;
        }
    }

    protected HttpVerb getHttpVerb(HttpServletRequest httpRequest) {
        String method = httpRequest.getMethod();
        try {
            return HttpVerb.valueOf(method);

        } catch (RuntimeException e) {
            log.error("Error determining verb: {}, exception: {}", method, e);
            return null;
        }
    }

    protected List<String> getUserGroups(Authentication auth) {
        DuracloudUserDetails userDetails =
            (DuracloudUserDetails) auth.getPrincipal();
        return userDetails.getGroups();
    }

    protected boolean groupsHaveReadAccess(List<String> userGroups,
                                           Map<String, AclType> acls) {
        return groupsHaveAccess(userGroups, acls, true);
    }

    protected boolean groupsHaveWriteAccess(List<String> userGroups,
                                            Map<String, AclType> acls) {
        return groupsHaveAccess(userGroups, acls, false);
    }

    private boolean groupsHaveAccess(List<String> userGroups,
                                     Map<String, AclType> acls,
                                     boolean isRead) {
        if (null != userGroups) {
            for (String group : userGroups) {
                if (isRead && hasReadAccess(group, acls)) {
                    return true;

                } else if (!isRead && hasWriteAccess(group, acls)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean hasReadAccess(String name, Map<String, AclType> acls) {
        return hasAccess(name, acls, true);
    }

    protected boolean hasWriteAccess(String name, Map<String, AclType> acls) {
        return hasAccess(name, acls, false);
    }

    private boolean hasAccess(String name,
                              Map<String, AclType> acls,
                              boolean isRead) {
        if (RootUserCredential.getRootUsername().equals(name)) {
            return true;
        }

        if (null == acls) {
            return false;
        }

        String aclName = StorageProvider.PROPERTIES_SPACE_ACL + name;
        if (acls.containsKey(aclName)) {
            AclType acl = acls.get(aclName);
            if (isRead) {
                return AclType.READ.equals(acl) || AclType.WRITE.equals(acl);

            } else {
                return AclType.WRITE.equals(acl);
            }
        }
        return false;
    }

    protected boolean isAdmin(String name) {
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(name);
            
        } catch (UsernameNotFoundException e) {
            log.debug("Not admin: {}, error: {}", name, e);
            return false;
        }

        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method provides entry-point for alternate implementations of
     * StorageProvider.
     */
    protected StorageProvider getStorageProvider(String storeId) {
        return storageProviderFactory.getStorageProvider(storeId);
    }

    protected HttpServletRequest getHttpServletRequest(Object resource) {
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
