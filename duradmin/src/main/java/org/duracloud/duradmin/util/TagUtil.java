/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TagUtil {

    public static final String TAGS = "tags";

    protected static final String DELIMITER = "|";

    /**
     * @param tag
     * @param properties
     * @return true if the tag was present and was removed.
     */
    public static boolean removeTag(String tag, Map<String, String> properties) {
        String tagsValue = properties.get(TAGS);
        Set<String> tags = parseTags(tagsValue);
        boolean result = tags.remove(tag);
        tagsValue = formatTags(tags);
        properties.put(TAGS, tagsValue);
        return result;
    }

    static Set<String> parseTags(String tagsValue) {
        Set<String> set = new LinkedHashSet<String>();
        if (StringUtils.hasText(tagsValue)) {
            set.addAll(Arrays.asList(tagsValue.split("[" + DELIMITER + "]")));
        }
        return set;
    }

    private static String formatTags(Set<String> tags) {
        StringBuffer buf = new StringBuffer();
        String[] tagsArray = tags.toArray(new String[0]);
        for (int i = 0; i < tagsArray.length; i++) {
            if (i > 0) {
                buf.append(DELIMITER);
            }
            buf.append(tagsArray[i]);
        }

        return buf.toString();
    }

    /**
     * @param tag
     * @param properties
     * @return true if the tag was added (ie wasn't already present in list)
     */
    public static boolean addTag(String tag, Map<String, String> properties) {
        String tags = properties.get(TAGS);
        if (tags == null) {
            properties.put(TAGS, tag);
            return true;
        }
        Set<String> list = parseTags(tags);
        boolean result = list.add(tag);

        properties.put(TAGS, formatTags(list));
        return result;
    }

	public static void remove(String[] tags, Map<String, String> properties) {
		for(int i = 0; i < tags.length; i++){
			removeTag(tags[i], properties);
		}
	}

	public static void add(String[] tags,  Map<String, String> properties) {
		for(int i = 0; i < tags.length; i++){
			addTag(tags[i], properties);
		}
	}
}
