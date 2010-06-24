/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.manifest.xml;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.duracloud.ChunksManifestDocument;
import org.duracloud.ChunksManifestType;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean;
import org.duracloud.common.error.DuraCloudRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;


/**
 * This class is a helper utility for binding ChunksManifest objects to a
 * ChunksManifest xml document.
 *
 * @author Andrew Woods
 *         Date: Feb 9, 2010
 */
public class ManifestDocumentBinding {

    /**
     * This method binds a ChunksManifest object to the content of the arg xml.
     *
     * @param xml manifest document to be bound to ChunksManifest object
     * @return ChunksManifest object
     */
    public static ChunksManifest createManifestFrom(InputStream xml) {
        try {
            ChunksManifestDocument doc = ChunksManifestDocument.Factory.parse(
                xml);
            return ManifestElementReader.createManifestFrom(doc);
        } catch (XmlException e) {
            throw new DuraCloudRuntimeException(e);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    /**
     * This method serializes the arg ChunksManifest object into an xml document.
     *
     * @param manifest ChunksManifest object to be serialized
     * @return ChunksManifest xml document
     */
    public static String createDocumentFrom(ChunksManifestBean manifest) {
        ChunksManifestDocument doc = ChunksManifestDocument.Factory
            .newInstance();
        if (null != manifest) {
            ChunksManifestType manifestType = ManifestElementWriter.createChunksManifestElementFrom(
                manifest);
            doc.setChunksManifest(manifestType);
        }
        return docToString(doc);
    }

    private static String docToString(XmlObject doc) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            doc.save(outputStream);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException(e);
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                throw new DuraCloudRuntimeException(e);
            }
        }
        return outputStream.toString();
    }

}