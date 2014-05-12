/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.client;

import static org.duracloud.contentindex.client.ContentIndexItem.ID_SEPARATOR;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.simpleQueryString;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.duracloud.common.collection.StreamingIterator;
import org.duracloud.contentindex.client.iterator.ESContentIndexClientContentIdIteratorSource;
import org.duracloud.contentindex.client.iterator.ESContentIndexClientContentIteratorSource;
import org.duracloud.storage.provider.StorageProvider;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

/**
 * @author Erik Paulsson
 *         Date: 3/11/14
 */
public class ESContentIndexClient implements ContentIndexClient {

    private static Logger log = LoggerFactory
        .getLogger(ESContentIndexClient.class);

    public static final String SHARED_INDEX = "dc_multi";
    public static final String TYPE_ACCOUNT = "account";
    public static final String TYPE_CONTENT = "content";

    private ElasticsearchOperations elasticSearchOps;
    private Client client;
    private int pageSize = 200;
    private Validator validator;

    public ESContentIndexClient(ElasticsearchOperations elasticSearchOps,
                                Client client) {
        this(elasticSearchOps, client, 200);
    }

    public ESContentIndexClient(ElasticsearchOperations elasticSearchOps,
                                Client client,
                                int pageSize) {
        this.elasticSearchOps = elasticSearchOps;
        this.client = client;
        this.pageSize = pageSize;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Returns the max number of records that con be contained in a "page".
     * @return the max number of records that can be contained in a "page" of a
     *   large result set.  A "page" is a subset of a larger result set.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the max number of records that can be contained in a "page".  A page
     * is a subset of a larger result set.
     * @param pageSize
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public Collection<String> getSpaces(String account, String storeId) {
        GetQuery q = new GetQuery();
        q.setId(account);
        AccountIndexItem item = elasticSearchOps
            .queryForObject(q, AccountIndexItem.class);
        return item.getStoreSpaces().get(storeId);
    }

    @Override
    public String save(AccountIndexItem item) {
        IndexQuery indexQuery = createIndexQuery(item);
        String id = elasticSearchOps.index(indexQuery);

        // refresh the index, to make this item searchable right away, GET by
        // ID does not require a refresh.  Automatic refreshes are done periodically.
        // @See: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/indices-refresh.html
        elasticSearchOps.refresh(AccountIndexItem.class, true);

        return id;
    }

    private IndexQuery createIndexQuery(AccountIndexItem item) {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setType(TYPE_ACCOUNT);
        indexQuery.setId(item.getId());
        indexQuery.setObject(item);
        return indexQuery;
    }

    @Override
    public Iterator<ContentIndexItem> getSpaceContents(String account,
                                                       String storeId,
                                                       String space) {
        return new StreamingIterator<ContentIndexItem>(
            new ESContentIndexClientContentIteratorSource(
                this, account, storeId, space));
    }

    @Override
    public List<ContentIndexItem> getSpaceContents(String account,
                                                   String storeId,
                                                   String space,
                                                   int pageNum,
                                                   int pageSize) {
        SearchQuery searchQuery = getSortedQueryForSpace(account, storeId,
                                                         space);
        searchQuery.setPageable(new PageRequest(pageNum, pageSize));
        Page<ContentIndexItem> page = elasticSearchOps
            .queryForPage(searchQuery, ContentIndexItem.class);

        List<ContentIndexItem> items = new ArrayList<>(page.getNumberOfElements());
        for(ContentIndexItem item: page.getContent()) {
            items.add(item);
        }
        return items;
    }

    @Override
    public Iterator<String> getSpaceContentIds(String account,
                                                     String storeId,
                                                     String space) {
        return new StreamingIterator<String>(
            new ESContentIndexClientContentIdIteratorSource(
                this, account, storeId, space));
    }

    @Override
    public List<String> getSpaceContentIds(String account,
                                              String storeId,
                                              String space,
                                              int pageNum,
                                              int pageSize) {
        SearchQuery searchQuery = getSortedQueryForSpace(account, storeId,
                                                         space);
        searchQuery.addFields("contentId");
        searchQuery.setPageable(new PageRequest(pageNum, pageSize));
        Page<ContentIndexItem> page = elasticSearchOps.queryForPage(searchQuery,
                                                                    ContentIndexItem.class);
        List<String> ids = new ArrayList<>(page.getNumberOfElements());
        for(ContentIndexItem item: page.getContent()) {
            ids.add(item.getContentId());
        }
        return ids;
    }

    @Override
    public Long getSpaceCount(String account, String storeId, String space) {
        SearchQuery searchQuery = getQueryForSpace(account, storeId, space);
        Long count = elasticSearchOps
            .count(searchQuery, ContentIndexItem.class);
        return count;
    }

    protected SearchQuery getSortedQueryForSpace(String account, String storeId,
                                                 String space) {
        SearchQuery searchQuery = getQueryForSpace(account, storeId, space);
        searchQuery.addSort(new Sort(Sort.Direction.ASC, "contentId"));
        return searchQuery;
    }

    protected SearchQuery getQueryForSpace(String account, String storeId,
                                           String space) {
        QueryBuilder queryBuilder = boolQuery()
            .must(termQuery("storeId", storeId))
            .must(termQuery("space", space));
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryBuilder).withTypes(TYPE_CONTENT).build();
        searchQuery.addIndices(account);
        return searchQuery;
    }

