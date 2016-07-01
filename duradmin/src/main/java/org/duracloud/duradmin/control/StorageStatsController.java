/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.SpaceStatsDTOList;
import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.common.util.DateUtil;
import org.duracloud.error.ContentStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author "Daniel Bernstein"
 *          Date: March 9, 2016 
 * 
 */
@Controller
@RequestMapping("/storagestats")
public class StorageStatsController {
    private ContentStoreManager contentStoreManager;
    private JaxbJsonSerializer<SpaceStatsDTOList> spaceStatsListSerializer =
        new JaxbJsonSerializer<>(SpaceStatsDTOList.class);

    @Autowired
    public StorageStatsController(
        @Qualifier("contentStoreManager") ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
    }

    @RequestMapping(value = "/timeseries")
    @ResponseBody 
    public String getSpaceStats(HttpServletResponse response,
                                      @RequestParam(required = false, value = "spaceId") String spaceId,
                                      @RequestParam(required = false, value = "storeId") String storeId,
                                      @RequestParam(required = false, value = "start") String start,
                                      @RequestParam(required = false, value = "end") String end)
                                          throws ContentStoreException,
                                              IOException,
                                                ParseException {

        ContentStore store = contentStoreManager.getPrimaryContentStore();
        if (storeId != null) {
            store = contentStoreManager.getContentStore(storeId);
        }
        
        if(spaceId != null){
            return spaceStatsListSerializer.serialize(store.getSpaceStats(spaceId,
                                                                          convertDate(start),
                                                                          convertDate(end)));
        }else{
            return spaceStatsListSerializer.serialize(store.getStorageProviderStats(convertDate(start),
                                                                                    convertDate(end)));
        }

    }

    @RequestMapping(value = "/snapshot-by-day")
    @ResponseBody 
    public String getStorageProviderStats(HttpServletResponse response,
                                      @RequestParam(required = false, value = "storeId") String storeId,
                                      @RequestParam(required = true, value = "date") String date)
                                          throws ContentStoreException,
                                              IOException,
                                                ParseException {

        ContentStore store = contentStoreManager.getPrimaryContentStore();
        if (storeId != null) {
            store = contentStoreManager.getContentStore(storeId);
        }
        return spaceStatsListSerializer.serialize(store.getStorageProviderStatsByDay(convertDate(date)));
    }

    private Date convertDate(String date) throws ParseException{
        if(date == null){
            return null;
        }
        return new Date(Long.parseLong(date));
    }

}
