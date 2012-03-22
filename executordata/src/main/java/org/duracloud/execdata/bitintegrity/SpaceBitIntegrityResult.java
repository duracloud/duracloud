/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.execdata.bitintegrity;

import javax.xml.bind.annotation.XmlElement;
import java.util.Date;

/**
 * Captures the results of a single bit integrity check on a space.
 *
 * @author: Bill Branan
 * Date: 3/20/12
 */
public class SpaceBitIntegrityResult implements Comparable<SpaceBitIntegrityResult> {

    @XmlElement
    private Date completionDate;

    @XmlElement
    private String result;

    @XmlElement
    private String reportContentId;

    @XmlElement
    private boolean display;

    // Required for JAXB
    public SpaceBitIntegrityResult() {
    }

    public SpaceBitIntegrityResult(Date completionDate,
                                   String result,
                                   String reportContentId,
                                   boolean display) {
        this.completionDate = completionDate;
        this.result = result;
        this.reportContentId = reportContentId;
        this.display = display;
    }

    @Override
    public int compareTo(SpaceBitIntegrityResult o) {
        return completionDate.compareTo(o.completionDate);
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getReportContentId() {
        return reportContentId;
    }

    public void setReportContentId(String reportContentId) {
        this.reportContentId = reportContentId;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SpaceBitIntegrityResult that = (SpaceBitIntegrityResult) o;

        if (display != that.display) {
            return false;
        }
        if (completionDate != null ? !completionDate
            .equals(that.completionDate) : that.completionDate != null) {
            return false;
        }
        if (reportContentId != null ? !reportContentId
            .equals(that.reportContentId) : that.reportContentId != null) {
            return false;
        }
        if (result != null ? !result.equals(that.result) :
            that.result != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result1 = completionDate != null ? completionDate.hashCode() : 0;
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 +
            (reportContentId != null ? reportContentId.hashCode() : 0);
        result1 = 31 * result1 + (display ? 1 : 0);
        return result1;
    }

}
