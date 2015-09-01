/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.duracloud.manifest.ContentMessage;
import org.duracloud.manifest.ManifestFormatter;
import org.duracloud.manifest.error.ManifestFormatterException;
import org.duracloud.mill.db.model.ManifestItem;
import org.slf4j.Logger;

/**
 * This class provides the common logic for all ManifestFormatters.
 *
 * @author Andrew Woods
 *         Date: 3/29/12
 */
public abstract class ManifestFormatterBase implements ManifestFormatter {
    private boolean headerWasWritten = false;
    @Override
    public void writeEventsToOutput(Collection<ContentMessage> events,
                                    OutputStream output) {
        writeHeader(output);

        Iterator<ContentMessage> itr = events.iterator();
        while (itr.hasNext()) {
            ContentMessage event = itr.next();
            write(formatLine(event), output);

            if (itr.hasNext()) {
                write("\n", output);
            }
        }
    }
    
    @Override
    public void writeManifestItemToOutput(ManifestItem item,
                                          OutputStream output) {
        writeHeader(output);
        
        if(item != null){
            write(formatLine(item), output);
            write("\n", output);
        }
    }

    protected void writeHeader(OutputStream output) {
        String header = getHeader();
        if (null != header && !headerWasWritten) {
            write(header + "\n", output);
            headerWasWritten = true;
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

    public String formatLine(ContentMessage event) {
        return formatLine(event.getContentMd5(), event.getSpaceId(), event.getContentId());
    }

    public String formatLine(ManifestItem item){
        return formatLine(item.getContentChecksum(), item.getSpaceId(), item.getContentId());
    }

    
    
    protected abstract String formatLine(String contentMd5, String spaceId, String contentId);


}
