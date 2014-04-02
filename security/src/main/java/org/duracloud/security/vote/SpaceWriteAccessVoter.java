/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import static org.duracloud.security.vote.VoterUtil.debugText;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.duracloud.common.constant.Constants;
import org.duracloud.common.model.AclType;
import org.duracloud.security.domain.HttpVerb;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * This class decides if a caller has WRITE access to a given resource. If the
 * caller is seeking READ access to this resource, this class abstains from
 * casting a vote.
 *
 * @author Andrew Woods
 *         Date: 11/18/11
 */
public class SpaceWriteAccessVoter extends SpaceAccessVoter {

    private final Logger log =
        LoggerFactory.getLogger(SpaceReadAccessVoter.class);

    public SpaceWriteAccessVoter(StorageProviderFactory storageProviderFactory,
                                 UserDetailsService userDetailsService) {
        super(storageProviderFactory, userDetailsService);
    }

    /**
     * This method checks the ACL state of the arg resource
     * (space and provider) and denies access to principals if they are
     * anonymous or if they do not have a WRITE ACL for the space.
     *
     * @param auth     principal seeking AuthZ
     * @param resource that is under protection
     * @param config   access-attributes defined on resource
     * @return vote (AccessDecisionVoter.ACCESS_GRANTED, ACCESS_DENIED, ACCESS_ABSTAIN)
     */
    public int vote(Authentication auth,
                    Object resource,
                    Collection config) {
        String label = "SpaceWriteAccessVoterImpl";
        if (resource != null && !supports(resource.getClass())) {
            log.debug(debugText(label, auth, config, resource, ACCESS_ABSTAIN));
            return ACCESS_ABSTAIN;
        }

        HttpServletRequest httpRequest = getHttpServletRequest(resource);
        if (null == httpRequest) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        HttpVerb verb = getHttpVerb(httpRequest);
        if (null == verb) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        // This class only handles HTTP write verbs.
        if (verb.isRead()) {
            log.debug(debugText(label, auth, config, resource, ACCESS_ABSTAIN));
            return ACCESS_ABSTAIN;
        }

        // Anonymous users can not WRITE spaces.
        if (auth instanceof AnonymousAuthenticationToken) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        // Root users always have access
        if (isRoot(auth)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }

        if (isSnapshotInProgress(httpRequest)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        // The Admin always has WRITE access.
        if (isAdmin(auth.getName())) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }

        
        // Since not an Admin, DENY permission to create spaces.
        if (isSpaceCreation(httpRequest)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        // Since not an Admin, DENY permission to delete spaces.
        if (isSpaceDeletion(httpRequest)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        // Since not an Admin, DENY permission to update space ACLs.
        if (isSpaceAclUpdate(httpRequest)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        Map<String, AclType> acls = getSpaceACLs(httpRequest);
        if (hasWriteAccess(auth.getName(), acls)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }

        List<String> userGroups = getUserGroups(auth);
        if (groupsHaveWriteAccess(userGroups, acls)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }

        int grant = ACCESS_DENIED;
        log.debug(debugText(label, auth, config, resource, grant));
        return grant;
    }

    private boolean isRoot(Authentication auth) {
        for(GrantedAuthority g : auth.getAuthorities()){
            if(g.getAuthority().equals("ROLE_ROOT")){
                return true;
            }
        }
        
        return false;
    }

    private boolean isSnapshotInProgress(HttpServletRequest httpRequest) {
       String storeId = getStoreId(httpRequest);
       StorageProviderFactory factory = getStorageProviderFactory();
       List<StorageAccount> accounts = factory.getStorageAccounts();
       if(storeId == null){
           for(StorageAccount account : accounts ){
               if(account.isPrimary()){
                   storeId = account.getId();
                   break;
               }
           }
       }

       for(StorageAccount account : accounts){
           if(account.getId().equals(storeId)){
               if(account.getType().equals(StorageProviderType.CHRON_STAGE)){
                   StorageProvider store = factory.getStorageProvider(storeId);
                   try{
                       String spaceId = getSpaceId(httpRequest);
                       store.getContentProperties(spaceId, Constants.SNAPSHOT_ID);
                       return true;
                   }catch(NotFoundException ex){}
                   break;
               }
           }
       }
       
       return false;
    }

    private boolean isSpaceCreation(HttpServletRequest httpRequest) {
        if (HttpVerb.PUT.equals(getHttpVerb(httpRequest))) {
            return !hasContentId(httpRequest);
        }
        return false;
    }

    private boolean isSpaceDeletion(HttpServletRequest httpRequest) {
        if (HttpVerb.DELETE.equals(getHttpVerb(httpRequest))) {
            return !hasContentId(httpRequest);
        }
        return false;
    }

    private boolean isSpaceAclUpdate(HttpServletRequest httpRequest) {
        if (HttpVerb.POST.equals(getHttpVerb(httpRequest))) {
            String path = httpRequest.getPathInfo();
            return path.startsWith("/acl/") || path.startsWith("acl/");
        }
        return false;
    }

}
