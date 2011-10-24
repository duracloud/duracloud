/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.upload.panel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.duracloud.upload.UploadFacilitator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * @author: Bill Branan
 * Date: 10/19/11
 */
public class ConnectionPanel extends JPanel {

    private static final String HOST_PREFIX = "https://";
    private static final String HOST_SUFFIX = ".duracloud.org";

    private JLabel hostLabel;
    private JLabel hostPrefixLabel;
    private JLabel hostSuffixLabel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel spaceIdLabel;

    private JTextField hostField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField spaceIdField;
    private JButton continueButton;

    private UploadFacilitator facilitator;

    private static final String columnSpecs = // 9 columns
        "20dlu,right:max(30dlu;pref),6dlu,pref,2dlu,25dlu:grow,2dlu,70dlu,20dlu";
    private static final String rowSpecs = // 11 rows
        "10dlu,pref,5dlu,pref,5dlu,pref,5dlu,pref,8dlu:grow,pref,5dlu";

    public ConnectionPanel(UploadFacilitator facilitator) {
        super(new FormLayout(columnSpecs, rowSpecs));

        initComponents(new ChangeListener());

        CellConstraints cc = new CellConstraints();
        add(hostLabel, cc.xyw(2, 2, 1));
        add(usernameLabel, cc.xyw(2, 4, 1));
        add(passwordLabel, cc.xyw(2, 6, 1));
        add(spaceIdLabel, cc.xyw(2, 8, 1));
        add(hostField, cc.xyw(6, 2, 1));
        add(usernameField, cc.xyw(4, 4, 5));
        add(passwordField, cc.xyw(4, 6, 5));
        add(spaceIdField, cc.xyw(4, 8, 5));
        add(continueButton, cc.xyw(8, 10, 1));
        add(hostPrefixLabel, cc.xyw(4, 2, 1));
        add(hostSuffixLabel, cc.xyw(8, 2, 1));

        this.facilitator = facilitator;
    }

    private void initComponents(ActionListener actionListener) {
        hostLabel = new JLabel("Host:");
        hostPrefixLabel = new JLabel(HOST_PREFIX);
        hostSuffixLabel = new JLabel(HOST_SUFFIX);
        passwordLabel = new JLabel("Password:");
        usernameLabel = new JLabel("Username:");
        spaceIdLabel = new JLabel("Space ID:");

        hostField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        spaceIdField = new JTextField();

        continueButton = new JButton("Continue");
        URL continueIcon =
            this.getClass().getClassLoader().getResource("arrow_right.png");
        continueButton.setIcon(new ImageIcon(continueIcon));
        continueButton.setHorizontalTextPosition(SwingConstants.LEFT);
        continueButton.setIconTextGap(8);
        continueButton.addActionListener(actionListener);
    }

    private class ChangeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == continueButton) {
                String host = hostField.getText();
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String spaceId = spaceIdField.getText();

                if(host.isEmpty() ||
                   username.isEmpty() ||
                   password.isEmpty() ||
                   spaceId.isEmpty()) {
                    displayMessage("All fields are required");
                } else {
                    String fullHost = host + HOST_SUFFIX;
                    facilitator.connect(fullHost,
                                        443,   // Expect https connection
                                        username,
                                        password,
                                        spaceId,
                                        null); // Primary storage provider
                }
            }
        }
    }

    private void displayMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

}
