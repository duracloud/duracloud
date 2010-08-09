/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.domain;

/**
 * @author Andrew Woods
 *         Date: Aug 6, 2010
 */
public class ContentLocationPair {
    private ContentLocation contentLocationA;
    private ContentLocation contentLocationB;

    public ContentLocationPair(ContentLocation contentLocationA,
                               ContentLocation contentLocationB) {
        this.contentLocationA = contentLocationA;
        this.contentLocationB = contentLocationB;
    }

    public ContentLocation getContentLocationA() {
        return contentLocationA;
    }

    public ContentLocation getContentLocationB() {
        return contentLocationB;
    }
}
