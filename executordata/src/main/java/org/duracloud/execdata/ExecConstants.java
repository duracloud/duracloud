/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.execdata;

/**
 * @author: Bill Branan
 * Date: 4/6/12
 */
public interface ExecConstants {

    // Exec
    public static final String HANDLER_STATE_SPACE = "x-duracloud-admin";
    public static final String ERROR_PREFIX = "Error: ";

    // Media Streaming
    public static final String MEDIA_STREAMER_NAME = "Media Streamer";
    public static final String SOURCE_SPACE_ID = "mediaSourceSpaceId";

    // Actions - Media Streaming
    public static final String START_STREAMING = "start-streaming";
    public static final String STOP_STREAMING = "stop-streaming";
    public static final String START_STREAMING_SPACE = "start-streaming-space";
    public static final String STOP_STREAMING_SPACE = "stop-streaming-space";

    // Actions - Bit Integrity
    public static final String START_BIT_INTEGRITY = "start-bit-integrity";
    public static final String CANCEL_BIT_INTEGRITY = "cancel-bit-integrity";

    // Actions - Image Server
    public static final String START_DUPLICATION = "start-duplication";
    public static final String ADD_TO_DUPLICATION = "add-to-duplication";
    public static final String REMOVE_FROM_DUPLICATION =
        "remove-from-duplication";
}
