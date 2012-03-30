/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest;

import org.duracloud.storage.aop.ContentMessage;

import java.io.OutputStream;
import java.util.Collection;

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

}
