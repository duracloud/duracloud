/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.duracloud.common.model.AclType;
import org.duracloud.common.model.RootUserCredential;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
/**
 * A helper class that performs common Authorization related functions shared by different 
 * system layers.
 * @author Daniel Bernstein
 *
 */
public class AuthorizationHelper {
    private final Logger log =
        LoggerFactory.getLogger(AuthorizationHelper.class);

    private StorageProviderFactory storageProviderFactory;

    public AuthorizationHelper(StorageProviderFactory storageProviderFactory) {
        this.storageProviderFactory = storageProviderFactory;
    }
    
    public List<String> getUserGroups(Authentication auth) {
        DuracloudUserDetails userDetails = getUserDetails(auth);
        return userDetails.getGroups();
    }

    protected DuracloudUserDetails getUserDetails(Authentication auth) {
        DuracloudUserDetails userDetails =
            (DuracloudUserDetails) auth.getPrincipal();
        return userDetails;
    }

    public Collection<GrantedAuthority> getAuthorities(Authentication auth) {
        DuracloudUserDetails userDetails = getUserDetails(auth);
        return userDetails.getAuthorities();
    }

    public boolean groupsHaveReadAccess(Authentication auth,
                                        Map<String, AclType> acls) {
        return groupsHaveAccess(getUserGroups(auth), acls, true);
    }

    public boolean groupsHaveReadAccess(List<String> userGroups,
                                           Map<String, AclType> acls) {
        return groupsHaveAccess(userGroups, acls, true);
    }

    public boolean groupsHaveWriteAccess(List<String> userGroups,
                                            Map<String, AclType> acls) {
        return groupsHaveAccess(userGroups, acls, false);
    }

    public boolean groupsHaveAccess(List<String> userGroups,
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

    public boolean hasReadAccess(String name, Map<String, AclType> acls) {
        return hasAccess(name, acls, true);
    }

    public boolean hasWriteAccess(String name, Map<String, AclType> acls) {
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
    
    public boolean hasRole(Authentication auth, String role) {
        Collection<GrantedAuthority> authorities = getAuthorities(auth);
        return hasRole(role, authorities);
    }
    
    public boolean hasAdmin(Authentication auth){
        return hasRole(auth, "ROLE_ADMIN");
    }

    public boolean hasRole(String role,
                              Collection<GrantedAuthority> authorities) {
        for (GrantedAuthority authority : authorities) {
            if (role.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
    
    public Map<String, AclType> getSpaceACLs(String storeId,
                                                String spaceId) {

        Map<String, AclType> emptyACLs = new HashMap<String, AclType>();
        if (null == spaceId) {
            return emptyACLs;
        }

        if (spaceId.equals("security") || spaceId.equals("init")) {
            return emptyACLs;
        }

        StorageProvider store = storageProviderFactory.getStorageProvider(storeId);
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
}
