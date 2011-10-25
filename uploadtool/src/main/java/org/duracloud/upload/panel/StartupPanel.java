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
import org.duracloud.upload.UploadStatus;
import org.duracloud.upload.Uploader;

import javax.swing.*;
import java.awt.*;

/**
 * @author: Bill Branan
 * Date: 10/24/11
 */
public class StartupPanel extends JPanel {

    private JLabel startingLabel;
    private JProgressBar spinnerBar;

    private UploadFacilitator facilitator;
    private Uploader uploader;

    private static final String columnSpecs = // 4 columns
        "70dlu,20dlu,80dlu,20dlu";
    private static final String rowSpecs = // 4 rows
        "20dlu,pref,10dlu,pref";

    public StartupPanel(UploadFacilitator facilitator) {
        super(new FormLayout(columnSpecs, rowSpecs));

        initComponents();

        CellConstraints cc = new CellConstraints();
        add(startingLabel, cc.xyw(2, 2, 3));
        add(spinnerBar, cc.xyw(3, 4, 1));

        this.facilitator = facilitator;
    }

    private void initComponents() {
        startingLabel = new JLabel("Upload is Starting");
        Font defaultFont = startingLabel.getFont();
        Font updatedFont = new Font(defaultFont.getFontName(),
                                    defaultFont.getStyle(),
                                    defaultFont.getSize()+11);
        startingLabel.setFont(updatedFont);
        startingLabel.setHorizontalAlignment(JLabel.CENTER);

        spinnerBar = new JProgressBar();
        spinnerBar.setIndeterminate(true);
    }

    public void monitorStatus(Uploader uploader) {
        this.uploader = uploader;

        // Transition when upload is started
        Thread monitorThread = new Thread(new StatusMonitor());
        monitorThread.start();
    }

    private class StatusMonitor implements Runnable {
        @Override
        public void run() {
            UploadStatus status;
            do {
                wait(1000);
                status = uploader.getUploadStatus();
            } while(!status.isComplete() &&
                    status.getFilesInTransfer().size() <= 0);
            startupComplete();
        }

        private void wait(int milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch(InterruptedException e) {
            }
        }
    }

    private void startupComplete() {
        facilitator.showStatus();
    }

}