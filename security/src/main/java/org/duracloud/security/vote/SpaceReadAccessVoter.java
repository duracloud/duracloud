/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import static org.duracloud.security.vote.VoterUtil.*;

import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.duracloud.common.model.AclType;
import org.duracloud.security.domain.HttpVerb;
import org.duracloud.snapshot.id.SnapshotIdentifier;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.CollectionUtils;

/**
 * This class decides if a caller has READ access to a given resource. If the
 * caller is seeking WRITE access to this resource, this class abstains from
 * casting a vote.
 *
 * @author Andrew Woods
 *         Date: 11/18/11
 */
public class SpaceReadAccessVoter extends SpaceAccessVoter {

    private final Logger log =
        LoggerFactory.getLogger(SpaceReadAccessVoter.class);
    
    private List<String> pathExemptions = null;
    public SpaceReadAccessVoter(StorageProviderFactory storageProviderFactory,
                                UserDetailsService userDetailsService) {
        this(storageProviderFactory,
             userDetailsService,
             new LinkedList<String>());
    }
    /**
     * 
     * @param storageProviderFactory
     * @param userDetailsService
     * @param pathExemptions A list of regular expressions designating path info strings allowable for users.
     */
    public SpaceReadAccessVoter(StorageProviderFactory storageProviderFactory,
                                UserDetailsService userDetailsService, List<String> pathExemptions) {
        super(storageProviderFactory, userDetailsService);
        this.pathExemptions = pathExemptions;
    }

    /**
     * This method checks the Access and ACL state of the arg resource
     * (space and provider) and denies access to principals if they are
     * anonymous and the space is CLOSED, or if they do not have a READ ACL for
     * the space.
     *
     * @param auth     principal seeking AuthZ
     * @param resource that is under protection
     * @param config   access-attributes defined on resource
     * @return vote (AccessDecisionVoter.ACCESS_GRANTED, ACCESS_DENIED, ACCESS_ABSTAIN)
     */
    public int vote(Authentication auth,
                    Object resource,
                    Collection config) {
        String label = "SpaceReadAccessVoterImpl";
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

        // This class only handles HTTP read verbs.
        if (!verb.isRead()) {
            log.debug(debugText(label, auth, config, resource, ACCESS_ABSTAIN));
            return ACCESS_ABSTAIN;
        }

        // The Admin always has READ access.
        if (isAdmin(auth.getName())) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }

        if (isOpenResource(httpRequest)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }
        
        
        Map<String, AclType> acls = getSpaceACLs(httpRequest);
        // All READs on PUBLIC spaces are granted.
        if (acls.containsKey(StorageProvider.PROPERTIES_SPACE_ACL_PUBLIC)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }

        // Anonymous users can not READ 'closed' spaces.
        if (auth instanceof AnonymousAuthenticationToken) {
            log.debug(debugText(label, auth, config, resource, ACCESS_DENIED));
            return ACCESS_DENIED;
        }

        String username = auth.getName();
        if (hasReadAccess(username, acls)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }

        List<String> userGroups = getUserGroups(auth);
        if (groupsHaveReadAccess(userGroups, acls)) {
            log.debug(debugText(label, auth, config, resource, ACCESS_GRANTED));
            return ACCESS_GRANTED;
        }
        
        //allow users to read snapshot metadata files for snapshots to which the 
        //user has access
        if(isSnapshotMetadataSpace(httpRequest) && hasContentId(httpRequest)){
            return hasSnapshotSpacePermissions(httpRequest,
                                               username,
                                               userGroups)
                                                   ? ACCESS_GRANTED
                                                   : ACCESS_DENIED;
        }

        //special case allowing configurable file patterns to be exempted 
        if(matchesPathExemptions(httpRequest)){
          return ACCESS_GRANTED;
        }
        
        int grant = ACCESS_DENIED;
        log.debug(debugText(label, auth, config, resource, grant));
        return grant;
    }

    private boolean hasSnapshotSpacePermissions(HttpServletRequest httpRequest, String username, List<String> userGroups) {
        String contentId = getContentId(httpRequest);
        String metadataSuffix = ".zip";
        if(contentId != null && contentId.endsWith(metadataSuffix)){
            try {
                String snapshotId =
                    contentId.substring(0,
                                        contentId.indexOf(metadataSuffix));
                SnapshotIdentifier identifier =
                    SnapshotIdentifier.parseSnapshotId(snapshotId);
                String spaceId = identifier.getSpaceId();
                Map<String, AclType> snapshotSpaceAcls =
                    getSpaceACLs(getStoreId(httpRequest), spaceId);
                if(hasReadAccess(username, snapshotSpaceAcls) || 
                    groupsHaveReadAccess(userGroups, snapshotSpaceAcls)){
                    return true;
                }
            } catch (ParseException e) {
                log.error("unable to parse snapshot metadata content id : "
                          + contentId + ": " + e.getMessage(), e);
            }
        }else{
            log.error("snapshot metadata content id did not end in '.zip' as expected.");
        }
        
        return false;
    }

    private boolean matchesPathExemptions(HttpServletRequest httpRequest) {
        String path = httpRequest.getPathInfo();
        
        if(!CollectionUtils.isEmpty(this.pathExemptions)){
            for(String pattern : this.pathExemptions){
                if(path.matches(pattern)){
                    return true;
                }
            }
        }
        return false;
       
    }
}
