/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A spring controller to support jqueryFileTree.js
 * 
 * @author Daniel Bernstein
 * 
 */
@Controller
public class JQueryFileTreeController {


 
    @RequestMapping(value = { "/ajax/jqueryFileTree" })
    public String get(@RequestParam(value="dir", required=false) String dir, Model model) throws Exception {
        // if blank
        List<File> children = new ArrayList<File>();
        model.addAttribute("children", children);
        
        if (StringUtils.isBlank(dir)) {
            File[] roots = File.listRoots();

            // display roots if more than one
            if (roots.length > 1) {
                // otherwise display the list of multiple roots (windows)
                children.addAll(Arrays.asList(roots));
            } else {
                loadChildren(roots[0], children);
            }
        } else {
            if (dir.charAt(dir.length() - 1) == '\\') {
                dir = dir.substring(0, dir.length() - 1) + "/";
            } else if (dir.charAt(dir.length() - 1) != '/') {
                dir += "/";
            }

            dir = java.net.URLDecoder.decode(dir, "UTF-8");
            File directory = new File(dir);
            loadChildren(directory, children);
        }

        return "jqueryFileTree";
    }

    private void loadChildren(File directory, List<File> children) {
        if (directory.exists()) {
            String[] filenames = directory.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.charAt(0) != '.';
                }
            });

            Arrays.sort(filenames, String.CASE_INSENSITIVE_ORDER);
            for (String filename : filenames) {
                children.add(new File(directory, filename));
            }
        }
    }

}
