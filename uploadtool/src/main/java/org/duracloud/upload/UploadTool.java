/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.upload;

import org.duracloud.upload.panel.CompletedPanel;
import org.duracloud.upload.panel.ConnectionPanel;
import org.duracloud.upload.panel.SelectionPanel;
import org.duracloud.upload.panel.StartupPanel;
import org.duracloud.upload.panel.StatusPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 10/13/11
 */
public class UploadTool extends JPanel implements UploadFacilitator {

    private String host;
    private int port;
    private String username;
    private String password;
    private String spaceId;
    private String storeId;

    private static final String CONNECTION_PANEL = "connectionPanel";
    private static final String SELECTION_PANEL = "selectionPanel";
    private static final String STARTUP_PANEL = "startupPanel";
    private static final String STATUS_PANEL = "statusPanel";
    private static final String COMPLETED_PANEL = "completedPanel";

    private Uploader uploader;
    private StartupPanel startupPanel;
    private StatusPanel statusPanel;

    /**
     * Starts the upload tool in a mode which will ask the user for connection
     * parameters. This is for stand-alone use of the tool.
     */
    public UploadTool() {
        super(new CardLayout());
        addToolComponents();
    }

    private void addToolComponents() {
        this.add(new ConnectionPanel(this), CONNECTION_PANEL);
        this.add(new SelectionPanel(this), SELECTION_PANEL);
        this.startupPanel = new StartupPanel(this);
        this.add(startupPanel, STARTUP_PANEL);
        this.statusPanel = new StatusPanel(this);
        this.add(statusPanel, STATUS_PANEL);
        this.add(new CompletedPanel(this), COMPLETED_PANEL);
    }

    /**
     * Starts the upload tool by passing in connection params, so the user
     * will not be asked for this information. This is for embedded use of
     * the tool (applet).
     *
     * @param host at which DuraCloud can be found
     * @param username necessary to connect to DuraCloud
     * @param password necessary to connect to DuraCloud
     * @param spaceId location to which content will be uploaded
     */
    public UploadTool(String host,
                      int port,
                      String username,
                      String password,
                      String spaceId,
                      String storeId) {
        this();
        connect(host, port, username, password, spaceId, storeId);
    }

    private void setViewPanel(String panelId) {
        CardLayout layout = (CardLayout)getLayout();
        layout.show(this, panelId);
    }

    @Override
    public void connect(String host,
                        int port,
                        String username,
                        String password,
                        String spaceId,
                        String storeId) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.spaceId = spaceId;
        this.storeId = storeId;

        setViewPanel(SELECTION_PANEL);
    }

    @Override
    public void startUpload(List<File> items) {
        try {
            setViewPanel(STARTUP_PANEL);
            uploader = new Uploader(host,
                                    port,
                                    username,
                                    password,
                                    spaceId,
                                    storeId,
                                    items);
            uploader.startUpload();
            startupPanel.monitorStatus(uploader);
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    @Override
    public void showStatus() {
        statusPanel.monitorStatus(uploader);
        setViewPanel(STATUS_PANEL);
    }

    @Override
    public void completeUpload() {
        setViewPanel(COMPLETED_PANEL);
    }

    @Override
    public void restart() {
        this.removeAll();
        addToolComponents();
        setViewPanel(SELECTION_PANEL);
    }

    @Override
    public void exit() {
        System.exit(0);
    }

}
