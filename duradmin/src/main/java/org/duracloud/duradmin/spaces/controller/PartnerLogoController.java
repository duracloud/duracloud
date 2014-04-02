/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.duradmin.util.SpaceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Daniel Bernstein
 *
 */
@Controller
public class PartnerLogoController {

    protected final Logger log = 
        LoggerFactory.getLogger(PartnerLogoController.class);

    private ContentStoreManager contentStoreManager;

    private String adminSpaceId;

    @Autowired
    public PartnerLogoController(
        @Qualifier("contentStoreManager") ContentStoreManager contentStoreManager,
        @Qualifier("adminSpaceId") String adminSpaceId) {
        this.contentStoreManager = contentStoreManager;
        this.adminSpaceId = adminSpaceId;
    }
    
    @RequestMapping("/partnerlogo")
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		try{
			SpaceUtil.streamContent( contentStoreManager.getPrimaryContentStore(), response,this.adminSpaceId, "logo");
		}catch(Exception ex){
			ServletContext sc = request.getSession().getServletContext();
			InputStream is =  sc.getResourceAsStream("/images/partner_logo_placeholder.png");
			response.setContentType("image/png");
			OutputStream os = response.getOutputStream();
			byte[] buf = new byte[1024]; 
			int read = 0; 
			while ((read = is.read(buf)) >= 0) { 
				os.write(buf, 0, read); 
			} 
			is.close(); 
			os.close(); 		
		}
		
		return null;
	}
}
