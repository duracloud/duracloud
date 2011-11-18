/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.duradmin.domain.Acl;
import org.duracloud.duradmin.domain.Space;
import org.duracloud.error.ContentStoreException;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityUserBean;
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
 * @author Daniel Bernstein Date: Nov 17, 2011
 */
@Controller
public class SpaceAclController {

    protected final Logger log =
        LoggerFactory.getLogger(SpaceAclController.class);

    private ContentStoreManager contentStoreManager;
    private DuracloudUserDetailsService userDetailsService;

    @Autowired(required=true)
    public SpaceAclController(@Qualifier("contentStoreManager") ContentStoreManager contentStoreManager, 
                              DuracloudUserDetailsService userDetailsService) {
        this.contentStoreManager = contentStoreManager;
        this.userDetailsService = userDetailsService;
    }


    @RequestMapping(value = "/spaces/acls", method = RequestMethod.POST)
    public ModelAndView postSpaceAcls(HttpServletRequest request,
                                      @RequestParam String spaceId,
                                      @RequestParam String storeId,
                                      @RequestParam String action)
        throws Exception {

        Map<String, String> acls = buildAclMap(request);

        if ("add".equals(action)) {
            //overlay the new acls on top of the old.
            Map<String, String> currentAcls = getSpaceACLs(storeId,spaceId);
            currentAcls.putAll(acls);
            acls = currentAcls;
        }

        setSpaceACLs(storeId, spaceId, acls);
        acls = getSpaceACLs(storeId, spaceId);
        List<Acl> list = toAclList(acls);
        return createModel(list);
    }

    private ContentStore getContentStore(String storeId)
        throws ContentStoreException {
        return this.contentStoreManager.getContentStore(storeId);
    }

   
    @RequestMapping(value = "/spaces/acls", method = RequestMethod.GET)
    public ModelAndView getSpaceAcls(@RequestParam String spaceId,
                                     @RequestParam String storeId)
        throws Exception {

        Map<String,String> acls = getSpaceACLs(storeId, spaceId);
        List<Acl> list = toAclList(acls);
        return createModel(list);
    }

    private Map<String, String> getSpaceACLs(String storeId, String spaceId) throws Exception{
        return getContentStore(storeId).getSpaceACLs(spaceId);
    }

    private void setSpaceACLs(String storeId, String spaceId, Map<String,String> spaceACLs) throws Exception{
        getContentStore(storeId).setSpaceACLs(spaceId, spaceACLs);
    }

    private List<Acl> toAclList(Map<String, String> spaceACLs) {
        List<Acl> acls = new LinkedList<Acl>();

        if (spaceACLs != null) {
            for (Map.Entry<String, String> entry : spaceACLs.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                boolean read = false, write = false;
                if (value.equals("r")) {
                    read = true;
                } else if (value.equals("w")) {
                    read = true;
                    write = true;
                }

                acls.add(new Acl(key, formatDisplayName(key), read, write));
            }
        }

        return acls;

    }
    

    
    private Map<String, String> buildAclMap(HttpServletRequest request) {
        Map<String, String> acls = new HashMap<String, String>();
        
        String[] read = request.getParameterValues("read");
        if(read != null){
            for(String name : read){
                acls.put(name, "r");
            }
        }
        
        String[] write = request.getParameterValues("write");

        if(write != null){
            for(String name : write){
                acls.put(name, "w");
            }
        }

        return acls;
    }

    private String formatDisplayName(String key) {
        String prefix = "group-";
        if (key.startsWith(prefix)) {
            return key.replaceFirst(prefix, "");
        }

        return key;
    }

    @RequestMapping(value = "/spaces/acls/unassignedAcls", method = RequestMethod.GET)
    public ModelAndView
        getNewUserAcls(@RequestParam String spaceId,
                       @RequestParam String storeId) throws Exception {
        
        Map<String,String> currentAcls = getSpaceACLs(storeId, spaceId);
        List<Acl> list = new LinkedList<Acl>();

        List<SecurityUserBean>  users = getUsers();

        //add groups not already in the current acls
        List<String>  groups = getGroups(users);
        for (String group : groups) {
            if(!currentAcls.containsKey(group)){
                list.add(new Acl(group, formatDisplayName(group), false, false));
            }
        }

        //add users not already in the current acls
        for (SecurityUserBean user : users) {
            String username = user.getUsername();
            if(!currentAcls.containsKey(username)){
                list.add(new Acl(username, username, false, false));
            }
        }

        return createModel(list);
    }

    private List<SecurityUserBean> getUsers() {
       return  userDetailsService.getUsers();
    }

    private List<String> getGroups(Collection<SecurityUserBean> users) {
        Set<String> groups = new HashSet<String>();
        for(SecurityUserBean user : users){
            for(String group : user.getGroups()){
                groups.add(group);
            }
        }

        List<String> list = new LinkedList<String>(groups);
        Collections.sort(list);
        return list;
    }

    private ModelAndView createModel(List<Acl> acls) {
        ModelAndView mav = new ModelAndView("jsonView");
        if (acls != null) {
            mav.addObject("acls", acls);
        }

        return mav;
    }

    protected ContentStore getContentStore(Space space)
        throws ContentStoreException {
        return contentStoreManager.getContentStore(space.getStoreId());
    }
}
