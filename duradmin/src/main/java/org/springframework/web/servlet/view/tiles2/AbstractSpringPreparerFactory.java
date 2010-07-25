/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.springframework.web.servlet.view.tiles2;

import javax.servlet.ServletRequest;

import org.apache.tiles.TilesException;
import org.apache.tiles.context.TilesRequestContext;
import org.apache.tiles.preparer.PreparerFactory;
import org.apache.tiles.preparer.ViewPreparer;
import org.apache.tiles.servlet.context.ServletTilesApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Abstract implementation of the Tiles2
 * {@link org.apache.tiles.preparer.PreparerFactory} interface, obtaining the
 * current Spring WebApplicationContext and delegating to
 * {@link #getPreparer(String, org.springframework.web.context.WebApplicationContext)}
 * .
 * 
 * @author Juergen Hoeller
 * @since 2.5
 * @see #getPreparer(String,
 *      org.springframework.web.context.WebApplicationContext)
 * @see SimpleSpringPreparerFactory
 * @see SpringBeanPreparerFactory
 */
public abstract class AbstractSpringPreparerFactory
        implements PreparerFactory {

    public ViewPreparer getPreparer(String name, TilesRequestContext context)
            throws TilesException {
        ServletRequest servletRequest = null;
        if (context.getRequest() instanceof ServletRequest) {
            servletRequest = (ServletRequest) context.getRequest();
        }
        ServletTilesApplicationContext tilesApplicationContext = null;
        if (context instanceof ServletTilesApplicationContext) {
            tilesApplicationContext = (ServletTilesApplicationContext) context;
        }
        if (servletRequest == null && tilesApplicationContext == null) {
            throw new IllegalStateException("SpringBeanPreparerFactory requires either a "
                    + "ServletRequest or a ServletTilesApplicationContext to operate on");
        }

        //This is a temporary fix for a problem that is fixed in the spring 3.0 codebase. 
        //TODO REMOVE WHEN WE UPGRADE TO SPRING 3.0
        //		WebApplicationContext webApplicationContext = RequestContextUtils.getWebApplicationContext(
        //				servletRequest, tilesApplicationContext.getServletContext());
        //added these lines to fix problem
        //more info:  http://jira.springframework.org/browse/SPR-5886?page=com.atlassian.jira.plugin.system.issuetabpanels%3Aall-tabpanel
        WebApplicationContext webApplicationContext =
                RequestContextUtils
                        .getWebApplicationContext(servletRequest,
                                                  tilesApplicationContext == null ? null
                                                          : tilesApplicationContext
                                                                  .getServletContext());
        return getPreparer(name, webApplicationContext);
    }

    /**
     * Obtain a preparer instance for the given preparer name, based on the
     * given Spring WebApplicationContext.
     * 
     * @param name
     *        the name of the preparer
     * @param context
     *        the current Spring WebApplicationContext
     * @return the preparer instance
     * @throws TilesException
     *         in case of failure
     */
    protected abstract ViewPreparer getPreparer(String name,
                                                WebApplicationContext context)
            throws TilesException;

}
