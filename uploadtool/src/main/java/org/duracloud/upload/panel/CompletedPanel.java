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
import org.duracloud.sync.mgmt.SyncSummary;
import org.duracloud.upload.UploadFacilitator;
import org.duracloud.upload.Uploader;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 10/19/11
 */
public class CompletedPanel extends JPanel {

    private JLabel completeLabel;
    private JLabel successLabel;
    private JLabel failedLabel;
    private JButton failedButton;
    private JButton exitButton;

    private UploadFacilitator facilitator;
    private String failedItems;

    private static final String columnSpecs = // 7 columns
        "115dlu,10dlu,40dlu,60dlu,40dlu,10dlu,115dlu";
    private static final String rowSpecs = // 8 rows
        "15dlu,pref,5dlu,pref,5dlu,pref,10dlu,pref";

    public CompletedPanel(UploadFacilitator facilitator) {
        super(new FormLayout(columnSpecs, rowSpecs));

        initComponents(new ChangeListener());

        CellConstraints cc = new CellConstraints();
        add(completeLabel, cc.xyw(3, 2, 3));
        add(successLabel, cc.xyw(3, 4, 3));
        add(failedLabel, cc.xyw(3, 6, 2));
        add(failedButton, cc.xyw(5, 6, 1));
        add(exitButton, cc.xyw(4, 8, 1));

        this.facilitator = facilitator;
        this.failedItems = "";
    }

    private void initComponents(ActionListener actionListener) {
        completeLabel = new JLabel("Upload is Complete");
        Font defaultFont = completeLabel.getFont();
        Font updatedFont = new Font(defaultFont.getFontName(),
                                    defaultFont.getStyle(),
                                    defaultFont.getSize()+11);
        completeLabel.setFont(updatedFont);
        completeLabel.setHorizontalAlignment(JLabel.CENTER);

        successLabel = new JLabel();
        successLabel.setHorizontalAlignment(JLabel.CENTER);
        failedLabel = new JLabel();
        failedLabel.setHorizontalAlignment(JLabel.CENTER);

        failedButton = new JButton("View");
        failedButton.addActionListener(actionListener);
        failedButton.setVisible(false);

        exitButton = new JButton("Close");
        URL exitIcon =
            this.getClass().getClassLoader().getResource("close.png");
        exitButton.setIcon(new ImageIcon(exitIcon));
        exitButton.addActionListener(actionListener);
    }

    public void displayResults(Uploader uploader) {
        long successful = uploader.getSuccessfulTransfers();
        successLabel.setText(successful + " files uploaded successfully");

        List<SyncSummary> failed = uploader.getFailedTransfers();
        if(null != failed && failed.size() > 0) {
            failedLabel.setText(failed.size() + " files failed to upload");
            for(SyncSummary summary : failed) {
                failedItems += (summary.getAbsolutePath() + "\n");
            }
            failedButton.setVisible(true);
        }
    }

    private class ChangeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == exitButton) {
                facilitator.exit();
            } else if(e.getSource() == failedButton) {
                JTextArea textArea = new JTextArea(failedItems);
                textArea.setLineWrap(false);
                textArea.setMargin(new Insets(5,5,5,5));
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane();
                scrollPane.setPreferredSize(new Dimension(500,200));
                scrollPane.getViewport().setView(textArea);
                displayMessage(scrollPane, "Files which failed to upload");
            }
        }
    }

    private void displayMessage(Object msg, String title) {
        int type = JOptionPane.WARNING_MESSAGE;
        JOptionPane.showMessageDialog(this, msg, title, type);
    }

}
