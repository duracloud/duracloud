/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.util.ArrayList;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Daniel Bernstein
 */

@Controller
public class ContentItemUploadController {

    protected final Logger log =
        LoggerFactory.getLogger(ContentItemUploadController.class);

    private ContentStoreManager contentStoreManager;

    @Autowired
    public ContentItemUploadController(
        @Qualifier("contentStoreManager") ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
    }

    @RequestMapping(value = "/spaces/content/upload", method = RequestMethod.POST)
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
                    Authentication auth = SecurityContextHolder.getContextHolderStrategy()
                                                               .getContext()
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
