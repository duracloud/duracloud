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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 10/19/11
 */
public class SelectionPanel extends JPanel {

    private JTable itemTable;
    private DefaultTableModel itemTableModel;
    private JButton addItemButton;
    private JButton removeItemButton;
    private JButton uploadButton;
    private JFileChooser fileChooser;
    private UploadFacilitator facilitator;

    private static final String columnSpecs = // 7 columns
        "3dlu,pref,3dlu,pref,3dlu:grow,pref,3dlu";
    private static final String rowSpecs = // 5 rows
        "3dlu,90dlu:grow,3dlu,pref,3dlu";

    public SelectionPanel(UploadFacilitator facilitator) {
        super(new FormLayout(columnSpecs, rowSpecs));

        initComponents(new ChangeListener());

        JScrollPane tablePane = new JScrollPane(itemTable);

        CellConstraints cc = new CellConstraints();
        add(tablePane, cc.xyw(2, 2, 5));
        add(addItemButton, cc.xy(2, 4));
        add(removeItemButton, cc.xy(4, 4));
        add(uploadButton, cc.xy(6, 4));

        this.facilitator = facilitator;
    }

    private void initComponents(ActionListener actionListener) {
        String[] itemColumnNames = {"Name", "Size", "Location"};
        itemTableModel = new DefaultTableModel(itemColumnNames, 0);
        itemTable = new JTable(itemTableModel);

        addItemButton = new JButton("Add Files and Folders");
        URL addIcon = this.getClass().getClassLoader().getResource("add.png");
        addItemButton.setIcon(new ImageIcon(addIcon));
        addItemButton.addActionListener(actionListener);

        removeItemButton = new JButton("Remove Selected");
        URL removeIcon =
            this.getClass().getClassLoader().getResource("minus.png");
        removeItemButton.setIcon(new ImageIcon(removeIcon));
        removeItemButton.addActionListener(actionListener);

        uploadButton = new JButton("Start Upload");
        URL startUploadIcon =
            this.getClass().getClassLoader().getResource("arrow_up.png");
        uploadButton.setIcon(new ImageIcon(startUploadIcon));
        uploadButton.addActionListener(actionListener);

        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);
    }

    private class ChangeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == addItemButton) {
                int returnVal = fileChooser.showOpenDialog(itemTable);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    addItemsToList(fileChooser.getSelectedFiles());
                }
            } else if(e.getSource() == removeItemButton) {
                int[] selectedRows = itemTable.getSelectedRows();
                Arrays.sort(selectedRows);
                for(int i = selectedRows.length-1; i >= 0; i--) {
                    itemTableModel.removeRow(selectedRows[i]);
                }
            } else if(e.getSource() == uploadButton) {
                List<File> items = new ArrayList();
                int rowCount = itemTableModel.getRowCount();
                for(int i=0; i<rowCount; i++) {
                    String path =
                        String.valueOf(itemTableModel.getValueAt(i, 2));
                    File item = new File(path);
                    if(item.exists()) {
                        items.add(item);
                    }
                }
                facilitator.startUpload(items);
            }
        }
    }

    private void addItemsToList(File[] items) {
        for(File item : items) {
            // Add to itemTable
            String name = item.getName();
            String size = String.valueOf(item.length());
            String location = item.getAbsolutePath();
            if(item.isDirectory()) {
                name = name + " (folder)";
                size = FileUtils.byteCountToDisplaySize(
                    FileUtils.sizeOfDirectory(item));
            }
            if(!isDuplicate(location)) {
                itemTableModel.addRow(new String[]{name, size, location});
            }
        }
    }

    public boolean isDuplicate(String addedPath) {
        int rowCount = itemTableModel.getRowCount();
        for(int i=0; i<rowCount; i++) {
            String itemPath = String.valueOf(itemTableModel.getValueAt(i, 2));
            if(itemPath.equals(addedPath)) {
                return true;
            }
        }
        return false;
    }

}
