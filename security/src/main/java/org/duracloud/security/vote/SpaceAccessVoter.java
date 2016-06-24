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

import org.duracloud.common.constant.Constants;
import org.duracloud.common.model.AclType;
import org.duracloud.security.domain.HttpVerb;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.security.util.AuthorizationHelper;
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

    private UserDetailsService userDetailsService;
    private AuthorizationHelper authHelper;
    private StorageProviderFactory storageProviderFactory;
    private static String[] EXCEPTIONAL_PATH_PREFIXES =
        { "/manifest/", "/bit-integrity/", "/report/space/" };

    public SpaceAccessVoter(StorageProviderFactory storageProviderFactory,
                            UserDetailsService userDetailsService) {
        this.storageProviderFactory = storageProviderFactory;
        this.userDetailsService = userDetailsService;
        this.authHelper = new AuthorizationHelper(storageProviderFactory);
    }


    protected boolean isOpenResource(HttpServletRequest httpRequest) {
        String spaceId = getSpaceId(httpRequest);
        if (null == spaceId) {
            return false;
        }

        return spaceId.equals("spaces")
            || spaceId.equals("stores")
            || spaceId.equals("acl")
            || spaceId.equals("task");

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

        return extractSpaceId(spaceId);
    }


    protected String extractSpaceId(String pathInfo) {

        
        for(String prefix : EXCEPTIONAL_PATH_PREFIXES){
            if(pathInfo.startsWith(prefix)){
                return pathInfo.substring(prefix.length());
            }
        }

        String spaceId = pathInfo;
        if (spaceId.startsWith("/")) {
            spaceId = pathInfo.substring(1);
        }

        int slashIndex = spaceId.indexOf("/");
        if (slashIndex > 0) {
            spaceId = spaceId.substring(0, slashIndex);
        }
        return spaceId;
    }

    protected boolean hasContentId(HttpServletRequest httpRequest) {
        return getContentId(httpRequest) != null;
    }

    protected String getContentId(HttpServletRequest httpRequest) {
        String spaceId = getSpaceId(httpRequest);
        if (null != spaceId) {
            String path = httpRequest.getPathInfo();
            if(!path.endsWith(spaceId)){
                return path.substring(path.indexOf(spaceId)+spaceId.length()+1);
            }
        }
        return null;
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
        String storeId = getStoreId(request);
        String spaceId = getSpaceId(request);
        return getSpaceACLs(storeId, spaceId);
    }


    protected Map<String, AclType> getSpaceACLs(String storeId,
                                                String spaceId) {
        return this.authHelper.getSpaceACLs(storeId, spaceId);
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
        return this.authHelper.groupsHaveReadAccess(userGroups, acls);
    }

    protected boolean groupsHaveWriteAccess(List<String> userGroups,
                                            Map<String, AclType> acls) {
        return this.authHelper.groupsHaveAccess(userGroups, acls, false);
    }

    protected boolean hasReadAccess(String name, Map<String, AclType> acls) {
        return this.authHelper.hasReadAccess(name, acls);
    }

    protected boolean hasWriteAccess(String name, Map<String, AclType> acls) {
        return this.authHelper.hasWriteAccess(name, acls);
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

    public StorageProviderFactory getStorageProviderFactory() {
        return storageProviderFactory;
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


    protected boolean isSnapshotMetadataSpace(HttpServletRequest httpRequest) {
        String spaceId = getSpaceId(httpRequest);
        return(Constants.SNAPSHOT_METADATA_SPACE.equals(spaceId));
    }

}
