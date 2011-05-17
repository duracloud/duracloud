/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.view;

import org.apache.tiles.AttributeContext;
import org.apache.tiles.context.TilesRequestContext;
import org.apache.tiles.preparer.ViewPreparer;
import org.duracloud.duradmin.contentstore.ContentStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A view preparer to be invoked by every page in the application.
 * 
 * @author Daniel Bernstein
 * @version $Id$
 */
public class BaseViewPreparer
        implements ViewPreparer {

    private Logger log = LoggerFactory.getLogger(BaseViewPreparer.class);

    private ContentStoreProvider contentStoreProvider;

    public ContentStoreProvider getContentStoreProvider() {
        return contentStoreProvider;
    }

    public void setContentStoreProvider(ContentStoreProvider contentStoreProvider) {
        this.contentStoreProvider = contentStoreProvider;
    }

    public void execute(TilesRequestContext tilesRequestContext,
                        AttributeContext attributeContext) {


    }
}
