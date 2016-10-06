/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui;

import java.awt.AWTException;
import java.awt.Dialog.ModalityType;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.security.ProtectionDomain;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.duracloud.common.util.WaitUtil;
import org.duracloud.syncui.config.SyncUIConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncUIDriver {
    private static int port;
    private static String contextPath;
    private final static Logger log =
        LoggerFactory.getLogger(org.duracloud.syncui.SyncUIDriver.class);

    public static void main(String[] args) throws Exception {
        String url = "http://localhost:" + SyncUIConfig.getPort() +
                     SyncUIConfig.getContextPath();
        CloseableHttpClient client = HttpClients.createDefault();

        if(isAppRunning(url, client)) {
            log.info("Sync Application already running, launching browser...");
            launchBrowser(url);
        } else {
            log.info("Sync Application not yet running, launching server...");
            launchServer(url, client);
        }
    }

    private static void launchServer(final String url,
                                     final CloseableHttpClient client) {
        try {
            final JDialog dialog = new JDialog();
            dialog.setSize(new java.awt.Dimension(400, 75));
            dialog.setModalityType(ModalityType.MODELESS);
            dialog.setTitle("DuraCloud Sync");
            dialog.setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            final JLabel label = new JLabel("Loading...");
            final JProgressBar progress = new JProgressBar();
            progress.setStringPainted(true);

            panel.add(label);
            panel.add(progress);
            dialog.add(panel);
            dialog.setVisible(true);

            port = SyncUIConfig.getPort();
            contextPath = SyncUIConfig.getContextPath();
            Server srv = new Server(port);
            
            ProtectionDomain protectionDomain =
                org.duracloud.syncui.SyncUIDriver.class.getProtectionDomain();
            String warFile =
                protectionDomain.getCodeSource().getLocation().toExternalForm();
            log.debug("warfile: {}", warFile);
            final WebAppContext context = new WebAppContext();
            context.setContextPath(contextPath);
            context.setAttribute("extractWAR", Boolean.FALSE);
            context.setWar(warFile);
            srv.setHandler(context);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    createSysTray(url, srv);

                    while (true) {
                        if (progress.getValue() < 100) {
                            progress.setValue(progress.getValue() + 3);
                        }

                        sleep(2000);
                        if(isAppRunning(url, client)) {
                            break;
                        }
                    }

                    progress.setValue(100);

                    label.setText("Launching browser...");
                    launchBrowser(url);
                    dialog.setVisible(false);
                }
            }).start();

            srv.start();

            srv.join();

        } catch (Exception e) {
            log.error("Error launching server: " + e.getMessage(), e);
        }
    }

    private static boolean isAppRunning(String url, CloseableHttpClient client) {
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response;
        int responseCode;
        try {
            response = client.execute(get);
            responseCode = response.getStatusLine().getStatusCode();
        } catch(IOException e) {
            log.debug("Attempt to connect to synctool app at url " + url +
                      " failed due to: " + e.getMessage());
            responseCode = 0;
        }
        log.debug("Response from {}: {}", url, responseCode);
        return responseCode == 200;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
        }
    }

    private static void createSysTray(final String url, final Server srv) {
        final TrayIcon trayIcon;
        try {

            if (SystemTray.isSupported()) {

                SystemTray tray = SystemTray.getSystemTray();
                InputStream is =  org.duracloud.syncui.SyncUIDriver.class.getClassLoader().getResourceAsStream("tray.png");
                
                Image image = ImageIO.read(is);
                MouseListener mouseListener = new MouseListener() {

                    public void mouseClicked(MouseEvent e) {
                        log.debug("Tray Icon - Mouse clicked!");
                    }

                    public void mouseEntered(MouseEvent e) {
                        log.debug("Tray Icon - Mouse entered!");
                    }

                    public void mouseExited(MouseEvent e) {
                        log.debug("Tray Icon - Mouse exited!");
                    }

                    public void mousePressed(MouseEvent e) {
                        log.debug("Tray Icon - Mouse pressed!");
                    }

                    public void mouseReleased(MouseEvent e) {
                        log.debug("Tray Icon - Mouse released!");
                    }
                };

                ActionListener exitListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        log.info("Exiting...");
                        try {
                            srv.stop();
                            while(!srv.isStopped()){
                                WaitUtil.wait(1);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        
                        System.exit(0);
                    }
                };

                PopupMenu popup = new PopupMenu();
                MenuItem view = new MenuItem("View Status");
                view.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        launchBrowser(url);
                    }
                });
                popup.add(view);

                MenuItem exit = new MenuItem("Exit");
                exit.addActionListener(exitListener);
                popup.add(exit);

                trayIcon = new TrayIcon(image, "DuraCloud Sync Tool", popup);

                ActionListener actionListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        trayIcon.displayMessage("Action Event",
                                                "An Action Event Has Been Performed!",
                                                TrayIcon.MessageType.INFO);
                    }
                };

                trayIcon.setImageAutoSize(true);
                trayIcon.addActionListener(actionListener);
                trayIcon.addMouseListener(mouseListener);

                try {
                    tray.add(trayIcon);
                } catch (AWTException e) {
                    log.error("TrayIcon could not be added.");
                }
            } else {
                log.warn("System Tray is not supported.");
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

    }

    private static void launchBrowser(final String url) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            log.warn("Desktop is not supported. Unable to open");

        } else {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                log.warn("Desktop doesn't support the browse action.");
            } else {
                java.net.URI uri;
                try {
                    uri = new java.net.URI(url);
                    desktop.browse(uri);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

}