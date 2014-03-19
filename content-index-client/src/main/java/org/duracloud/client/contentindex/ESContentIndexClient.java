/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.contentindex;

import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.util.ArrayList;
import java.util.List;

import static org.duracloud.client.contentindex.ContentIndexItem.ID_SEPARATOR;

import static org.elasticsearch.index.query.QueryBuilders.*;
/**
 * @author Erik Paulsson
 *         Date: 3/11/14
 */
public class ESContentIndexClient implements ContentIndexClient {

    private static Logger log = LoggerFactory
        .getLogger(ESContentIndexClient.class);

    public static final String SHARED_INDEX = "dc_multi";
    public static final String TYPE = "content";

    private ElasticsearchOperations elasticSearchOps;
    private Client client;

    public ESContentIndexClient(ElasticsearchOperations elasticSearchOps,
                                Client client) {
        this.elasticSearchOps = elasticSearchOps;
        this.client = client;
    }

    @Override
    public List<ContentIndexItem> getSpaceContents(String account, int storeId,
                                                   String space) {
        SearchQuery searchQuery = getSortedQueryForSpace(account, storeId,
                                                         space);
        List<ContentIndexItem> items = elasticSearchOps.queryForList(
            searchQuery, ContentIndexItem.class);
        return items;
    }

    @Override
    public List<ContentIndexItem> getSpaceContentIds(String account,
                                                     int storeId,
                                                     String space) {
        SearchQuery searchQuery = getSortedQueryForSpace(account, storeId,
                                                         space);
        searchQuery.addFields("contentId");
        List<ContentIndexItem> items = elasticSearchOps.queryForList(
            searchQuery, ContentIndexItem.class);
        return items;
    }

    @Override
    public Long getSpaceCount(String account, int storeId, String space) {
        SearchQuery searchQuery = getQueryForSpace(account, storeId, space);
        Long count = elasticSearchOps.count(searchQuery, ContentIndexItem.class);
        return count;
    }

    protected SearchQuery getSortedQueryForSpace(String account,
                                                 int storeId,
                                                 String space) {
        SearchQuery searchQuery = getQueryForSpace(account, storeId, space);
        //searchQuery.addSort(new Sort(Sort.Direction.ASC, "contentId"));
        //searchQuery.addSort((new Sort(new Sort.Order(Sort.Direction.ASC, "contentId"))));
        return searchQuery;
    }

    protected SearchQuery getQueryForSpace(String account,
                                           int storeId,
                                           String space) {
        QueryBuilder queryBuilder = boolQuery()
            .must(termQuery("storeId", storeId))
            .must(termQuery("space", space));
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(queryBuilder)
            .withTypes(TYPE)
            .withSort(new FieldSortBuilder("contentId").ignoreUnmapped(true).order(SortOrder.ASC))
            .build();
        searchQuery.addIndices(account);
        return searchQuery;
    }

    @Override
    public ContentIndexItem get(String account, int storeId,
                                                String space,
                                                String contentId) {
        String id = account + ID_SEPARATOR + storeId + ID_SEPARATOR +
            space + ID_SEPARATOR + contentId;

        GetQuery q = new GetQuery();
        q.setId(id);
        ContentIndexItem item = elasticSearchOps.queryForObject(
            q, ContentIndexItem.class);
        return item;
    }

    @Override
    public String save(ContentIndexItem item) {
        IndexQuery indexQuery = createIndexQuery(item);
        String id = elasticSearchOps.index(indexQuery);

        // refresh the index.
        // @See: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/indices-refresh.html
        elasticSearchOps.refresh(item.getAccount(), true);

        return id;
    }

    @Override
    public void bulkSave(List<ContentIndexItem> items) {
        if(! items.isEmpty()) {
            List<IndexQuery> queries = new ArrayList();
            for(ContentIndexItem item: items) {
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
        indexQuery.setType(TYPE);
        indexQuery.setId(item.getId());
        indexQuery.setObject(item);
        return indexQuery;
    }

    @Override
    public void addIndex(String index, boolean isAlias) {
        if(isAlias) {
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
