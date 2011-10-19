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
import org.duracloud.upload.Uploader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * @author: Bill Branan
 * Date: 10/19/11
 */
public class StatusPanel extends JPanel {

    private JLabel processLabel;
    private JButton cancelButton;

    private UploadFacilitator facilitator;
    private Uploader uploader;

    private static final String columnSpecs = // 4 columns
        "80dlu,15dlu,80dlu,15dlu";
    private static final String rowSpecs = // 4 rows
        "20dlu,pref,10dlu,pref";

    public StatusPanel(UploadFacilitator facilitator) {
        super(new FormLayout(columnSpecs, rowSpecs));

        initComponents(new ChangeListener());

        CellConstraints cc = new CellConstraints();
        add(processLabel, cc.xyw(2, 2, 3));
        add(cancelButton, cc.xyw(3, 4, 1));

        this.facilitator = facilitator;
    }

    private void initComponents(ActionListener actionListener) {
        processLabel = new JLabel("Upload is in Process");
        Font defaultFont = processLabel.getFont();
        Font updatedFont = new Font(defaultFont.getFontName(),
                                    defaultFont.getStyle(),
                                    defaultFont.getSize()+10);
        processLabel.setFont(updatedFont);

        cancelButton = new JButton("Cancel Upload");
        URL cancelIcon =
            this.getClass().getClassLoader().getResource("cancel.png");
        cancelButton.setIcon(new ImageIcon(cancelIcon));
        cancelButton.addActionListener(actionListener);
    }

    private class ChangeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == cancelButton) {
                if(null != uploader) {
                    uploader.stopUpload();
                }
                completeUpload();
            }
        }
    }

    public void monitorStatus(Uploader uploader) {
        this.uploader = uploader;

        // TODO: Upldate display to indicate status
        // UploadStatus status = uploader.getUploadStatus();

        // TODO: Transition when upload is complete
        // completeUpload();
    }

    private void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(InterruptedException e) {
        }
    }

    private void completeUpload() {
        facilitator.completeUpload();
    }

}
