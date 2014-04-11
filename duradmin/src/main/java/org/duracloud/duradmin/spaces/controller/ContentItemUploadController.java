/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang.StringUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.duradmin.domain.ContentItem;
import org.duracloud.duradmin.util.SpaceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * 
 * @author Daniel Bernstein
 */

public class ContentItemUploadController implements Controller {

    protected final Logger log =
        LoggerFactory.getLogger(ContentItemUploadController.class);


    public ContentStoreManager getContentStoreManager() {
        return contentStoreManager;
    }

    public void setContentStoreManager(ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
    }

    private ContentStoreManager contentStoreManager;

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response)
        throws Exception {
        try {
            log.debug("handling request...");

            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iter = upload.getItemIterator(request);
            String spaceId = null;
            String storeId = null;
            String contentId = null;
            List<ContentItem> results = new ArrayList<ContentItem>();

            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                if (item.isFormField()) {
                    String value = Streams.asString(item.openStream(), "UTF-8");
                    if (item.getFieldName().equals("spaceId")) {
                        log.debug("setting spaceId: {}", value);
                        spaceId = value;
                    } else if (item.getFieldName().equals("storeId")) {
                        storeId = value;
                    } else if (item.getFieldName().equals("contentId")) {
                        contentId = value;
                    }
                } else {
                    log.debug("setting fileStream: {}", item);

                    if (StringUtils.isBlank(spaceId)) {
                        throw new IllegalArgumentException("space id required.");
                    }

                    ContentItem ci = new ContentItem();
                    if (StringUtils.isBlank(contentId)) {
                        contentId = item.getName();
                    }

                    ci.setContentId(contentId);
                    ci.setSpaceId(spaceId);
                    ci.setStoreId(storeId);
                    ci.setContentMimetype(item.getContentType());
                    ContentStore contentStore =
                        contentStoreManager.getContentStore(ci.getStoreId());
                    ContentItemUploadTask task =
                        new ContentItemUploadTask(ci,
                                                  contentStore,
                                                  item.openStream(),
                                                  request.getUserPrincipal()
                                                         .getName());

                    task.execute();
                    ContentItem result = new ContentItem();
                    Authentication auth =
                        (Authentication) SecurityContextHolder.getContext()
                                                              .getAuthentication();
                    SpaceUtil.populateContentItem(ContentItemController.getBaseURL(request),
                                                  result,
                                                  ci.getSpaceId(),
                                                  ci.getContentId(),
                                                  contentStore,
                                                  auth);
                    results.add(result);
                    contentId = null;
                }
            }

            return new ModelAndView("javascriptJsonView", "results", results);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

    }

}
