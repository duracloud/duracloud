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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * @author: Bill Branan
 * Date: 10/19/11
 */
public class CompletedPanel extends JPanel {

    private JLabel completeLabel;
    private JButton exitButton;

    private UploadFacilitator facilitator;

    private static final String columnSpecs = // 4 columns
        "80dlu,15dlu,80dlu,15dlu";
    private static final String rowSpecs = // 4 rows
        "20dlu,pref,10dlu,pref";

    public CompletedPanel(UploadFacilitator facilitator) {
        super(new FormLayout(columnSpecs, rowSpecs));

        initComponents(new ChangeListener());

        CellConstraints cc = new CellConstraints();
        add(completeLabel, cc.xyw(2, 2, 3));
        add(exitButton, cc.xyw(3, 4, 1));

        this.facilitator = facilitator;
    }

    private void initComponents(ActionListener actionListener) {
        completeLabel = new JLabel("Upload is Complete");
        Font defaultFont = completeLabel.getFont();
        Font updatedFont = new Font(defaultFont.getFontName(),
                                    defaultFont.getStyle(),
                                    defaultFont.getSize()+11);
        completeLabel.setFont(updatedFont);

        exitButton = new JButton("Close");
        URL exitIcon =
            this.getClass().getClassLoader().getResource("close.png");
        exitButton.setIcon(new ImageIcon(exitIcon));
        exitButton.addActionListener(actionListener);
    }

    private class ChangeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == exitButton) {
                facilitator.exit();
            }
        }
    }

    private void displayMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

}
