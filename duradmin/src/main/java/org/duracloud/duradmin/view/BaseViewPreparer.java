/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.view;

import org.apache.tiles.Attribute;
import org.apache.tiles.AttributeContext;
import org.apache.tiles.context.TilesRequestContext;
import org.apache.tiles.preparer.ViewPreparer;
import org.duracloud.error.ContentStoreException;
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

    	
        try {
        	attributeContext.putAttribute("mainMenu", new Attribute(MainMenu
                    .instance()), true);
            log.debug("main menu attribute set");

            String currentUrl =
                    (String) tilesRequestContext.getRequestScope()
                            .get("currentUrl");
            attributeContext.putAttribute("currentUrl",
                                          new Attribute(currentUrl),
                                          true);

            log.debug("currentUrl attribute set:" + currentUrl);

            contentStoreProvider.getContentStore();

            attributeContext.putAttribute("contentStoreProvider",
                                          new Attribute(contentStoreProvider),
                                          true);

            log.debug("contentStoreProvider attribute set: "
                    + contentStoreProvider);
            
            
            
        } catch (ContentStoreException ex) {
            log.error("failed to complete execution of BaseViewPreparer: "
                    + ex.getMessage());

        }

    }
}
