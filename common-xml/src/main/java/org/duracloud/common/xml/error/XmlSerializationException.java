/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.xml.error;

/**
 * @author: Bill Branan
 * Date: 7/8/11
 */
public class XmlSerializationException extends RuntimeException {

    public XmlSerializationException(String message) {
        super(message);
    }

    public XmlSerializationException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
