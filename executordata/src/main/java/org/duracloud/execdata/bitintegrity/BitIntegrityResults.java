/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.execdata.bitintegrity;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.HashMap;
import java.util.Map;

/**
 * Captures all bit integrity check results for all spaces within all
 * storage providers.
 *
 * @author: Bill Branan
 * Date: 3/20/12
 */
@XmlRootElement
public class BitIntegrityResults {

    @XmlValue
    private Map<String, StoreBitIntegrityResults> stores;

    public BitIntegrityResults() {
        stores = new HashMap<String, StoreBitIntegrityResults>();
    }

    public void addStoreResults(String storeId,
                                StoreBitIntegrityResults storeResults) {
        stores.put(storeId, storeResults);
    }

    public void addSpaceResult(String storeId,
                               String spaceId,
                               SpaceBitIntegrityResult result) {
        StoreBitIntegrityResults storeResults = stores.get(storeId);
        if(null == storeResults) {
            storeResults = new StoreBitIntegrityResults();
        }

        storeResults.addSpaceResult(spaceId, result);
        stores.put(storeId, storeResults);
    }

    public Map<String, StoreBitIntegrityResults> getStores() {
        return stores;
    }

    public void setStores(Map<String, StoreBitIntegrityResults> stores) {
        this.stores = stores;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BitIntegrityResults that = (BitIntegrityResults) o;

        if (stores != null ? !stores.equals(that.stores) :
            that.stores != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return stores != null ? stores.hashCode() : 0;
    }

}
