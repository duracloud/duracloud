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
import org.apache.commons.io.FileUtils;
import org.duracloud.upload.UploadFacilitator;
import org.duracloud.upload.UploadStatus;
import org.duracloud.upload.Uploader;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 10/19/11
 */
public class StatusPanel extends JPanel {

    private JLabel file1NameLabel;
    private JLabel file1StatusLabel;
    private JLabel file1TotalLabel;
    private JLabel file2NameLabel;
    private JLabel file2StatusLabel;
    private JLabel file2TotalLabel;
    private JLabel file3NameLabel;
    private JLabel file3StatusLabel;
    private JLabel file3TotalLabel;
    private JLabel uploadNameLabel;
    private JLabel uploadStatusLabel;
    private JLabel uploadTotalLabel;
    private JProgressBar file1ProgressBar;
    private JProgressBar file2ProgressBar;
    private JProgressBar file3ProgressBar;
    private JProgressBar uploadProgressBar;
    private JSeparator separator;
    private JButton cancelButton;

    private UploadFacilitator facilitator;
    private Uploader uploader;

    private static final String columnSpecs = // 9 columns
        "3dlu,90dlu:grow,2dlu,right:30dlu,2dlu,100dlu,2dlu,30dlu,3dlu";
    private static final String rowSpecs = // 11 rows
        "5dlu,15dlu,3dlu,15dlu,3dlu,15dlu,10dlu,15dlu,10dlu:grow,pref,3dlu";

    public StatusPanel(UploadFacilitator facilitator) {
        super(new FormLayout(columnSpecs, rowSpecs));

        initComponents(new ChangeListener());

        CellConstraints cc = new CellConstraints();
        add(file1NameLabel,    cc.xyw(2, 2, 1));
        add(file1StatusLabel,  cc.xyw(4, 2, 1));
        add(file1TotalLabel,   cc.xyw(8, 2, 1));
        add(file2NameLabel,    cc.xyw(2, 4, 1));
        add(file2StatusLabel,  cc.xyw(4, 4, 1));
        add(file2TotalLabel,   cc.xyw(8, 4, 1));
        add(file3NameLabel,    cc.xyw(2, 6, 1));
        add(file3StatusLabel,  cc.xyw(4, 6, 1));
        add(file3TotalLabel,   cc.xyw(8, 6, 1));
        add(uploadNameLabel,   cc.xyw(2, 8, 1));
        add(uploadStatusLabel, cc.xyw(4, 8, 1));
        add(uploadTotalLabel,  cc.xyw(8, 8, 1));

        add(file1ProgressBar,  cc.xyw(6, 2, 1));
        add(file2ProgressBar,  cc.xyw(6, 4, 1));
        add(file3ProgressBar,  cc.xyw(6, 6, 1));
        add(uploadProgressBar, cc.xyw(6, 8, 1));

        add(separator,         cc.xyw(2, 7, 7));
        add(cancelButton,      cc.xyw(6, 10, 3));

        this.facilitator = facilitator;
    }

    private void initComponents(ActionListener actionListener) {
        file1NameLabel = new JLabel("");
        file1StatusLabel = new JLabel("");
        file1TotalLabel = new JLabel("");
        file2NameLabel = new JLabel("");
        file2StatusLabel = new JLabel("");
        file2TotalLabel = new JLabel("");
        file3NameLabel = new JLabel("");
        file3StatusLabel = new JLabel("");
        file3TotalLabel = new JLabel("");
        uploadNameLabel = new JLabel("Files Uploaded");
        uploadStatusLabel = new JLabel("");
        uploadTotalLabel = new JLabel("");

        file1ProgressBar = new JProgressBar(0, 1);
        file1ProgressBar.setVisible(false);
        file2ProgressBar = new JProgressBar(0, 1);
        file2ProgressBar.setVisible(false);
        file3ProgressBar = new JProgressBar(0, 1);
        file3ProgressBar.setVisible(false);
        uploadProgressBar = new JProgressBar(0, 1);

        separator = new JSeparator();

        cancelButton = new JButton("Cancel Upload");
        URL cancelIcon =
            this.getClass().getClassLoader().getResource("cancel.png");
        cancelButton.setIcon(new ImageIcon(cancelIcon));
        cancelButton.addActionListener(actionListener);
    }

