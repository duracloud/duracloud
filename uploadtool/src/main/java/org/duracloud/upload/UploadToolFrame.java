/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.upload;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * @author Andrew Woods
 *         Date: 10/17/11
 */
public class UploadToolFrame extends JFrame {

    public void start(final String title, final JComponent component) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI(title, component);
            }
        });
    }

    private void createGUI(String title, JComponent component) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(component);

        //Display the window.
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        UploadTool uploadTool;

        String storeId = null;
        if(args.length == 6) {
            storeId = args[5];
        }

        if (args.length >= 5) {
            String host = args[0];
            int port = Integer.valueOf(args[1]);
            String username = args[2];
            String password = args[3];
            String spaceId = args[4];

            uploadTool = new UploadTool(host,
                                        port,
                                        username,
                                        password,
                                        spaceId,
                                        storeId);
        } else {
            uploadTool = new UploadTool();
        }

        UploadToolFrame frame = new UploadToolFrame();
        frame.start("DuraCloud Upload Tool", uploadTool);
    }

}
