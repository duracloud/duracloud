/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.task.SnapshotTaskClient;
import org.duracloud.client.task.SnapshotTaskClientManager;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.error.ContentStoreException;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.SnapshotContentItem;
import org.duracloud.snapshot.dto.task.CreateSnapshotTaskParameters;
import org.duracloud.snapshot.dto.task.GetRestoreTaskParameters;
import org.duracloud.snapshot.dto.task.GetSnapshotContentsTaskResult;
import org.duracloud.snapshot.dto.task.GetSnapshotTaskParameters;
import org.duracloud.snapshot.dto.task.RestoreSnapshotTaskParameters;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * 
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
    public SnapshotController(
        @Qualifier("contentStoreManager") ContentStoreManager contentStoreManager,
        DuracloudUserDetailsService userDetailsService, 
        SnapshotTaskClientManager snapshotTaskClientManager) {
        this.contentStoreManager = contentStoreManager;
        this.userDetailsService = userDetailsService;
        this.snapshotTaskClientManager = snapshotTaskClientManager;
    }

    @RequestMapping(value = "/spaces/snapshot", method = RequestMethod.POST)
    public String create(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestParam String spaceId,
                         @RequestParam String storeId,
                         @RequestParam String description) throws Exception {

        ContentStore store = getContentStore(storeId);
        // check that a snapshot is not already being generated.
        response.setHeader("Content-Type", "application/json");

        if (isSnapshotInProgress(store, spaceId)) {
            response.setStatus(HttpStatus.SC_METHOD_FAILURE);
            response.getWriter()
                    .write("{\"result\":\"Snapshot already in progress.\"}");
        } else {
            CreateSnapshotTaskParameters params =
                new CreateSnapshotTaskParameters();
            params.setSpaceId(spaceId);
            params.setDescription(description);
            String username = request.getUserPrincipal().getName();
            params.setUserEmail(userDetailsService.getUserByUsername(username)
                                                  .getEmail());

            JaxbJsonSerializer<CreateSnapshotTaskParameters> serializer =
                new JaxbJsonSerializer<>(CreateSnapshotTaskParameters.class);
            String paramString = serializer.serialize(params);
            String json = store.performTask("create-snapshot", paramString);
            response.setStatus(HttpStatus.SC_ACCEPTED);
            response.getWriter().write(json);
        }
        return null;
    }

    @RequestMapping(value = "/spaces/snapshot", method = RequestMethod.GET)
    public ModelAndView get(@RequestParam String storeId,
                            @RequestParam String spaceId) {

        Properties props = new Properties();
        try {
            ContentStore store =
                this.contentStoreManager.getContentStore(storeId);
            if (store.contentExists(spaceId, Constants.SNAPSHOT_ID)) {
                try (InputStream is =
                    store.getContent(spaceId, Constants.SNAPSHOT_ID)
                         .getStream()) {
                    props.load(is);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            props.put("error",
                      "Snapshot properties could not be loaded: "
                          + e.getMessage());
        }

        ModelAndView mav = new ModelAndView("jsonView");
        for (Object key : props.keySet()) {
            mav.addObject(key.toString(), props.get(key));
        }
        return mav;
    }

    @RequestMapping(value = "/spaces/snapshots/{storeId}", method = RequestMethod.GET)
    @ResponseBody
    public String
        getSnapshotList(@PathVariable("storeId") String storeId,
                        HttpServletRequest request) {
        try {
            ContentStore store =
                this.contentStoreManager.getContentStore(storeId);
            String json = store.performTask("get-snapshots", "");
            return json;
        } catch (ContentStoreException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/spaces/snapshots/{storeId}/{snapshotId}", method = RequestMethod.GET)
    @ResponseBody
    public String
        getSnapshot(@PathVariable("storeId") String storeId,
                    @PathVariable("snapshotId") String snapshotId) {

        try {

            ContentStore store =
                this.contentStoreManager.getContentStore(storeId);

            GetSnapshotTaskParameters params = new GetSnapshotTaskParameters();
            params.setSnapshotId(snapshotId);

            JaxbJsonSerializer<GetSnapshotTaskParameters> serializer =
                new JaxbJsonSerializer<>(GetSnapshotTaskParameters.class);

            String paramString = serializer.serialize(params);
            String json = store.performTask("get-snapshot", paramString);
            return json;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
    
    @RequestMapping(value = "/spaces/snapshots/{storeId}/{snapshotId}/content", method = RequestMethod.GET)
    public ModelAndView
        getContent(@PathVariable("storeId") String storeId,
                    @PathVariable("snapshotId") String snapshotId,
                    @RequestParam(value="page", required=false) Integer page,
                    @RequestParam(value="prefix", required=false) String prefix) {
        try {
            SnapshotTaskClient taskClient = getTaskClient(storeId);

            if(page == null){
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
            mav.addObject("nextPage", items.size() == pageSize? page+1 : null);
            mav.addObject("prefix", prefix);
            return mav;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected SnapshotTaskClient getTaskClient(String storeId)
        throws ContentStoreException {
        SnapshotTaskClient taskClient =
            this.snapshotTaskClientManager.get(storeId);
        return taskClient;
    }

    @RequestMapping(value = "/spaces/restores/{storeId}/by-snapshot/{snapshotId}", method = RequestMethod.GET)
    @ResponseBody
    public String
        getRestore(@PathVariable("storeId") String storeId,
                   @PathVariable("snapshotId") String snapshotId) {

        GetRestoreTaskParameters params = new GetRestoreTaskParameters();
        params.setSnapshotId(snapshotId);
        return getRestore(storeId, params);
    }

    @RequestMapping(value = "/spaces/restores/{storeId}/{restoreId}", method = RequestMethod.GET)
    @ResponseBody
    public String
        getRestore(@PathVariable("storeId") String storeId,
                   @PathVariable("restoreId") Long restoreId) {
        GetRestoreTaskParameters params = new GetRestoreTaskParameters();
        params.setRestoreId(restoreId);
        return getRestore(storeId, params);
    }

    private String getRestore(String storeId, GetRestoreTaskParameters params) {
        try {
            JaxbJsonSerializer<GetRestoreTaskParameters> serializer =
                new JaxbJsonSerializer<>(GetRestoreTaskParameters.class);

            String paramString = serializer.serialize(params);
            ContentStore store =
                this.contentStoreManager.getContentStore(storeId);
            String json = store.performTask("get-restore", paramString);
            return json;
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

        ContentStore store = getContentStore(storeId);

        RestoreSnapshotTaskParameters params =
            new RestoreSnapshotTaskParameters();
        params.setSnapshotId(snapshotId);
        String username = request.getUserPrincipal().getName();
        params.setUserEmail(userDetailsService.getUserByUsername(username)
                                              .getEmail());

        JaxbJsonSerializer<RestoreSnapshotTaskParameters> serializer =
            new JaxbJsonSerializer<>(RestoreSnapshotTaskParameters.class);
        String paramString = serializer.serialize(params);
        String json =
            store.performTask(SnapshotConstants.RESTORE_SNAPSHOT_TASK_NAME, paramString);
        return json;
    }

    private boolean isSnapshotInProgress(ContentStore store, String spaceId) {
        try {
            return store.contentExists(spaceId, Constants.SNAPSHOT_ID);
        } catch (ContentStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ContentStore getContentStore(String storeId)
        throws ContentStoreException {
        return this.contentStoreManager.getContentStore(storeId);
    }

    protected ContentStore getContentStore(Space space)
        throws ContentStoreException {
        return contentStoreManager.getContentStore(space.getStoreId());
    }
}
