/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.http.HttpStatus;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.task.SnapshotTaskClient;
import org.duracloud.client.task.SnapshotTaskClientManager;
import org.duracloud.common.constant.Constants;
import org.duracloud.error.ContentStateException;
import org.duracloud.error.ContentStoreException;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.snapshot.dto.SnapshotContentItem;
import org.duracloud.snapshot.dto.SnapshotHistoryItem;
import org.duracloud.snapshot.dto.task.CreateSnapshotTaskResult;
import org.duracloud.snapshot.dto.task.GetSnapshotContentsTaskResult;
import org.duracloud.snapshot.dto.task.GetSnapshotHistoryTaskResult;
import org.duracloud.snapshot.id.SnapshotIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Daniel Bernstein Date: Jan 27,2014
 */
@Controller
public class SnapshotController {

    protected final Logger log =
        LoggerFactory.getLogger(SnapshotController.class);

    private ContentStoreManager contentStoreManager;
    private DuracloudUserDetailsService userDetailsService;
    private SnapshotTaskClientManager snapshotTaskClientManager;

    @Autowired(required = true)
    public SnapshotController(@Qualifier("contentStoreManager") ContentStoreManager contentStoreManager,
                              @Qualifier("userDetailsSvc") DuracloudUserDetailsService userDetailsService,
                              SnapshotTaskClientManager snapshotTaskClientManager) {
        this.contentStoreManager = contentStoreManager;
        this.userDetailsService = userDetailsService;
        this.snapshotTaskClientManager = snapshotTaskClientManager;
    }

    @RequestMapping(value = "/spaces/snapshot", method = RequestMethod.POST)
    @ResponseBody
    public String create(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestParam String spaceId,
                         @RequestParam String storeId,
                         @RequestParam String description) throws Exception {

        ContentStore store = getContentStore(storeId);
        // check that a snapshot is not already being generated.
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON);

