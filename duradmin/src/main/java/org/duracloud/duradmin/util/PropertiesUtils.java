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
import org.duracloud.common.util.TagUtil;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class PropertiesUtils {

    private static Logger log = LoggerFactory.getLogger(PropertiesUtils.class);
    
    
    
    
    public static void setProperties(ContentStore contentStore,
                                     String spaceId,
                                     String contentId,
                                     Map<String, String> properties)
            throws ContentStoreException {
        if (StringUtils.hasText(contentId)) {
            log.info("on contentStore[" + contentStore.getStoreId() + "]: " +
            		 "setting content properties: spaceId="+spaceId+", " +
            	     "contentId="+ contentId + ", properties="+ properties);
            
            contentStore.setContentProperties(spaceId, contentId, properties);
        }
    }

    public static Map<String, String> getProperties(ContentStore contentStore,
                                                    String spaceId,
                                                    String contentId)
            throws ContentStoreException {
        if (StringUtils.hasText(contentId)) {

            Map<String,String> properties =
                contentStore.getContentProperties(spaceId, contentId);
            log.info("from contentStore[" + contentStore.getStoreId() + "]: " +
                     "getting content properties: spaceId="+spaceId+", " +
                     "contentId="+ contentId + ", properties="+ properties);
             return properties;
        } else {

            Map<String,String> properties =
                contentStore.getSpaceProperties(spaceId);
            log.info("from contentStore[" + contentStore.getStoreId() + "]: " +
                     "getting space properties: spaceId="+spaceId+", " +
                     "contentId="+ contentId + ", properties="+ properties);
             return properties;
        }
    }

    public static List<NameValuePair> convertExtendedProperties(Map<String, String> properties) {
        List<NameValuePair> extendedProperties = new LinkedList<NameValuePair>();
        if (extendedProperties != null) {
            for (String name : properties.keySet()) {
                extendedProperties.add(new NameValuePair(name,
                                                         properties.get(name)));
            }
        }

        return extendedProperties;
    }

    public static Object remove(String name, Map<String, String> properties) {
        return properties.remove(name);
    }

    public static Object add(String name,
                             String value,
                             Map<String, String> properties) {
        return properties.put(name, value);

    }

    public static Object getValue(String key, Map<String, String> properties) {
        return properties.get(key);
    }

	public static void handle(String method,
                              String context,
                              Map<String, String> properties,
			HttpServletRequest request) {
		if(method.equals("addRemove")){
			//remove properties;
			String[] names = extractList("properties-name-remove", request);
			String[] values = extractList("properties-value-remove", request);
			PropertiesUtils.remove(names, values, properties);

			//add properties
			names = extractList("properties-name-add", request);
			values = extractList("properties-value-add", request);
			PropertiesUtils.add(names, values, properties);
			
			//remove tags
			String[] tags = extractList("tag-remove", request);
			TagUtil.remove(tags, properties);
			//add tags
			tags = extractList("tag-add", request);
			TagUtil.add(tags,properties);
			
		}else{
	    	String tag = request.getParameter("tag");
	    	String name = request.getParameter("properties-name");
	    	String value = request.getParameter("properties-value");
	    	if(method.equals("addTag")){
	        	TagUtil.addTag(tag, properties);
	        	log.info("added tag [{}] to [{}]", tag, context);
	        }else if(method.equals("removeTag")){
	        	TagUtil.removeTag(tag, properties);
	        	log.info("removed tag [{}] from [{}]", tag, context);
	        }else if(method.equals("addProperties")){
	        	PropertiesUtils.add(name, value, properties);
	        	log.info("added properties [{}] to  [{}]", name+":"+value,context);
	        }else if(method.equals("removeProperties")){
	        	PropertiesUtils.remove(name, properties);
	        	log.info("removed properties [{}] from  [{}]", name+":"+value,context);
	        }else{
	        	log.warn("unexpected method parameter: " + method);
	        }
		}
	}

	private static String[] extractList(String prefix,
			HttpServletRequest request) {
		List<String> values = new LinkedList<String>();
		int count = 0;
		while(true){
			String value = request.getParameter(prefix + "-" + count);
			if(value != null){
				values.add(value);
			}else{
				break;
			}
			count++;
		}
		return values.toArray(new String[0]);
	}

	private static void add(String[] names, String[] values,
			Map<String, String> properties) {
		for(int i = 0; i < names.length; i++){
			add(names[i], values[i], properties);
		}
	}

	private static void remove(String[] names, String[] values,
			Map<String, String> properties) {
		for(int i = 0; i < names.length; i++){
			remove(names[i], properties);
		}
	}

}
