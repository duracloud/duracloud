/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest;

import java.io.OutputStream;
import java.text.ParseException;
import java.util.Collection;

import org.duracloud.mill.db.model.ManifestItem;

/**
 * This class defines the contract for Manifest Formatters.
 *
 * @author Andrew Woods
 *         Date: 3/29/12
 */
public interface ManifestFormatter {

    /**
     * This method writes the arg events to the arg output stream.
     *
     * @param events to write
     * @param output destination of formatted events
     */
    public void writeEventsToOutput(Collection<ContentMessage> events,
                                    OutputStream output);
    
    /**
     * 
     * @param item to write
     * @param outputStream destination of formatted items.  Null manifest items are ignored.
     */
    public void writeManifestItemToOutput(ManifestItem item, OutputStream outputStream);

    /**
     * Returns the header if there is one, otherwise null.
     * @return
     */
    public String getHeader();
    
    /**
     * Parses a line into a ManifestItem
     * @param line
     * @return
     * @throws ParseException
     */
    public ManifestItem parseLine(String line) throws ParseException;

    /**
     * Formats a manifest item into an appropriate line.
     * @param item
     * @return
     */
    public String formatLine(ManifestItem item);
}
