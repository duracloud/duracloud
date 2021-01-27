/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.duracloud.client.ContentStore;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.model.AclType;
import org.duracloud.common.util.TagUtil;
import org.duracloud.common.web.EncodeUtil;
import org.duracloud.domain.Content;
import org.duracloud.duradmin.config.DuradminConfig;
import org.duracloud.duradmin.domain.Acl;
import org.duracloud.duradmin.domain.ContentItem;
import org.duracloud.duradmin.domain.ContentProperties;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.duradmin.domain.SpaceProperties;
import org.duracloud.error.ContentStateException;
import org.duracloud.error.ContentStoreException;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.storage.domain.StorageProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Provides utility methods for spaces.
 *
 * @author Bill Branan
 */
public class SpaceUtil {

    private static Logger log = LoggerFactory.getLogger(SpaceUtil.class);

    private SpaceUtil() {
        // Ensures no instances are made of this class, as there are only static members.
    }

    public static Space populateSpace(Space space,
                                      org.duracloud.domain.Space cloudSpace,
                                      ContentStore contentStore,
                                      Authentication authentication) throws ContentStoreException {
        space.setSpaceId(cloudSpace.getId());
        space.setProperties(getSpaceProperties(cloudSpace.getProperties()));
        space.setHlsEnabled(StringUtils.isNotBlank(space.getProperties().getHlsStreamingHost()));

        space.setExtendedProperties(cloudSpace.getProperties());
        space.setContents(cloudSpace.getContentIds());
        Map<String, AclType> spaceAcls = contentStore.getSpaceACLs(cloudSpace.getId());
        space.setAcls(toAclList(spaceAcls));

        if (isAdmin(authentication)
            && isSnapshotProvider(contentStore)
            && isSnapshotInProgress(contentStore, space.getSpaceId())) {
            space.setSnapshotInProgress(true);
        }

        AclType callerAcl = resolveCallerAcl(space.getSpaceId(),
                                             contentStore,
                                             spaceAcls,
                                             authentication,
                                             space.isSnapshotInProgress());

        String aclName = null;
        if (null != callerAcl) {
            aclName = callerAcl.name();
        }
        space.setCallerAcl(aclName);

        space.setMillDbEnabled(DuradminConfig.isMillDbEnabled());

        return space;
    }

    private static SpaceProperties getSpaceProperties(Map<String, String> spaceProps) {
        SpaceProperties spaceProperties = new SpaceProperties();
        spaceProperties.setCreated(spaceProps.remove(ContentStore.SPACE_CREATED));
        spaceProperties.setCount(spaceProps.remove(ContentStore.SPACE_COUNT));
        spaceProperties.setSize(spaceProps.remove(ContentStore.SPACE_SIZE));
        spaceProperties.setTags(TagUtil.parseTags(spaceProps.remove(TagUtil.TAGS)));
        spaceProperties.setHlsStreamingHost(spaceProps.get(ContentStore.HLS_STREAMING_HOST));
        spaceProperties.setHlsStreamingType(spaceProps.get(ContentStore.HLS_STREAMING_TYPE));

        spaceProperties.setSnapshotId(spaceProps.get(Constants.SNAPSHOT_ID_PROP));

        String restoreId = spaceProps.get(Constants.RESTORE_ID_PROP);
        if (StringUtils.isNotBlank(restoreId)) {
            spaceProperties.setRestoreId(restoreId);
        }

        return spaceProperties;
    }

    public static void populateContentItem(String duradminBaseURL,
                                           ContentItem contentItem,
                                           String spaceId,
                                           String contentId,
                                           ContentStore store,
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
        Map<String, AclType> acls = store.getSpaceACLs(spaceId);
        contentItem.setAcls(toAclList(acls));
        AclType callerAcl = resolveCallerAcl(spaceId, store, acls, authentication);

        String aclName = null;
        if (null != callerAcl) {
            aclName = callerAcl.name();
        }
        contentItem.setCallerAcl(aclName);
        contentItem.setImageViewerBaseURL(null);
    }

