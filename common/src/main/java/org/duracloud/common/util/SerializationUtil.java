/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Provides utility methods for serializing and deserializing.
 *
 * @author Bill Branan
 */
public class SerializationUtil {

    /**
     * Serializes a Map to XML. If the map is either empty or null
     * the XML will indicate an empty map.
     *
     * @param map
     * @return
     */
    public static String serializeMap(Map<String, String> map) {
        if(map == null) {
            map = new HashMap<String, String>();
        }
        XStream xstream = new XStream(new DomDriver());
        return xstream.toXML(map);
    }

    /**
     * DeSerializes XML into a Map. If the XML is either empty or
     * null an empty Map is returned.
     *
     * @param map
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> deserializeMap(String map) {
        if(map == null || map.equals("")) {
            return new HashMap<String, String>();
        } else {
            XStream xstream = new XStream(new DomDriver());
            return (Map<String, String>)xstream.fromXML(map);
        }
    }

    /**
     * Serializes a List to XML. If the list is either empty or null
     * the XML will indicate an empty list.
     *
     * @param list
     * @return
     */
    public static String serializeList(List<?> list) {
        if(list == null) {
            list = new ArrayList<String>();
        }
        XStream xstream = new XStream(new DomDriver());
        return xstream.toXML(list);
    }

    /**
     * DeSerializes XML into a List of Strings. If the XML is either
     * empty or null an empty List is returned.
     *
     * @param list
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<String> deserializeList(String list) {
        if(list == null || list.equals("")) {
            return new ArrayList<String>();
        }
        XStream xstream = new XStream(new DomDriver());
        return (List<String>)xstream.fromXML(list);
    }
}
