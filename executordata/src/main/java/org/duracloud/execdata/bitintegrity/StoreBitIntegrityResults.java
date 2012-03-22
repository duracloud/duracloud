/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.execdata.bitintegrity;

import javax.xml.bind.annotation.XmlValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Captures all bit integrity check results for all spaces within a single
 * storage provider.
 *
 * @author: Bill Branan
 * Date: 3/20/12
 */
public class StoreBitIntegrityResults {

    @XmlValue
    private Map<String, List<SpaceBitIntegrityResult>> spaces;

    public StoreBitIntegrityResults() {
        spaces = new HashMap<String, List<SpaceBitIntegrityResult>>();
    }

    public List<SpaceBitIntegrityResult> getSpaceResults(String spaceId) {
        List<SpaceBitIntegrityResult> results = spaces.get(spaceId);
        if(null == results) {
            results = new ArrayList<SpaceBitIntegrityResult>();
        }
        return results;
    }

    public void addSpaceResult(String spaceId, SpaceBitIntegrityResult result) {
        List<SpaceBitIntegrityResult> resultList = spaces.get(spaceId);
        if(null == resultList) {
            resultList = new ArrayList<SpaceBitIntegrityResult>();
        }

        resultList.add(0, result);
        spaces.put(spaceId, resultList);
    }

    public Map<String, List<SpaceBitIntegrityResult>> getSpaces() {
        return spaces;
    }

    public void setSpaces(Map<String, List<SpaceBitIntegrityResult>> spaces) {
        this.spaces = spaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StoreBitIntegrityResults that = (StoreBitIntegrityResults) o;

        if (spaces != null ? !spaces.equals(that.spaces) :
            that.spaces != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return spaces != null ? spaces.hashCode() : 0;
    }

}
