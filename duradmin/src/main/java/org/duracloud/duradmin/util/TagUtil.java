/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

public class TagUtil {

    public static final String TAGS = "tags";

    protected static final String DELIMITER = "|";

    /**
     * @param tag
     * @param metadata
     * @return true if the tag was present and was removed.
     */
    public static boolean removeTag(String tag, Map<String, String> metadata) {
        String tagsValue = metadata.get(TAGS);
        Set<String> tags = parseTags(tagsValue);
        boolean result = tags.remove(tag);
        tagsValue = formatTags(tags);
        metadata.put(TAGS, tagsValue);
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
     * @param metadata
     * @return true if the tag was added (ie wasn't already present in list)
     */
    public static boolean addTag(String tag, Map<String, String> metadata) {
        String tags = metadata.get(TAGS);
        if (tags == null) {
            metadata.put(TAGS, tag);
            return true;
        }
        Set<String> list = parseTags(tags);
        boolean result = list.add(tag);

        metadata.put(TAGS, formatTags(list));
        return result;
    }
}
