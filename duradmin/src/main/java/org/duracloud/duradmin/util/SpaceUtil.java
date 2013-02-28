/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.client.ContentStore;
import org.duracloud.common.model.AclType;
import org.duracloud.common.web.EncodeUtil;
import org.duracloud.domain.Content;
import org.duracloud.duradmin.domain.Acl;
import org.duracloud.duradmin.domain.ContentItem;
import org.duracloud.duradmin.domain.ContentProperties;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.duradmin.domain.SpaceProperties;
import org.duracloud.duradmin.security.RootAuthentication;
import org.duracloud.error.ContentStateException;
import org.duracloud.error.ContentStoreException;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.serviceapi.ServicesManager;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides utility methods for spaces.
 * 
 * @author Bill Branan
 */
public class SpaceUtil {


    public static Space populateSpace(Space space,
                                      org.duracloud.domain.Space cloudSpace, 
                                      ContentStore contentStore, 
                                      Authentication authentication) throws ContentStoreException {
        space.setSpaceId(cloudSpace.getId());
        space.setProperties(getSpaceProperties(cloudSpace.getProperties()));
        space.setExtendedProperties(cloudSpace.getProperties());
        space.setContents(cloudSpace.getContentIds());
        Map<String,AclType> spaceAcls = contentStore.getSpaceACLs(cloudSpace.getId());
        space.setAcls(toAclList(spaceAcls));
        AclType callerAcl = resolveCallerAcl(spaceAcls,  authentication);

        String aclName = null;
        if (null != callerAcl) {
            aclName = callerAcl.name();
        }
        space.setCallerAcl(aclName);
        return space;
    }

    private static SpaceProperties getSpaceProperties(Map<String, String> spaceProps) {
        SpaceProperties spaceProperties = new SpaceProperties();
        spaceProperties.setCreated(spaceProps.remove(ContentStore.SPACE_CREATED));
        spaceProperties.setCount(spaceProps.remove(ContentStore.SPACE_COUNT));
        spaceProperties.setSize(spaceProps.remove(ContentStore.SPACE_SIZE));
        spaceProperties.setTags(TagUtil.parseTags(spaceProps.remove(TagUtil.TAGS)));
        return spaceProperties;
    }

    public static void populateContentItem(String duradminBaseURL,
    									   ContentItem contentItem,
                                           String spaceId,
                                           String contentId,
                                           ContentStore store,
                                           ServicesManager servicesManager, 
                                           Authentication authentication)
            throws ContentStoreException {
    	contentItem.setSpaceId(spaceId);
    	contentItem.setContentId(contentId);
    	contentItem.setStoreId(store.getStoreId());
        Map<String, String> contentProperties =
                store.getContentProperties(spaceId, contentId);
        ContentProperties properties = populateContentProperties(contentProperties);
        contentItem.setProperties(properties);
        contentItem.setExtendedProperties(contentProperties);
        contentItem.setDurastoreURL(formatDurastoreURL(contentItem, store));
        Map<String,AclType> acls = store.getSpaceACLs(spaceId);
        contentItem.setAcls(toAclList(acls));
        AclType callerAcl = resolveCallerAcl(acls, authentication);

        
        String aclName = null;
        if (null != callerAcl) {
            aclName = callerAcl.name();
        }
        contentItem.setCallerAcl(aclName);
        contentItem.setImageViewerBaseURL(null);
    }

