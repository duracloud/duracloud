/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mimeutil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.droid.core.signature.droid4.Droid;
import uk.gov.nationalarchives.droid.core.signature.droid4.FileFormatHit;
import uk.gov.nationalarchives.droid.core.signature.droid4.IdentificationFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class wraps the Droid utility for the purpose of identifying mimetypes
 * of provided files.
 * http://droid.sourceforge.net/
 *
 * @author Andrew Woods
 *         Date: Nov 5, 2010
 */
public class DroidWrapper {
    private final static Logger log = LoggerFactory.getLogger(DroidWrapper.class);

    private Droid droid;
    private static final String DEFAULT_MIMETYPE = "application/octet-stream";
    private static final String DEFAULT_SIGFILE = "DROID_SignatureFile_V42.xml";

    public DroidWrapper() {
        this(getDefaultSignatureFilePath());
    }

    public DroidWrapper(String signatureFilePath) {
        try {
            droid = new Droid();
            droid.readSignatureFile(signatureFilePath);

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * This method inspects the arg inputstream and returns its mimetype.
     * application/octet-stream is returned if the mimetype is unknown or an
     * error occurs.
     *
     * @param inputStream of content for which mimetype is sought
     * @return mimetype
     */
    public String determineMimeType(InputStream inputStream) {
        File file;
        OutputStream outputStream;
        try {
            file = File.createTempFile("droid-stream", ".tmp");
            outputStream = FileUtils.openOutputStream(file);
            IOUtils.copy(inputStream, outputStream);

        } catch (IOException e) {
            log.warn("Error creating tmp stream: " + e.getMessage());
            return DEFAULT_MIMETYPE;
        }

        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);

        return determineMimeType(file);
    }

    /**
     * This method inspects the arg file and returns its mimetype.
     * application/octet-stream is returned if the mimetype is unknown or an
     * error occurs.
     *
     * @param file for which mimetype is sought
     * @return mimetype
     */
    public String determineMimeType(File file) {
        IdentificationFile identificationFile = identify(file);
        if (null == identificationFile) {
            log.warn("Error identifying file: " + file);
            return DEFAULT_MIMETYPE;
        }

        String mime = DEFAULT_MIMETYPE;
        for (int i = 0; i < identificationFile.getNumHits(); ++i) {
            FileFormatHit hit = identificationFile.getHit(i);
            String currentMime = getMimetype(hit);

            if (hit.isSpecific()) {
                return currentMime;

            } else if (DEFAULT_MIMETYPE != getMimetype(hit)) {
                mime = currentMime;
            }
        }

        return mime;
    }

    private IdentificationFile identify(File file) {
        return droid.identify(file.getAbsolutePath());
    }

    private String getMimetype(FileFormatHit hit) {
        String mimetype = hit.getMimeType();
        if (!StringUtils.isBlank(mimetype)) {
            return mimetype;
        } else {
            return DEFAULT_MIMETYPE;
        }
    }

    private static String getDefaultSignatureFilePath() {
        InputStream signatureStream = DroidWrapper.class.getClassLoader()
            .getResourceAsStream(DEFAULT_SIGFILE);

        if (null == signatureStream) {
            String msg = "Unable to load resource: " + DEFAULT_SIGFILE;
            log.error(msg);
            throw new RuntimeException(msg);
        }

        File sigFile = null;
        OutputStream outputStream = null;
        try {
            sigFile = File.createTempFile(DEFAULT_SIGFILE, ".tmp");
            outputStream = FileUtils.openOutputStream(sigFile);
            IOUtils.copy(signatureStream, outputStream);

        } catch (IOException e) {
            String msg = "Error writing sigfile resource: " + e.getMessage();
            log.error(msg);
            throw new RuntimeException(msg);

        } finally {
            IOUtils.closeQuietly(signatureStream);
            IOUtils.closeQuietly(outputStream);
        }

        return sigFile.getAbsolutePath();
    }

}
