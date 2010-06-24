/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.streaming;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import java.util.Iterator;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: May 13, 2010
 */
public class PlaylistCreator {

    public static final String TITLE_META = "title";
    public static final String DESCRIPTION_META = "description";

    public String createPlaylist(ContentStore contentStore,
                                 String videoSourceSpaceId)
        throws ContentStoreException {
        Iterator<String> videoFiles =
            contentStore.getSpaceContents(videoSourceSpaceId);

        Element rss = new Element("rss");
        rss.setAttribute("version", "2.0");
        Namespace mediaNamespace =
            Namespace.getNamespace("media", "http://search.yahoo.com/mrss/");
        rss.addNamespaceDeclaration(mediaNamespace);

        Element channel = new Element("channel");
        rss.addContent(channel);

        while(videoFiles.hasNext()) {
            String videoFileId = videoFiles.next();

            // Determine metadata
            String videoTitle = videoFileId;
            String videoDescription = videoFileId;
            Map<String, String> metadata =
                contentStore.getContentMetadata(videoSourceSpaceId, videoFileId);
            if(metadata.containsKey(TITLE_META)) {
                videoTitle = metadata.get(TITLE_META);
            }
            if(metadata.containsKey(DESCRIPTION_META)) {
                videoDescription = metadata.get(DESCRIPTION_META);
            }

            // Create item XML
            Element item = new Element("item");
            channel.addContent(item);

            Element title = new Element("title");
            title.setText(videoTitle);
            item.addContent(title);

            Element description = new Element("description");
            description.setText(videoDescription);
            item.addContent(description);

            Element content = new Element("content");
            content.setNamespace(mediaNamespace);
            content.setAttribute("url", videoFileId);
            item.addContent(content);
        }

        Document doc = new Document(rss);
        XMLOutputter serializer = new XMLOutputter();
        return serializer.outputString(doc);        
    }
}
