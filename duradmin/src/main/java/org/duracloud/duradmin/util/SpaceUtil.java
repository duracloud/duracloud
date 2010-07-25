/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ServicesManager;
import org.duracloud.common.web.EncodeUtil;
import org.duracloud.duradmin.domain.ContentItem;
import org.duracloud.duradmin.domain.ContentMetadata;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.duradmin.domain.SpaceMetadata;
import org.duracloud.error.ContentStoreException;
import org.duracloud.serviceconfig.ServiceInfo;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Provides utility methods for spaces.
 * 
 * @author Bill Branan
 */
public class SpaceUtil {

    private static final String IMAGE_J2K_MIME_TYPE = "image/jp2";

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
        populateURLs(duradminBaseURL,contentItem, store, servicesManager);
        
    }

    private static void populateURLs(String duradminBaseURL,
    								ContentItem contentItem,
                                     ContentStore store,
                                     ServicesManager servicesManager) {
        String j2KBaseURL = null;
        j2KBaseURL = resolveJ2KServiceBaseURL(servicesManager);
    	contentItem.setTinyThumbnailURL(formatThumbnail(duradminBaseURL, contentItem, store, j2KBaseURL, 1));
        contentItem.setThumbnailURL(formatThumbnail(duradminBaseURL, contentItem, store, j2KBaseURL, 2));
        contentItem.setViewerURL(formatViewerURL(duradminBaseURL, contentItem, store, j2KBaseURL));
        contentItem.setDownloadURL(formatDownloadURL(duradminBaseURL, contentItem,store,true));
    }

    



	private static String formatViewerURL(String duradminBaseURL, ContentItem contentItem, ContentStore store, String j2KBaseURL) {
         if(j2KBaseURL !=null && contentItem.getMetadata().getMimetype().startsWith("image/")){
             return MessageFormat.format("{0}/viewer.html?rft_id={1}", 
                     j2KBaseURL, 
                     EncodeUtil.urlEncode(formatDurastoreURL(contentItem,store)));
         }else{
        	 return formatDownloadURL(duradminBaseURL, contentItem,store,false);
             
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


	private static String[] GENERIC_THUMBNAIL_PREFIXES = {"image", "video", "text", "pdf"}; 
    private static String formatThumbnail(String duradminBaseURL,ContentItem contentItem, ContentStore store, String j2KBaseURL, int size) {
        String mimetype = contentItem.getMetadata().getMimetype();
        if(mimetype.toLowerCase().startsWith("image/") && j2KBaseURL != null){
	    	String pattern = "{0}/resolver?url_ver=Z39.88-2004&rft_id={1}&" + 
	                            "svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&" +
	                            "svc.format=image/png&svc.level={2}&svc.rotate=0&svc.region=0,0,500,500";
	
	        return MessageFormat.format(
	                                    pattern, 
	                                    j2KBaseURL,
	                                    EncodeUtil.urlEncode(formatDurastoreURL(contentItem, store)),
	                                    size);
        }else{
        	for(String gtf : GENERIC_THUMBNAIL_PREFIXES){
        		if(mimetype.startsWith(gtf)){
        			return duradminBaseURL+"/images/generic-thumb-" + gtf + ".png";
        		}
        	}
        	
        	return duradminBaseURL+"/images/generic-thumb-other.png";
        }
        
        
    }

    private static String formatDownloadURL(String duradminBaseURL, String spaceId, String contentId, String storeId, boolean asAttachment) {
       	String pattern = "{0}/download/contentItem?spaceId={1}&contentId={2}&storeID={3}&attachment={4}";
        return MessageFormat.format(pattern,
        							duradminBaseURL,
                                    spaceId,
                                    EncodeUtil.urlEncode(contentId),
                                    storeId,
                                    asAttachment);
    }
    
    public static String formatDownloadURL(String duradminBaseURL, ContentItem contentItem, ContentStore store, boolean asAttachment) {
    	return formatDownloadURL(duradminBaseURL, contentItem.getSpaceId(), contentItem.getContentId(), store.getStoreId(), asAttachment);
     }

    /**
     * Returns the j2k service base URL if the service is running
     * @return null if the J2K Service is not running
     */
    public static String resolveJ2KServiceBaseURL(ServicesManager servicesManager) {
        int deploymentId;
        Map<String, String> props;
        try {
            List<ServiceInfo> serviceInfos = servicesManager.getDeployedServices();
            for (ServiceInfo serviceInfo : serviceInfos) {

                if (serviceInfo.getContentId().toLowerCase().contains("j2k") &&
                    serviceInfo.getDeploymentCount() > 0) {

                    deploymentId = serviceInfo.getDeployments().get(0).getId();
                    props = servicesManager.getDeployedServiceProps(serviceInfo.getId(),
                                                                    deploymentId);

                    return props.get("url");
                }
            }

            return null;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        
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
}