        if (isSnapshotInProgress(store, spaceId)) {
            response.setStatus(HttpStatus.SC_METHOD_FAILURE);
            return "{\"result\":\"Snapshot already in progress.\"}";
        } else {

            String username = getUsername(request);
            String userEmail = getUserEmail(username);

            SnapshotTaskClient taskClient = getTaskClient(storeId);
            try {
                CreateSnapshotTaskResult result = taskClient.createSnapshot(spaceId, description, userEmail);
                response.setStatus(HttpStatus.SC_ACCEPTED);
                return result.serialize();

            } catch (ContentStateException ex) {
                response.setStatus(HttpStatus.SC_CONFLICT);
                return ex.getMessage();
            }
        }
    }

    protected String getUserEmail(String username) {
        String userEmail = userDetailsService.getUserByUsername(username).getEmail();
        return userEmail;
    }

    protected String getUsername(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        return username;
    }

    @RequestMapping(value = "/spaces/snapshot", method = RequestMethod.GET)
    public ModelAndView get(@RequestParam String storeId,
                            @RequestParam String spaceId) {

        Properties props = new Properties();
        try {
            ContentStore store =
                this.contentStoreManager.getContentStore(storeId);
            if (store.contentExists(spaceId, Constants.SNAPSHOT_PROPS_FILENAME)) {
                try (InputStream is =
                         store.getContent(spaceId, Constants.SNAPSHOT_PROPS_FILENAME).getStream()) {
                    props.load(is);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            props.put("error", "Snapshot properties could not be loaded: " + e.getMessage());
        }

        ModelAndView mav = new ModelAndView("jsonView");
        for (Object key : props.keySet()) {
            mav.addObject(key.toString(), props.get(key));
        }
        return mav;
    }

    @RequestMapping(value = "/spaces/snapshots/{storeId}", method = RequestMethod.GET)
    @ResponseBody
    public String getSnapshotList(@PathVariable("storeId") String storeId) {
        try {
            return getTaskClient(storeId).getSnapshots().serialize();
        } catch (ContentStoreException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/spaces/snapshots/{storeId}/{snapshotId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public String getSnapshot(@PathVariable("storeId") String storeId,
                              @PathVariable("snapshotId") String snapshotId) throws Exception {

        return getTaskClient(storeId).getSnapshot(snapshotId).serialize();
    }

    @RequestMapping(value = "/spaces/snapshots/{storeId}/{snapshotId}/history", method = RequestMethod.GET)
    public ModelAndView getHistory(@PathVariable("storeId") String storeId,
                                   @PathVariable("snapshotId") String snapshotId,
                                   @RequestParam(value = "page", required = false) Integer page,
                                   @RequestParam(value = "attachment", required = false, defaultValue = "false")
                                       Boolean attachment,
                                   HttpServletResponse response) {
        try {
            SnapshotTaskClient taskClient = getTaskClient(storeId);

            if (attachment) {
                StringBuffer contentDisposition = new StringBuffer();
                contentDisposition.append("attachment;");
                contentDisposition.append("filename=\"");
                contentDisposition.append(snapshotId + ".history.json");
                contentDisposition.append("\"");
                response.setHeader("Content-Disposition", contentDisposition.toString());
            }

            if (page == null) {
                page = 0;
            }

            streamSnapshotHistory(page, storeId, snapshotId, taskClient, response);
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void streamSnapshotHistory(int page,
                                       String storeId,
                                       String snapshotId,
                                       SnapshotTaskClient taskClient,
                                       HttpServletResponse response) throws IOException, ContentStoreException {
        PrintWriter writer = response.getWriter();

        int pageSize = 200;
        if (page < 0) {
            pageSize = 1000;
        }

        int pageCounter = page;

        JsonFactory factory = new JsonFactory();
        JsonGenerator jwriter = factory.createJsonGenerator(writer);
        jwriter.writeStartObject();

        jwriter.writeFieldName("historyItems");
        jwriter.writeStartArray();
        GetSnapshotHistoryTaskResult result = null;
        while (true) {
            result = getSnapshotHistory(snapshotId, pageCounter, taskClient, pageSize);
            List<SnapshotHistoryItem> items = result.getHistoryItems();
            for (SnapshotHistoryItem item : items) {
                jwriter.writeStartObject();
                jwriter.writeNumberField("historyDate", item.getHistoryDate().getTime());
                jwriter.writeStringField("history", item.getHistory());
                jwriter.writeEndObject();
            }

            if (items.size() < pageSize || page >= 0) {
                break;
            } else {
                pageCounter++;
            }
        }

        jwriter.writeEndArray();
        if (page >= 0) {
            jwriter.writeNumberField("page", page);
            jwriter.writeNumberField("totalCount", result.getTotalCount());

            if (result.getHistoryItems().size() == pageSize && (page + 1 * pageSize < result.getTotalCount())) {
                jwriter.writeNumberField("nextPage", page + 1);
            } else {
                jwriter.writeNullField("nextPage");
            }
        }

        jwriter.writeStringField("snapshotId", snapshotId);
        jwriter.writeStringField("storeId", storeId);
        jwriter.writeEndObject();
        jwriter.close();
    }

    protected GetSnapshotHistoryTaskResult getSnapshotHistory(String snapshotId,
                                                              Integer page,
                                                              SnapshotTaskClient taskClient,
                                                              int pageSize) throws ContentStoreException {
        GetSnapshotHistoryTaskResult result = taskClient.getSnapshotHistory(snapshotId, page, pageSize);
        List<SnapshotHistoryItem> items = result.getHistoryItems();
        // Replace single quotes with double quotes in history values.
        // This allows history values that are valid JSON (without escaping) to be
        // provided as snapshot history updates, and be displayed properly.
        for (SnapshotHistoryItem item : items) {
            item.setHistory(item.getHistory().replaceAll("'", "\""));
        }
        return result;
    }

    @RequestMapping(value = "/spaces/snapshots/{storeId}/{snapshotId}/content", method = RequestMethod.GET)
    public ModelAndView getContent(@PathVariable("storeId") String storeId,
                                   @PathVariable("snapshotId") String snapshotId,
                                   @RequestParam(value = "page", required = false) Integer page,
                                   @RequestParam(value = "prefix", required = false) String prefix) {
        try {
            SnapshotTaskClient taskClient = getTaskClient(storeId);

            if (page == null) {
                page = 0;
            }
            int pageSize = 200;
            GetSnapshotContentsTaskResult result =
                taskClient.getSnapshotContents(snapshotId, page, pageSize, prefix);
            List<SnapshotContentItem> items = result.getContentItems();
            ModelAndView mav = new ModelAndView("jsonView");
            mav.addObject("contents", items);
            mav.addObject("page", page);
            mav.addObject("snapshotId", snapshotId);
            mav.addObject("storeId", storeId);
            mav.addObject("nextPage", items.size() == pageSize ? page + 1 : null);
            mav.addObject("prefix", prefix);
            mav.addObject("totalCount", result.getTotalCount());
            return mav;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the name of the restore space, if it exists, associated with a snapshot
     *
     * @param request
     * @param snapshotId
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/spaces/snapshots/{storeId}/{snapshotId}/restore-space-id", method = RequestMethod.GET)
    @ResponseBody
    public String restoreSpaceId(HttpServletRequest request,
                                 @PathVariable("storeId") String storeId,
                                 @PathVariable("snapshotId") String snapshotId) throws Exception {
        ContentStore contentStore = getContentStore(storeId);

        String spaceId = SnapshotIdentifier.parseSnapshotId(snapshotId).getRestoreSpaceId();

        if (contentStore.spaceExists(spaceId)) {
            return "{ \"spaceId\": \"" + spaceId + "\"," + "\"storeId\": \"" + storeId + "\"}";
        } else {
            return "{}";
        }
    }

    protected SnapshotTaskClient getTaskClient(String storeId)
        throws ContentStoreException {
        SnapshotTaskClient taskClient = this.snapshotTaskClientManager.get(storeId);
        return taskClient;
    }

    @RequestMapping(value = "/spaces/restores/{storeId}/by-snapshot/{snapshotId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public String getRestore(@PathVariable("storeId") String storeId,
                             @PathVariable("snapshotId") String snapshotId) {
        try {
            return getTaskClient(storeId).getRestoreBySnapshot(snapshotId).serialize();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/spaces/restores/{storeId}/{restoreId:.+}", method = RequestMethod.GET)
    @ResponseBody
    public String getRestoreByRestoreId(@PathVariable("storeId") String storeId,
                                        @PathVariable("restoreId") String restoreId) {
        try {
            return getTaskClient(storeId)
                .getRestore(restoreId)
                .serialize();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/spaces/restores", method = RequestMethod.POST)
    @ResponseBody
    public String restore(HttpServletRequest request,
                          @RequestParam String storeId,
                          @RequestParam String snapshotId) throws Exception {
        try {
            String userEmail = getUserEmail(getUsername(request));
            return getTaskClient(storeId).restoreSnapshot(snapshotId, userEmail).serialize();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    @RequestMapping(value = "/spaces/restores/request", method = RequestMethod.POST)
    @ResponseBody
    public String requestRestore(HttpServletRequest request,
                                 @RequestParam String storeId,
                                 @RequestParam String snapshotId) throws Exception {
        try {
            String userEmail = getUserEmail(getUsername(request));
            return getTaskClient(storeId).requestRestoreSnapshot(snapshotId, userEmail).serialize();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    private boolean isSnapshotInProgress(ContentStore store, String spaceId) {
        try {
            return store.getSpaceProperties(spaceId).containsKey(Constants.SNAPSHOT_ID_PROP);
        } catch (ContentStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ContentStore getContentStore(String storeId)
        throws ContentStoreException {
        return this.contentStoreManager.getContentStore(storeId);
    }

}
