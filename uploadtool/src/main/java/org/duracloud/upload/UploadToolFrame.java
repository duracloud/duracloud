/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.upload;

import javax.swing.*;

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

        if (args.length == 4) {
            String host = args[0];
            String username = args[1];
            String password = args[2];
            String spaceId = args[3];

            uploadTool = new UploadTool(host, username, password, spaceId);
        } else {
            uploadTool = new UploadTool();
        }

        UploadToolFrame frame = new UploadToolFrame();
        frame.start("DuraCloud Upload Tool", uploadTool);
    }

}
