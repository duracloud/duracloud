/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.util.DateUtil.DateFormat;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
@Controller
@RequestMapping("/audit")
public class AuditLogController {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private ContentStoreManager contentStoreManager;

    @Autowired
    public AuditLogController(@Qualifier("contentStoreManager") ContentStoreManager contentStoreManager) {
        this.contentStoreManager = contentStoreManager;
    }

    @RequestMapping(value = "/{storeId}/{spaceId}", method = RequestMethod.GET)
    public String
        get(@PathVariable("storeId") String storeId,
            @PathVariable("spaceId") String spaceId, HttpServletResponse response) throws Exception {
        try {
            ContentStore contentStore =
                contentStoreManager.getContentStore(storeId);
            InputStream is = contentStore.getAuditLog(spaceId);
            StringBuffer contentDisposition = new StringBuffer();
            contentDisposition.append("attachment;");
            contentDisposition.append("filename=\"");
            SimpleDateFormat dateFormat =
                new SimpleDateFormat(DateFormat.PLAIN_FORMAT.getPattern());
            contentDisposition.append("audit-" + storeId
                                      + "-"
                                      + spaceId
                                      + "-"
                                      + dateFormat.format(new Date())
                                      + ".tsv");
            contentDisposition.append("\"");
            response.setHeader("Content-Disposition", contentDisposition.toString());
            IOUtils.copy(is, response.getOutputStream());
        } catch (NotFoundException ex) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            
        } catch (ContentStoreException | IOException ex) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return null;
    }

}
