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
import java.util.List;

import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public ESContentIndexClient(ElasticsearchOperations elasticSearchOps,
                                Client client) {
        this.elasticSearchOps = elasticSearchOps;
        this.client = client;
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
    public List<ContentIndexItem> getSpaceContents(String account,
                                                   String storeId,
                                                   String space) {
        SearchQuery searchQuery = getSortedQueryForSpace(account, storeId,
                                                         space);
        List<ContentIndexItem> items = elasticSearchOps
            .queryForList(searchQuery, ContentIndexItem.class);
        return items;
    }

    @Override
    public List<ContentIndexItem> getSpaceContentIds(String account,
                                                     String storeId,
                                                     String space) {
        SearchQuery searchQuery = getSortedQueryForSpace(account, storeId,
                                                         space);
        searchQuery.addFields("contentId");
        List<ContentIndexItem> items = elasticSearchOps
            .queryForList(searchQuery, ContentIndexItem.class);
        return items;
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
        if (!items.isEmpty()) {
            List<IndexQuery> queries = new ArrayList();
            for (ContentIndexItem item : items) {
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
