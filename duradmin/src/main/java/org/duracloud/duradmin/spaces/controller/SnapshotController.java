/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.chrontask.snapshot.SnapshotTaskParameters;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Daniel Bernstein 
 *         Date: Jan 27,2014
 */
@Controller
public class SnapshotController {

    protected final Logger log =
        LoggerFactory.getLogger(SnapshotController.class);

    private ContentStoreManager contentStoreManager;

    @Autowired(required=true)
    public SnapshotController(@Qualifier("contentStoreManager") ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
    }


    @RequestMapping(value = "/spaces/snapshot", method = RequestMethod.POST)
    public String create(HttpServletRequest request,
                                      HttpServletResponse response,
                                      @RequestParam String spaceId,
                                      @RequestParam String storeId,
                                      @RequestParam String description)
        throws Exception {


        ContentStore store = getContentStore(storeId);
        //check that a snapshot is not already being generated.
        response.setHeader("Content-Type", "application/json");

        if(isSnapshotInProgress(store,storeId, spaceId)) {
            response.setStatus(HttpStatus.SC_METHOD_FAILURE);
            response.getWriter().write("{\"result\":\"Snapshot already in progress.\"}");
        }else{
            Map<String,String> snapshotProperties = new HashMap<>();
            snapshotProperties.put("description", description);
            SnapshotTaskParameters params = new SnapshotTaskParameters();
            params.setSpaceId(spaceId);
            params.setSnapshotProperties(snapshotProperties);
            JaxbJsonSerializer<SnapshotTaskParameters> serializer =
                new JaxbJsonSerializer<>(SnapshotTaskParameters.class);
            
            String paramString = serializer.serialize(params);
            String json = store.performTask("snapshot", paramString);
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
            ContentStore store = this.contentStoreManager.getContentStore(storeId);
            try(InputStream is = store.getContent(spaceId, Constants.SNAPSHOT_ID).getStream()){
                props.load(is);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            props.put("error", "Snapshot properties could not be loaded: " + e.getMessage());
        }

        ModelAndView mav =  new ModelAndView("jsonView");
        for(Object key : props.keySet()){
            mav.addObject(key.toString(), props.get(key));
        }
        return mav;
    }

    private boolean isSnapshotInProgress(ContentStore store, String storeId, String spaceId) {
        try {
            
            store.getContentProperties(spaceId, Constants.SNAPSHOT_ID);
            return true;
        }catch(NotFoundException ex){
            return false;
        }catch (ContentStoreException ex){
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