    private void updateStatusView(UploadStatus status) {
        int complete = status.getCompleteFiles();
        int total = status.getTotalFiles();

        uploadStatusLabel.setText(String.valueOf(complete));
        uploadTotalLabel.setText(String.valueOf(total));
        uploadProgressBar.setMaximum(total);
        uploadProgressBar.setValue(complete);
        uploadProgressBar.setStringPainted(true);

        List<UploadStatus.FileInTransfer> files = status.getFilesInTransfer();
        int numFiles = files.size();
        if(numFiles > 0) {
            UploadStatus.FileInTransfer fit = files.get(0);
            file1NameLabel.setText(fit.getName());
            file1StatusLabel.setText(
                FileUtils.byteCountToDisplaySize(fit.getBytesRead()));
            file1TotalLabel.setText(
                FileUtils.byteCountToDisplaySize(fit.getTotalSize()));
            file1ProgressBar.setMaximum(new Long(fit.getTotalSize()).intValue());
            file1ProgressBar.setValue(new Long(fit.getBytesRead()).intValue());
            file1ProgressBar.setVisible(true);
            file1ProgressBar.setStringPainted(true);
        }  else {
            file1NameLabel.setText("");
            file1StatusLabel.setText("");
            file1TotalLabel.setText("");
            file1ProgressBar.setMaximum(1);
            file1ProgressBar.setValue(0);
            file1ProgressBar.setVisible(false);
            file1ProgressBar.setStringPainted(true);
        }

        if(numFiles > 1) {
            UploadStatus.FileInTransfer fit = files.get(1);
            file2NameLabel.setText(fit.getName());
            file2StatusLabel.setText(
                FileUtils.byteCountToDisplaySize(fit.getBytesRead()));
            file2TotalLabel.setText(
                FileUtils.byteCountToDisplaySize(fit.getTotalSize()));
            file2ProgressBar.setMaximum(new Long(fit.getTotalSize()).intValue());
            file2ProgressBar.setValue(new Long(fit.getBytesRead()).intValue());
            file2ProgressBar.setVisible(true);
            file2ProgressBar.setStringPainted(true);
        } else {
            file2NameLabel.setText("");
            file2StatusLabel.setText("");
            file2TotalLabel.setText("");
            file2ProgressBar.setMaximum(1);
            file2ProgressBar.setValue(0);
            file2ProgressBar.setVisible(false);
            file2ProgressBar.setStringPainted(true);
        }

        if(numFiles > 2) {
            UploadStatus.FileInTransfer fit = files.get(2);
            file3NameLabel.setText(fit.getName());
            file3StatusLabel.setText(
                FileUtils.byteCountToDisplaySize(fit.getBytesRead()));
            file3TotalLabel.setText(
                FileUtils.byteCountToDisplaySize(fit.getTotalSize()));
            file3ProgressBar.setMaximum(new Long(fit.getTotalSize()).intValue());
            file3ProgressBar.setValue(new Long(fit.getBytesRead()).intValue());
            file3ProgressBar.setVisible(true);
            file3ProgressBar.setStringPainted(true);
        } else {
            file3NameLabel.setText("");
            file3StatusLabel.setText("");
            file3TotalLabel.setText("");
            file3ProgressBar.setMaximum(1);
            file3ProgressBar.setValue(0);
            file3ProgressBar.setVisible(false);
            file3ProgressBar.setStringPainted(true);
        }
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

        // Transition when upload is complete
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
                updateStatusView(status);
            } while(!status.isComplete());
            completeUpload();
        }

        private void wait(int milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch(InterruptedException e) {
            }
        }
    }

    private void completeUpload() {
        facilitator.completeUpload();
    }

}
