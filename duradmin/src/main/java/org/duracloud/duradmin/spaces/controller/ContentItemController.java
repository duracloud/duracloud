/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.task.S3TaskClient;
import org.duracloud.client.task.S3TaskClientManager;
import org.duracloud.duradmin.domain.ContentItem;
import org.duracloud.duradmin.util.PropertiesUtils;
import org.duracloud.duradmin.util.SpaceUtil;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Daniel Bernstein
 */
@Controller
@RequestMapping("/spaces/content")
public class ContentItemController {

    protected final Logger log =
        LoggerFactory.getLogger(ContentItemController.class);

    private ContentStoreManager contentStoreManager;
    private S3TaskClientManager taskClientManager;

    @Autowired
    public ContentItemController(
        @Qualifier("contentStoreManager") ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
        this.taskClientManager = new S3TaskClientManager(contentStoreManager);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ModelAndView delete(@Valid ContentItem contentItem,
                               BindingResult result) throws Exception {
        String spaceId = contentItem.getSpaceId();
        ContentStore contentStore = getContentStore(contentItem);
        contentStore.deleteContent(spaceId, contentItem.getContentId());
        return createModel(contentItem);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ModelAndView get(HttpServletRequest request, HttpServletResponse response,
                            ContentItem ci,
                            BindingResult result) throws Exception {
        ContentItem contentItem = new ContentItem();
        try {
            populateContentItem(request, getContentStore(ci), ci, contentItem);

            if (!StringUtils.isBlank(contentItem.getContentId())) {
                return createModel(contentItem);
            } else {
                return new ModelAndView("jsonView", "contentItem", null);
            }
        } catch (ContentStoreException ex) {
            ex.printStackTrace();
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return new ModelAndView("jsonView", "contentItem", null);
        }
    }

    @RequestMapping(value = "hls-url", method = RequestMethod.GET)
    public ModelAndView getHlsUrl(HttpServletRequest request,
                                           HttpServletResponse response,
                                           ContentItem contentItem,
                                           BindingResult result) throws Exception {
        try {
            String streamingType = request.getParameter("hlsStreamingType");
            S3TaskClient taskClient = getTaskClient(contentItem);
            String urlToStream;
            if ("SECURE".equals(streamingType)) {
                urlToStream =
                    taskClient.getHlsUrl(contentItem.getSpaceId(),
                                            contentItem.getContentId()).getStreamUrl();
            } else {
                urlToStream =
                    taskClient.getHlsUrl(contentItem.getSpaceId(),
                                      contentItem.getContentId())
                              .getStreamUrl();
            }

            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("streamingUrl", urlToStream);
            return new ModelAndView("jsonView", "streamingInfo", responseMap);
        } catch (ContentStoreException ex) {
            ex.printStackTrace();
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return new ModelAndView("jsonView", "streamingInfo", null);
        }
    }

    @RequestMapping(value = "/update-properties", method = RequestMethod.POST)
    public ModelAndView updateContentProperties(HttpServletRequest request,
                                                @RequestParam String method,
                                                HttpServletResponse response, @Valid ContentItem contentItem,
                                                BindingResult results) throws Exception {
        try {
            String spaceId = contentItem.getSpaceId();
            String contentId = contentItem.getContentId();
            ContentStore contentStore = getContentStore(contentItem);
            ContentItem result = new ContentItem();
            Map<String, String> properties =
                contentStore.getContentProperties(spaceId, contentId);
            PropertiesUtils.handle(method,
                                   "space [" + spaceId + "]",
                                   properties,
                                   request);
            contentStore.setContentProperties(spaceId, contentId, properties);
            populateContentItem(request, contentStore, contentItem, result);
            return createModel(result);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @RequestMapping(value = "/change-mimetype", method = RequestMethod.POST)
    public ModelAndView changeMimeType(HttpServletRequest request,
                                       HttpServletResponse response, @Valid ContentItem contentItem,
                                       BindingResult results) throws Exception {
        try {
            String spaceId = contentItem.getSpaceId();
            String contentId = contentItem.getContentId();
            ContentStore contentStore = getContentStore(contentItem);
            ContentItem result = new ContentItem();
            Map<String, String> properties =
                contentStore.getContentProperties(spaceId, contentId);
            String mimetype = contentItem.getContentMimetype();
            String oldMimetype = properties.get(ContentStore.CONTENT_MIMETYPE);
            if (!StringUtils.isBlank(mimetype) && !mimetype.equals(oldMimetype)) {
                properties.put(ContentStore.CONTENT_MIMETYPE, mimetype);
                contentStore.setContentProperties(spaceId, contentId, properties);
            }

            populateContentItem(request, contentStore, contentItem, result);
            return createModel(result);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @RequestMapping(value = "/copy", method = RequestMethod.POST)
    public ModelAndView copy(HttpServletRequest request,
                             HttpServletResponse response, @Valid ContentItem contentItem,
                             BindingResult result) throws Exception {
        try {
            String spaceId = contentItem.getSpaceId();
            String contentId = contentItem.getContentId();
            ContentStore contentStore = getContentStore(contentItem);
            return handleCopyContentItem(request,
                                         contentItem,
                                         spaceId,
                                         contentId,
                                         contentStore);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private ModelAndView handleCopyContentItem(
        HttpServletRequest request, ContentItem contentItem, String spaceId,
        String contentId, ContentStore contentStore)
        throws ContentStoreException, MalformedURLException {

        String destStoreId = request.getParameter("destStoreId");
        String destSpaceId = request.getParameter("destSpaceId");
        String destContentId = request.getParameter("destContentId");

        if (Boolean.valueOf(request.getParameter("deleteOriginal"))) {
            contentStore.moveContent(
                spaceId,
                contentId,
                destStoreId,
                destSpaceId,
                destContentId);
        } else {
            contentStore.copyContent(
                spaceId,
                contentId,
                destStoreId,
                destSpaceId,
                destContentId);
        }

        ContentItem result = new ContentItem();
        result.setStoreId(destStoreId);
        result.setSpaceId(destSpaceId);
        result.setContentId(destContentId);

        if (!contentStore.getStoreId().equals(result.getStoreId())) {
            contentStore = getContentStore(result);
        }

        populateContentItem(request, contentStore, result, result);
        return createModel(result);

    }

    private void populateContentItem(HttpServletRequest request,
                                     ContentStore contentStore,
                                     ContentItem contentItem,
                                     ContentItem result)
        throws ContentStoreException,
        MalformedURLException {

        Authentication auth =
            (Authentication) SecurityContextHolder.getContext()
                                                  .getAuthentication();

        SpaceUtil.populateContentItem(getBaseURL(request),
                                      result,
                                      contentItem.getSpaceId(),
                                      contentItem.getContentId(),
                                      contentStore,
                                      auth);
        String primaryStorageProviderId =
            contentStoreManager.getPrimaryContentStore().getStoreId();
        boolean primary = contentItem.getStoreId().equals(primaryStorageProviderId);
        result.setPrimaryStorageProvider(primary);
    }

    public static String getBaseURL(HttpServletRequest request) throws MalformedURLException {
        URL url = new URL(request.getRequestURL().toString());
        int port = url.getPort();
        String baseURL = url.getProtocol() + "://" + url.getHost() + ":" +
                         (port > 0 && port != 80 ? url.getPort() : "") +
                         request.getContextPath();
        return baseURL;
    }

    private ModelAndView createModel(ContentItem ci) {
        return new ModelAndView("jsonView", "contentItem", ci);
    }

    protected ContentStore getContentStore(ContentItem contentItem) throws ContentStoreException {
        return contentStoreManager.getContentStore(contentItem.getStoreId());
    }

    protected S3TaskClient getTaskClient(ContentItem contentItem) throws ContentStoreException {
        return taskClientManager.get(contentItem.getStoreId());
    }
}
