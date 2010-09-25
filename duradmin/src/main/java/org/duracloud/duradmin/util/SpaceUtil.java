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
import java.text.MessageFormat;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ServicesManager;
import org.duracloud.common.web.EncodeUtil;
import org.duracloud.domain.Content;
import org.duracloud.duradmin.domain.ContentItem;
import org.duracloud.duradmin.domain.ContentMetadata;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.duradmin.domain.SpaceMetadata;
import org.duracloud.error.ContentStoreException;

/**
 * Provides utility methods for spaces.
 * 
 * @author Bill Branan
 */
public class SpaceUtil {


    public static Space populateSpace(Space space,
                                      org.duracloud.domain.Space cloudSpace) {
        space.setSpaceId(cloudSpace.getId());
        space.setMetadata(getSpaceMetadata(cloudSpace.getMetadata()));
        space.setExtendedMetadata(cloudSpace.getMetadata());
        space.setContents(cloudSpace.getContentIds());
        return space;
    }

    private static SpaceMetadata getSpaceMetadata(Map<String, String> spaceProps) {
        SpaceMetadata spaceMetadata = new SpaceMetadata();
        spaceMetadata.setCreated(spaceProps.get(ContentStore.SPACE_CREATED));
        spaceMetadata.setCount(spaceProps.get(ContentStore.SPACE_COUNT));
        spaceMetadata.setAccess(spaceProps.get(ContentStore.SPACE_ACCESS));
        spaceMetadata.setTags(TagUtil.parseTags(spaceProps.get(TagUtil.TAGS)));
        return spaceMetadata;
    }

    public static void populateContentItem(String duradminBaseURL,
    									   ContentItem contentItem,
                                           String spaceId,
                                           String contentId,
                                           ContentStore store,
                                           ServicesManager servicesManager)
            throws ContentStoreException {
    	contentItem.setSpaceId(spaceId);
    	contentItem.setContentId(contentId);
    	contentItem.setStoreId(store.getStoreId());
        Map<String, String> contentMetadata =
                store.getContentMetadata(spaceId, contentId);
        ContentMetadata metadata = populateContentMetadata(contentMetadata);
        contentItem.setMetadata(metadata);
        contentItem.setExtendedMetadata(contentMetadata);
        contentItem.setDurastoreURL(formatDurastoreURL(contentItem, store));
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

    private static ContentMetadata populateContentMetadata(Map<String, String> contentMetadata) {
        ContentMetadata metadata = new ContentMetadata();
        metadata
                .setMimetype(contentMetadata.get(ContentStore.CONTENT_MIMETYPE));
        metadata.setSize(contentMetadata.get(ContentStore.CONTENT_SIZE));
        metadata
                .setChecksum(contentMetadata.get(ContentStore.CONTENT_CHECKSUM));
        metadata
                .setModified(contentMetadata.get(ContentStore.CONTENT_MODIFIED));
        metadata.setTags(TagUtil.parseTags(contentMetadata.get(TagUtil.TAGS)));
       
        return metadata;
    }
    
	public static void streamContent(ContentStore store, HttpServletResponse response, String spaceId, String contentId)
			throws ContentStoreException, IOException {
		Content c = store.getContent(spaceId, contentId);
		Map<String,String> m = store.getContentMetadata(spaceId, contentId);
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

}
