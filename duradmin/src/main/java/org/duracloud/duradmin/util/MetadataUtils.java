/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class MetadataUtils {

    static final String NAME_KEY_PREFIX = "ext-metadata-";
    private static Logger log = LoggerFactory.getLogger(MetadataUtils.class);
    
    
    
    
    public static void setMetadata(ContentStore contentStore,
                                   String spaceId,
                                   String contentId,
                                   Map<String, String> metadata)
            throws ContentStoreException {
        if (StringUtils.hasText(contentId)) {
            log.info("on contentStore[" + contentStore.getStoreId() + "]: " +
            		"setting content metadata: spaceId="+spaceId+", " +
            				"contentId="+ contentId + ", metadata="+ metadata);
            
            contentStore.setContentMetadata(spaceId, contentId, metadata);
            
        } else {
            log.info("on contentStore[" + contentStore.getStoreId() + "]: " +
            		"setting space metadata: spaceId="+spaceId+", " +
            				"metadata="+ metadata);
            
            contentStore.setSpaceMetadata(spaceId, metadata);
        }

    }

    public static Map<String, String> getMetadata(ContentStore contentStore,
                                                  String spaceId,
                                                  String contentId)
            throws ContentStoreException {
        if (StringUtils.hasText(contentId)) {

            Map<String,String> metadata = contentStore.getContentMetadata(spaceId, contentId);
            log.info("from contentStore[" + contentStore.getStoreId() + "]: " +
                     "getting content metadata: spaceId="+spaceId+", " +
                             "contentId="+ contentId + ", metadata="+ metadata);
             return metadata;
        } else {

            Map<String,String> metadata = contentStore.getSpaceMetadata(spaceId);
            log.info("from contentStore[" + contentStore.getStoreId() + "]: " +
                     "getting space metadata: spaceId="+spaceId+", " +
                             "contentId="+ contentId + ", metadata="+ metadata);
             return metadata;
        }
    }

    public static List<NameValuePair> convertExtendedMetadata(Map<String, String> metadata) {
        List<NameValuePair> extendedMetadata = new LinkedList<NameValuePair>();
        if (extendedMetadata != null) {
            for (String name : metadata.keySet()) {
                if (name.startsWith(NAME_KEY_PREFIX)) {
                    extendedMetadata.add(new NameValuePair(name
                            .substring(NAME_KEY_PREFIX.length()), metadata
                            .get(name)));
                }
            }
        }

        return extendedMetadata;
    }

    public static Object remove(String name, Map<String, String> metadata) {
        return metadata.remove(MetadataUtils.NAME_KEY_PREFIX + name);
    }

    public static Object add(String name,
                             String value,
                             Map<String, String> metadata) {
        return metadata.put(NAME_KEY_PREFIX + name, value);

    }

    public static Object getValue(String key, Map<String, String> metadata) {
        return metadata.get(NAME_KEY_PREFIX+key);
    }

	public static void handle(String method, String context, Map<String, String> metadata,
			HttpServletRequest request) {
    	String tag = request.getParameter("tag");
    	String name = request.getParameter("metadata-name");
    	String value = request.getParameter("metadata-value");
    	if(method.equals("addTag")){
        	TagUtil.addTag(tag, metadata);
        	log.info("added tag [{}] to [{}]", tag, context);
        }else if(method.equals("removeTag")){
        	TagUtil.removeTag(tag, metadata);
        	log.info("removed tag [{}] from [{}]", tag, context);
        }else if(method.equals("addMetadata")){
        	MetadataUtils.add(name, value, metadata);
        	log.info("added metadata [{}] to  [{}]", name+":"+value,context);
        }else if(method.equals("removeMetadata")){
        	MetadataUtils.remove(name,metadata);
        	log.info("removed metadata [{}] from  [{}]", name+":"+value,context);
        }else{
        	log.warn("unexpected method parameter: " + method);
        }
	}

}
