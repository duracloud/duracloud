/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.client;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.text.Collator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Erik Paulsson
 *         Date: 3/20/14
 */
@Document(indexName="dc_multi", type="account")
public class AccountIndexItem {

    @Id
    private String id;  // this should be the account's hostname

    @Field(type = FieldType.Object)
    private Map<String, TreeSet<String>> storeSpaces = new HashMap();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, TreeSet<String>> getStoreSpaces() {
        return storeSpaces;
    }

    public void setStoreSpaces(Map<String, TreeSet<String>> storeSpaces) {
        this.storeSpaces = storeSpaces;
    }

    public Set<String> spacesForStore(Integer storeId) {
        return storeSpaces.get(storeId);
    }

    public AccountIndexItem addSpace(String storeId, String space) {
        if(space != null) {
            if(!storeSpaces.containsKey(storeId)) {
                addStore(storeId);
            }
            storeSpaces.get(storeId).add(space);
        }
        return this;
    }

    public void addSpaces(String storeId, Collection<String> spaces) {
        if(spaces != null && !spaces.isEmpty()) {
            if(!storeSpaces.containsKey(storeId)) {
                addStore(storeId);
            }
            storeSpaces.get(storeId).addAll(spaces);
        }
    }

    private void addStore(String storeId) {
        storeSpaces.put(storeId, new TreeSet<String>(Collator.getInstance()));
    }
}
