/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.contentindex;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Erik Paulsson
 *         Date: 3/11/14
 */
@Document(indexName="dc_multi", type="content")
public class ContentIndexItem {

    public static final String ID_SEPARATOR = "/";

    @Id
    private String id;

    private String account;
    private Integer storeId;
    private String storeType;
    private String space;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String contentId;

    private Map<String, String> props;
    private List<String> tags;

    /**
     * Required empty constructor for spring-data-elasticsearch to do the
     * mapping from JSON document to ContentIndexItem object
     * Application developers should use the non-empty constructor.
     */
    public ContentIndexItem() {}

    public ContentIndexItem(String account, Integer storeId,
                            String space, String contentId) {
        this.id = account + ID_SEPARATOR + storeId + ID_SEPARATOR +
            space + ID_SEPARATOR + contentId;
        this.account = account;
        this.storeId = storeId;
        this.space = space;
        this.contentId = contentId;
    }

    public String getId() {
        return id;
    }

    /**
     * Application developers should avoid using this method.  The id for this
     * object will be automatically generated from the 'account', 'storeId',
     * 'space', and 'contentId' fields.
     * Application developers should use the non-empty constructor which will
     * automatically assign the 'id' field.
     * This method needs to exist to allow for the mapping / setting of
     * persistent fields in the datastore to object fields.
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public ContentIndexItem addProp(String key, String value) {
        if(props == null) {
            props = new HashMap<String, String>();
        }
        props.put(key, value);
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public ContentIndexItem addTag(String tag) {
        if(tags == null) {
            tags = new ArrayList<String>();
        }
        tags.add(tag);
        return this;
    }
}