    /**
     * Search all field values for the provided 'text'
     *
     * @param text
     * @param account
     * @param storeId
     * @param space
     * @return
     */
    @Override
    public List<ContentIndexItem> getItemWithValue(String text, String account,
                                                   String storeId,
                                                   String space) {
        //FilterBuilders.
        TermFilterBuilder storeIdFilter = null;
        TermFilterBuilder spaceFilter = null;

        NativeSearchQueryBuilder nsqBuilder = new NativeSearchQueryBuilder()
            .withQuery(simpleQueryString(text)).withTypes(TYPE_CONTENT);
        if (account != null) {
            nsqBuilder.withIndices(account);
            if (storeId != null) {
                storeIdFilter = FilterBuilders.termFilter("storeId", storeId);
            }
        }

        if (space != null) {
            spaceFilter = FilterBuilders.termFilter("space", space);
        }

        if (storeIdFilter != null && spaceFilter != null) {
            nsqBuilder.withFilter(
                FilterBuilders.andFilter(storeIdFilter, spaceFilter));

        } else {
            if (storeIdFilter != null) {
                nsqBuilder.withFilter(storeIdFilter);
            } else if (spaceFilter != null) {
                nsqBuilder.withFilter(spaceFilter);
            }
        }

        List<ContentIndexItem> items = elasticSearchOps
            .queryForList(nsqBuilder.build(), ContentIndexItem.class);
        return items;
    }

    @Override
    public ContentIndexItem get(String account, String storeId, String space,
                                String contentId) {
        String id = account + ID_SEPARATOR + storeId + ID_SEPARATOR +
            space + ID_SEPARATOR + contentId;

        GetQuery q = new GetQuery();
        q.setId(id);
        ContentIndexItem item = elasticSearchOps
            .queryForObject(q, ContentIndexItem.class);
        return item;
    }

    @Override
    public String save(ContentIndexItem item)  throws ContentIndexClientValidationException {
        validate(item);
        IndexQuery indexQuery = createIndexQuery(item);
        String id = elasticSearchOps.index(indexQuery);

        // refresh the index.
        // @See: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/indices-refresh.html
        elasticSearchOps.refresh(item.getAccount(), true);

        return id;
    }

    private void validate(ContentIndexItem item) throws ContentIndexClientValidationException{
        if(item.getProps() != null){
            String key = StorageProvider.PROPERTIES_CONTENT_CHECKSUM;
            String checksum = item.getProps().get(key);
            if(checksum == null){
                throw new ContentIndexClientValidationException("The item properties must contain a non-null entry for the following key: "  + key + ";  item="+ item);
            }
        }
        
        Set<ConstraintViolation<ContentIndexItem>> results = validator.validate(item);
        
        if(results.size() > 0){
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to validate " + item + ":");
            for(ConstraintViolation<ContentIndexItem> violation : results){
                sb.append("\n\t");
                sb.append(violation.getMessage());
            }
            
            throw new ContentIndexClientValidationException(sb.toString());
        }
        
    }



    @Override
    public void bulkSave(List<ContentIndexItem> items) throws ContentIndexClientValidationException {
        if (!items.isEmpty()) {
            List<IndexQuery> queries = new ArrayList<>();
            for (ContentIndexItem item : items) {
                validate(item);
                queries.add(createIndexQuery(item));
            }
            elasticSearchOps.bulkIndex(queries);

            // refresh the index.
            // @See: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/indices-refresh.html
            elasticSearchOps.refresh(items.get(0).getAccount(), true);
        }
    }

    private IndexQuery createIndexQuery(ContentIndexItem item) {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setIndexName(item.getAccount());
        indexQuery.setType(TYPE_CONTENT);
        indexQuery.setId(item.getId());
        indexQuery.setObject(item);
        indexQuery.setVersion(item.getVersion());
        return indexQuery;
    }

    @Override
    public void addIndex(String index, boolean isAlias) {
        if (isAlias) {
            AliasAction aliasAction = new AliasAction(AliasAction.Type.ADD);
            aliasAction.alias(index).index(SHARED_INDEX).routing(index)
                       .filter(FilterBuilders.termFilter("account", index));

            client.admin().indices().prepareAliases()
                  .addAliasAction(aliasAction).execute().actionGet();

            // an alias can be updated to point to a new index by adding and
            // removing the alias in one request:
            /*
            client.admin().indices().prepareAliases()
                  .addAlias("my_index", "my_alias")
                  .removeAlias("my_old_index", "my_alias")
                  .execute().actionGet();
            */
        } else {
            client.admin().indices().prepareCreate(index).execute().actionGet();
        }
    }
}
