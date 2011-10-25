/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.upload;

import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import netscape.javascript.JSObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Andrew Woods
 *         Date: 10/17/11
 */
public class UploadToolApplet extends JApplet {

    private Logger log = LoggerFactory.getLogger(UploadToolApplet.class);

    @Override
    public void init() {
        final String host = getParameter("host");
        final int port = Integer.valueOf(getParameter("port"));
        final String username = getParameter("username");
        final String password = getParameter("password");
        final String spaceId = getParameter("spaceId");
        final String storeId = getParameter("storeId");

        final UploadTool uploadTool = new UploadTool(host,
                                                     port,
                                                     username,
                                                     password,
                                                     spaceId,
                                                     storeId){
            @Override
            public void exit() {
                JSObject win = (JSObject) JSObject.getWindow(UploadToolApplet.this);
                win.eval("self.close();");
                super.exit();
            }
            
        };
        
        
        
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createGUI("DuraCloud Upload Tool", uploadTool);
                }
            });

        } catch (Exception e) {
            log.error("createGUI did not successfully complete: ", e);
        }
    }

    private void createGUI(String title, JComponent component) {
        super.setName(title);
        setContentPane(component);
    }

    public void setSpaceId(String spaceId) {
        log.info("setting spaceId: {}", spaceId);
    }

}
