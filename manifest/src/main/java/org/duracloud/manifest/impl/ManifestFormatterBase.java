/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import org.apache.commons.io.IOUtils;
import org.duracloud.manifest.ContentMessage;
import org.duracloud.manifest.ManifestFormatter;
import org.duracloud.manifest.error.ManifestFormatterException;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class provides the common logic for all ManifestFormatters.
 *
 * @author Andrew Woods
 *         Date: 3/29/12
 */
public abstract class ManifestFormatterBase implements ManifestFormatter {

    @Override
    public void writeEventsToOutput(Collection<ContentMessage> events,
                                    OutputStream output) {
        String header = getHeader();
        if (null != header) {
            write(header + "\n", output);
        }

        Iterator<ContentMessage> itr = events.iterator();
        while (itr.hasNext()) {
            ContentMessage event = itr.next();
            write(getLine(event), output);

            if (itr.hasNext()) {
                write("\n", output);
            }
        }
    }

    private void write(String line, OutputStream output) {
        try {
            IOUtils.write(line, output);

        } catch (IOException e) {
            StringBuilder err = new StringBuilder("Error writing line: '");
            err.append(line);
            err.append("', error: ");
            err.append(e.getMessage());
            log().error(err.toString());
            throw new ManifestFormatterException(err.toString(), e);
        }
    }

    protected abstract Logger log();

    protected abstract String getHeader();

    protected abstract String getLine(ContentMessage event);

}
