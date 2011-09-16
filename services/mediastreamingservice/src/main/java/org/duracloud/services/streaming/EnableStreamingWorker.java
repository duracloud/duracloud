/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.streaming;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.ExceptionUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.util.MimetypeUtil;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Jun 3, 2010
 */
public class EnableStreamingWorker implements Runnable {

    private static final String ENABLE_STREAMING_TASK = "enable-streaming";
    private static final String SINGLE_PLAYER_NAME = "singleplayer.html";
    private static final String PLAYLIST_PLAYER_NAME = "playlistplayer.html";
    private static final String PLAYLIST_NAME = "playlist.xml";

    private final Logger log = LoggerFactory.getLogger(EnableStreamingWorker.class);

    private ContentStore contentStore;
    private String mediaViewerSpaceId;
    private String mediaSourceSpaceId;
    private PlaylistCreator playlistCreator;
    private File workDir;

    private boolean complete = false;
    private String streamHost = null;
    private String enableStreamingResult = null;
    private String error = null;

    public EnableStreamingWorker(ContentStore contentStore,
                                 String mediaViewerSpaceId,
                                 String mediaSourceSpaceId,
                                 PlaylistCreator playlistCreator,
                                 File workDir) {
        this.contentStore = contentStore;
        this.mediaViewerSpaceId = mediaViewerSpaceId;
        this.mediaSourceSpaceId = mediaSourceSpaceId;
        this.playlistCreator = playlistCreator;
        this.workDir = workDir;
    }

    @Override
    public void run() {
        try {
            createDistribution();
            File playlist = createPlaylist();
            createPlayers(playlist.getName());

            // Move files from work dir to media viewer space
            moveFilesToSpace(workDir.listFiles(),
                             contentStore,
                             mediaViewerSpaceId);
        } catch(Exception e) {
            log("Error encountered performing " + ENABLE_STREAMING_TASK +
                " task: " + e.getMessage(), e);
            error = e.getMessage();
        }
        complete = true;
    }

    public boolean isComplete() {
        return complete;
    }

    public String getStreamHost() {
        return streamHost;
    }

    public String getEnableStreamingResult() {
        return enableStreamingResult;
    }

    public String getError() {
        return error;        
    }

    public String getMediaSourceSpaceId() {
        return mediaSourceSpaceId;
    }

    /*
     * Create/enable distribution
     */
    private void createDistribution() throws ContentStoreException {
        String enableStreamingResponse =
            contentStore.performTask(ENABLE_STREAMING_TASK, mediaSourceSpaceId);
        Map<String, String> responseMap =
            SerializationUtil.deserializeMap(enableStreamingResponse);
        streamHost = responseMap.get("domain-name");
        enableStreamingResult = responseMap.get("results");
    }

    /*
     * Create playlist in work dir
     */
    private File createPlaylist() throws ContentStoreException {
        String playlistXml =
            playlistCreator.createPlaylist(contentStore, mediaSourceSpaceId);
        return storePlaylist(playlistXml, workDir);
    }

    private File storePlaylist(String playlistXml, File workDir) {
        File playlist =
            new File(workDir, mediaSourceSpaceId + "-" + PLAYLIST_NAME);

        FileOutputStream fileStream;
        try {
            fileStream = new FileOutputStream(playlist);
        } catch(FileNotFoundException e) {
            throw new RuntimeException("Unable to create playlist due to: " +
                                       e.getMessage());
        }

        OutputStreamWriter writer = new OutputStreamWriter(fileStream);
        try {
            writer.write(playlistXml, 0, playlistXml.length());
            writer.close();
        } catch(IOException e) {
            throw new RuntimeException("Unable to create playlist due to: " +
                                       e.getMessage());
        }

        return playlist;
    }

    /*
     * Replace variables in example player html files
     */
    private void createPlayers(String playlistName) throws ContentStoreException {
        String sampleMediaId = getIdFromSpace(contentStore, mediaSourceSpaceId);
        updatePlayers(workDir, streamHost, sampleMediaId, playlistName);
    }

    private String getIdFromSpace(ContentStore contentStore, String spaceId)
        throws ContentStoreException {
        Iterator<String> contents = contentStore.getSpaceContents(spaceId);
        String contentId = "";
        if(contents.hasNext()) {
            contentId = contents.next();
        }
        return contentId;
    }

    private void updatePlayers(File workDir,
                               String streamHost,
                               String sampleMediaId,
                               String playlistName) {
        File singlePlayerOrig = new File(workDir, SINGLE_PLAYER_NAME);
        File playlistPlayerOrig = new File(workDir, PLAYLIST_PLAYER_NAME);

        File singlePlayer =
            new File(workDir, mediaSourceSpaceId + "-" + SINGLE_PLAYER_NAME);
        File playlistPlayer =
            new File(workDir, mediaSourceSpaceId + "-" + PLAYLIST_PLAYER_NAME);

        try {
            FileUtils.copyFile(singlePlayerOrig, singlePlayer);
            FileUtils.copyFile(playlistPlayerOrig, playlistPlayer);
            IOUtil.fileFindReplace(singlePlayer, "$STREAM-HOST", streamHost);
            IOUtil.fileFindReplace(singlePlayer, "$MEDIA-FILE", sampleMediaId);
            IOUtil.fileFindReplace(playlistPlayer, "$STREAM-HOST", streamHost);
            IOUtil.fileFindReplace(playlistPlayer,
                                   "$PLAYLIST_NAME",
                                   playlistName);
        } catch(IOException e) {
            throw new RuntimeException("Unable to update player files due to: "
                                       + e.getMessage());
        }
    }

    private void moveFilesToSpace(File[] files,
                                  ContentStore contentStore,
                                  String spaceId)
        throws ContentStoreException, FileNotFoundException {
        MimetypeUtil mimeUtil = new MimetypeUtil();

        List<File> toAdd = new ArrayList<File>();
        for (File file : files) {
            String name = file.getName();
            if(!name.equals(SINGLE_PLAYER_NAME) &&
               !name.equals(PLAYLIST_PLAYER_NAME)) {
                toAdd.add(file);
            }
        }

        int maxloops = 20;
        int loops;
        for (loops = 0; !toAdd.isEmpty() && loops < maxloops; loops++) {
            File file = toAdd.remove(0);
            InputStream stream = new FileInputStream(file);
            try {
                contentStore.addContent(spaceId,
                                        file.getName(),
                                        stream,
                                        file.length(),
                                        mimeUtil.getMimeType(file),
                                        null,
                                        null);
            } catch (ContentStoreException e) {
                log(e.getMessage(), e);
                toAdd.add(file);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }

        if(loops == maxloops) {
            log("Unable to complete loading of files into " + spaceId);
        }
    }

    private void log(String logMsg) {
        log.warn(logMsg);
    }

    private void log(String logMsg, Exception e) {
        log.error(logMsg, e);
        log.error(ExceptionUtil.getStackTraceAsString(e));
    }
}