    private static String formatDurastoreURL(ContentItem contentItem, ContentStore store) {
        String pattern = "{0}/{1}/{2}?storeID={3}";
        return MessageFormat.format(pattern,
                                    // NOTE: The https --> http swap is required by the Djatoka SimpleListResolver,
                                    //       see: http://sourceforge.net/apps/mediawiki/djatoka/index.php?title=Installation#Configure_a_Referent_Resolver
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
        Map<String, String> m = store.getContentProperties(spaceId, contentId);
        String mimetype = m.get(ContentStore.CONTENT_MIMETYPE);
        String contentLength = m.get(ContentStore.CONTENT_SIZE);
        try (InputStream is = c.getStream()) {
            streamToResponse(is, response, mimetype, contentLength);
        }
    }

    public static void streamToResponse(InputStream is,
                                        HttpServletResponse response,
                                        String mimetype,
                                        String contentLength)
        throws ContentStoreException, IOException {

        OutputStream outStream = response.getOutputStream();

        try {
            response.setContentType(mimetype);

            if (contentLength != null) {
                response.setContentLengthLong(Long.parseLong(contentLength));
            }
            byte[] buf = new byte[1024];
            int read = -1;
            while ((read = is.read(buf)) > 0) {
                outStream.write(buf, 0, read);
            }

            response.flushBuffer();
        } catch (Exception ex) {
            if (ex.getCause() instanceof ContentStateException) {
                response.reset();
                response.setStatus(HttpStatus.SC_CONFLICT);
                String message =
                    "The requested content item is currently in long-term storage" +
                    " with limited retrieval capability. Please contact " +
                    "DuraCloud support (https://wiki.duraspace.org/x/6gPNAQ) " +
                    "for assistance in retrieving this content item.";
                //It is necessary to pad the message in order to force Internet Explorer to
                //display the server sent text rather than display the browser default error message.
                //If the message is less than 512 bytes, the browser will ignore the message.
                //c.f. http://support.microsoft.com/kb/294807
                message += StringUtils.repeat(" ", 512);
                outStream.write(message.getBytes());
            } else {
                throw ex;
            }
        } finally {
            try {
                outStream.close();
            } catch (Exception e) {
                log.warn("failed to close outputstream ( " + outStream + "): message=" + e.getMessage(), e);
            }
        }
    }

    public static AclType resolveCallerAcl(String spaceId, ContentStore store, Map<String, AclType> acls,
                                           Authentication authentication)
        throws ContentStoreException {
        return resolveCallerAcl(spaceId, store, acls, authentication, null);
    }

    public static AclType resolveCallerAcl(String spaceId, ContentStore store, Map<String, AclType> acls,
                                           Authentication authentication, Boolean snapshotInProgress)
        throws ContentStoreException {
        //if a snapshot is in progress, read only
        // check authorities
        if (isRoot(authentication)) {
            return AclType.WRITE;
        }

        if (snapshotInProgress == null) {
            snapshotInProgress = false;
            if (isSnapshotProvider(store)) {
                snapshotInProgress = isSnapshotInProgress(store, spaceId);
            }
        }

        if (spaceId.equals(Constants.SNAPSHOT_METADATA_SPACE)) {
            return AclType.READ;
        }

        if (snapshotInProgress) {
            return AclType.READ;
        }
        // check authorities
        if (isAdmin(authentication)) {
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
                if (AclType.WRITE.equals(callerAcl)) {
                    break;
                }
            }
        }

        return callerAcl;
    }

    private static boolean isSnapshotInProgress(ContentStore store,
                                                String spaceId)
        throws ContentStoreException {
        return store.getSpaceProperties(spaceId)
                    .containsKey(Constants.SNAPSHOT_ID_PROP);
    }

    protected static boolean isSnapshotProvider(ContentStore store) {
        String providerType = store.getStorageProviderType();
        return providerType.equals(StorageProviderType.CHRONOPOLIS.name());
    }

    public static boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADMIN");
    }

    private static boolean isRoot(Authentication authentication) {
        return hasRole(authentication, "ROLE_ROOT");
    }

    protected static boolean hasRole(Authentication authentication, String role) {
        Collection authorities = authentication.getAuthorities();
        for (Object a : authorities) {
            if (((GrantedAuthority) a).getAuthority().equals(role)) {
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
                boolean read = false;
                boolean write = false;
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
            if (o1.name.equals(o2.name)) {
                return 0;
            }

            if (o1.isPublicGroup()) {
                return -1;
            }

            if (o2.isPublicGroup()) {
                return 1;
            }

            if (o1.name.startsWith(groupPrefix)) {
                return !o2.name.startsWith(groupPrefix) ? -1 : o1.name.compareToIgnoreCase(o2.name);
            } else {
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
