/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import org.duracloud.client.ContentStore;
import org.duracloud.common.model.AclType;
import org.duracloud.common.web.EncodeUtil;
import org.duracloud.domain.Content;
import org.duracloud.duradmin.domain.ContentItem;
import org.duracloud.duradmin.domain.ContentProperties;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.duradmin.domain.SpaceProperties;
import org.duracloud.error.ContentStoreException;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.serviceapi.ServicesManager;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
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
        AclType callerAcl = resolveCallerAcl(contentStore, space.getSpaceId(), authentication);

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
        spaceProperties.setAccess(spaceProps.remove(ContentStore.SPACE_ACCESS));
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
        AclType callerAcl = resolveCallerAcl(store, spaceId, authentication);

        String aclName = null;
        if (null != callerAcl) {
            aclName = callerAcl.name();
        }
        contentItem.setCallerAcl(aclName);
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
		Content c = store.getContent(spaceId, contentId);
		Map<String,String> m = store.getContentProperties(spaceId, contentId);
		response.setContentType(m.get(ContentStore.CONTENT_MIMETYPE));
		response.setContentLength(Integer.parseInt(m.get(ContentStore.CONTENT_SIZE)));
		InputStream is = c.getStream();
		byte[] buf = new byte[1024];
		int read = -1;
		while((read = is.read(buf)) > 0){
			response.getOutputStream().write(buf, 0, read);
		}
		response.flushBuffer();
		response.getOutputStream().close();
	}

	public static AclType resolveCallerAcl(ContentStore contentStore,
                                          String spaceId,
                                          Authentication authentication)
        throws ContentStoreException {
        
        // check authorities
        GrantedAuthority[] authorities = authentication.getAuthorities();
        for (GrantedAuthority a : authorities) {
            if (a.getAuthority().equals("ROLE_ADMIN")) {
                //no need to make any further calls.
                return AclType.WRITE;
            }
        }

        Map<String, AclType> acls = contentStore.getSpaceACLs(spaceId);
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

}
