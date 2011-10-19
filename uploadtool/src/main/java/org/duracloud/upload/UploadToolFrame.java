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
        if (args.length != 4) {
            System.out.println(
                "Parameters expected: host, username, password, spaceId");
            System.exit(1);
        }

        String host = args[0];
        String username = args[1];
        String password = args[2];
        String spaceId = args[3];

        UploadTool uploadTool = new UploadTool(host,
                                               username,
                                               password,
                                               spaceId);

        UploadToolFrame frame = new UploadToolFrame();
        frame.start("DuraCloud Upload Tool", uploadTool);
    }

}
