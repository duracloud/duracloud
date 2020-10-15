/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.duracloud.common.model.ContentItem;

/**
 * Interface for the writing retrieval tool output
 *
 * @author: Bill Branan
 * Date: Oct 13, 2010
 */
public interface OutputWriter {

    public static final DateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public void writeSuccess(ContentItem contentItem,
                             String localFilePath,
                             int attempts);

    public void writeFailure(ContentItem contentItem,
                             String error,
                             int attempts);

    public void writeMissing(ContentItem contentItem,
                             String message,
                             int attempts);

    public void close();

}
