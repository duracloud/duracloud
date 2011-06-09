/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.domain.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Andrew Woods
 *         Date: 6/8/11
 */
public class ContentStreamUtil {
    private final Logger log = LoggerFactory.getLogger(ContentStreamUtil.class);

    public void writeToOutputStream(String text, OutputStream outputStream) {
        try {
            IOUtils.write(text, outputStream);

        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Error ");
            sb.append("writing to outputstream.");
            log.error(sb.toString());
            throw new DuraCloudRuntimeException(sb.toString(), e);
        }
    }

    public void writeToOutputStream(InputStream inputStream,
                                     OutputStream outputStream) {
        try {
            IOUtils.copy(inputStream, outputStream);

        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Error ");
            sb.append("copying from inputstream to outputstream.");
            log.error(sb.toString());
            throw new DuraCloudRuntimeException(sb.toString(), e);
        }
    }

    public OutputStream createOutputStream(File file) {
        try {
            return FileUtils.openOutputStream(file);

        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Error ");
            sb.append("creating outputstream: ");
            sb.append(file.getPath());
            log.error(sb.toString());
            throw new DuraCloudRuntimeException(sb.toString(), e);
        }

    }
}
