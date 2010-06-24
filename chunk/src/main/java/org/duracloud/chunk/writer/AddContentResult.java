/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.writer;

/**
 * @author Andrew Woods
 *         Date: Feb 15, 2010
 */
public class AddContentResult {
    private String spaceId;
    private String contentId;
    private String md5 = "not-found";
    private long contentSize;
    private State state = State.UNKNOWN;

    public AddContentResult(String spaceId,
                            String contentId,
                            long contentSize) {
        this.spaceId = spaceId;
        this.contentId = contentId;
        this.contentSize = contentSize;
    }

    public static enum State {
        SUCCESS, ERROR, IGNORED, UNKNOWN;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public String getContentId() {
        return contentId;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getContentSize() {
        return contentSize;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

}
