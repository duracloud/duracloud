/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto;

import java.util.Date;

/**
 * Contains information about a single history item which was included in a snapshot.
 *
 * @author Gad Krumholz Date: 6/05/15
 */
public class SnapshotHistoryItem extends BaseDTO {

    private Date historyDate;
    private String history;

    public Date getHistoryDate() {
        return historyDate;
    }

    public void setHistoryDate(Date historyDate) {
        this.historyDate = historyDate;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }
}