    /*
     * The use of this method is risky. As noted in this issue:
     * https://jira.duraspace.org/browse/DURACLOUD-708, when a call was being
     * made from populateContentItem() to this method, the user was often
     * seeing 500 errors due to a null authentication in the security context.
     * It appears that performing the authentication switch while other calls
     * were under way (likely via ajax) caused these errors to occur.
     */
    private static String resolveImageViewerBaseURL(ContentProperties properties,
                                  ServicesManager servicesManager) {
        if(!properties.getMimetype().startsWith("image")){
            return null;
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication auth = securityContext.getAuthentication();
        securityContext.setAuthentication(new RootAuthentication());
        
        try {
            return ServiceUtil.findImageServerUrlIfAvailable(servicesManager);
        } finally {
            securityContext.setAuthentication(auth);
        }
    }

    private static String formatDurastoreURL(ContentItem contentItem,ContentStore store) {
       	String pattern =  "{0}/{1}/{2}?storeID={3}";
        return MessageFormat.format(pattern,
                                    // NOTE: The https --> http swap is required by the Djatoka
                                    //       SimpleListResolver, see: http://sourceforge.net/apps/mediawiki/djatoka/index.php?title=Installation#Configure_a_Referent_Resolver
                                    //       https is not supported.
                                    store.getBaseURL().replace("https", "http"),
                                    contentItem.getSpaceId(),
                                    EncodeUtil.urlEncode(contentItem.getContentId()),
                                    store.getStoreId());
	}

    private static ContentProperties populateContentProperties(Map<String, String> contentProperties) {
        ContentProperties properties = new ContentProperties();
        properties
                .setMimetype(contentProperties.remove(ContentStore.CONTENT_MIMETYPE));
        properties.setSize(contentProperties.remove(ContentStore.CONTENT_SIZE));
        properties
                .setChecksum(contentProperties.remove(ContentStore.CONTENT_CHECKSUM));
        properties
                .setModified(contentProperties.remove(ContentStore.CONTENT_MODIFIED));
        properties.setTags(TagUtil.parseTags(contentProperties.remove(TagUtil.TAGS)));
       
        return properties;
    }
    
	public static void streamContent(ContentStore store, HttpServletResponse response, String spaceId, String contentId)
			throws ContentStoreException, IOException {
        OutputStream outStream = response.getOutputStream();
	    try{
	        Content c = store.getContent(spaceId, contentId);
	        Map<String,String> m = store.getContentProperties(spaceId, contentId);
	        response.setContentType(m.get(ContentStore.CONTENT_MIMETYPE));
	        response.setContentLength(Integer.parseInt(m.get(ContentStore.CONTENT_SIZE)));
	        InputStream is = c.getStream();
	        byte[] buf = new byte[1024];
	        int read = -1;
	        while((read = is.read(buf)) > 0){
	            outStream.write(buf, 0, read);
	        }
	        response.flushBuffer();
	        outStream.close();
	    }catch (ContentStoreException ex) {
	        if(ex.getCause() instanceof ContentStateException){
	            response.reset();
	            response.setStatus(HttpStatus.SC_CONFLICT);
                String message =
                    "The requested content item is currently in long-term storage" +
                    " with limited retrieval capability. Please contact " +
                    "DuraCloud support (https://wiki.duraspace.org/x/6gPNAQ) " +
                    "for assistance in retrieving this content item.";
                outStream.write(message.getBytes());
                outStream.close();
	        } else {
	            throw ex;
	        }
        }
	}

    
	public static AclType resolveCallerAcl(Map<String,AclType> acls,
                                          Authentication authentication)
        throws ContentStoreException {
        
        // check authorities
	    if(isAdmin(authentication)){
	        return AclType.WRITE;
	    }

        AclType callerAcl = null;

        DuracloudUserDetails details =
            (DuracloudUserDetails) authentication.getPrincipal();
        List<String> userGroups = details.getGroups();

        for (Map.Entry<String, AclType> e : acls.entrySet()) {
            AclType value = e.getValue();

            if (e.getKey().equals(details.getUsername())
                || userGroups.contains(e.getKey())) {
                callerAcl = value;
                if(AclType.WRITE.equals(callerAcl)){
                    break;
                }
            }
        }

        return callerAcl;
    }
	
    public static boolean isAdmin(Authentication authentication) {
        GrantedAuthority[] authorities = authentication.getAuthorities();
        for (GrantedAuthority a : authorities) {
            if (a.getAuthority().equals("ROLE_ADMIN")) {
                return true;
            }
        }
        return false;
    }

    public static List<Acl> toAclList(Map<String, AclType> spaceACLs) {
        List<Acl> acls = new LinkedList<Acl>();

        if (spaceACLs != null) {
            for (Map.Entry<String, AclType> entry : spaceACLs.entrySet()) {
                String key = entry.getKey();
                AclType value = entry.getValue();
                boolean read = false, write = false;
                if (value.equals(AclType.READ)) {
                    read = true;
                } else if (value.equals(AclType.WRITE)) {
                    read = true;
                    write = true;
                }

                acls.add(new Acl(key, formatAclDisplayName(key), read, write));
            }
        }

        sortAcls(acls);
        return acls;

    }
	
    private static final Comparator<Acl> ACL_COMPARATOR = new Comparator<Acl>() {
        private String groupPrefix = "group-";
        @Override
        public int compare(Acl o1, Acl o2) {
            if(o1.name.equals(o2.name)){
                return 0;
            }
            
            if(o1.isPublicGroup()){
                return -1;
            }

            if(o2.isPublicGroup()){
                return 1;
            }

            if(o1.name.startsWith(groupPrefix)){
                return !o2.name.startsWith(groupPrefix) ? -1 : o1.name.compareToIgnoreCase(o2.name);
            }else{
                return o2.name.startsWith(groupPrefix) ? 1 : o1.name.compareToIgnoreCase(o2.name);
            }
        }
        
    };
    
    public static void sortAcls(List<Acl> acls) {
        Collections.sort(acls, ACL_COMPARATOR);
    }

    public static String formatAclDisplayName(String key) {
        String prefix = "group-";
        if (key.startsWith(prefix)) {
            return key.replaceFirst(prefix, "");
        }

        return key;
    }


}
