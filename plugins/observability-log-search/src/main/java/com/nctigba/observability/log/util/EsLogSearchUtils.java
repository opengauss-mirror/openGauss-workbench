/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  EsLogSearchUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/util/EsLogSearchUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.util;

import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.nctigba.observability.log.config.ElasticsearchProvider;
import com.nctigba.observability.log.model.query.EsSearchQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;

/**
 * <p>
 * ES Util
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/11/17 09:15
 */
@Component
@Slf4j
public class EsLogSearchUtils {

    @Autowired
    private ElasticsearchProvider clientProvider;

    /**
     * ES queryLogInfo
     *
     * @param queryParam query log info by queryParam
     * @return logInfo SearchResponse
     */
    @SuppressWarnings("rawtypes")
    public SearchResponse<HashMap> queryLogInfo(EsSearchQuery queryParam) {
        SearchResponse<HashMap> response;
        try {
            var client = clientProvider.client();
            response = client.search(s -> {
                s.index(this.getIndexName(queryParam));
                s.size(queryParam.getRowCount());
                s.query(this.query(queryParam));
                s.sort(this.sort(queryParam));
                s.sort(this.indexsSort(queryParam));
                s.sort(this.logSort(queryParam));
                if (queryParam.getSorts() != null && !queryParam.getSorts().isEmpty()) {
                    s.searchAfter(queryParam.getSorts());
                }
                s.highlight(this.highlight());
                return s;
            }, HashMap.class);
            return response;
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    /**
     * ES deleteLogInfo
     */
    public void deleteLogInfo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        Date endDate = new Date();
        endDate.setTime(endDate.getTime() - 15 * 24 * 60 * 60 * 1000);
        try {
            var client = clientProvider.client();
            client.deleteByQuery(s -> {
                s.index("ob-*");
                s.query(f -> f.range(r -> r.field("@timestamp").lt(JsonData.of(sdf.format(endDate)))));
                s.conflicts(Conflicts.Proceed);
                return s;
            });
            client.indices().clearCache();
            client.indices().forcemerge();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * ES queryLogCount
     *
     * @param queryParam query log count by queryParam
     * @return logCount long
     */
    public long queryLogCount(EsSearchQuery queryParam) {
        try {
            var client = clientProvider.client();
            return client.count(c -> {
                c.index(this.getIndexName(queryParam));
                c.query(this.query(queryParam));
                return c;
            }).count();
        } catch (Exception e) {
            log.info(e.getMessage());
            return 0;
        }
    }

    /**
     * ES queryLogCount
     *
     * @param queryParam query log count by queryParam
     * @return logCount long
     */
    @SuppressWarnings("rawtypes")
    public SearchResponse<HashMap> queryLogCounts(EsSearchQuery queryParam) {
        SearchResponse<HashMap> response;
        try {
            var client = clientProvider.client();
            response = client.search(s -> {
                s.index(this.getIndexName(queryParam));
                s.size(0);
                s.query(this.query(queryParam));
                s.aggregations(
                        "agg1", agg -> agg.histogram(histogram -> histogram.field("@timestamp").interval(
                                (double) queryParam.getInterval())).aggregations(
                                "agg2", c -> c.terms(t -> t.field("_index"))));
                return s;
            }, HashMap.class);
            return response;
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    /**
     * ES indexList
     *
     * @param indexName query ES indexes by Fuzzy matching
     * @return indexes set
     */
    public Set<String> indexList(String indexName) {
        Set<String> indexs;
        try {
            var client = clientProvider.client();
            GetIndexResponse getIndexResponse = client.indices().get(builder -> builder.index(indexName));
            indexs = getIndexResponse.result().keySet();
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
        return indexs;
    }

    /**
     * ES queryLogLevel
     *
     * @param queryParam query log level by queryParam
     * @return logLevel SearchResponse
     */
    @SuppressWarnings("rawtypes")
    public SearchResponse<Map> queryLogLevel(EsSearchQuery queryParam) {
        try {
            var client = clientProvider.client();
            return client.search(s -> {
                s.index(this.getIndexName(queryParam));
                s.size(5000);
                s.query(this.query(queryParam));
                s.aggregations("LogLevelMap", a -> a.terms(b -> b.field("log_level.keyword")));
                return s;
            }, Map.class);
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    /**
     * ES getIndexName
     *
     * @param queryParam query index name by queryParam
     * @return indexName String
     */
    public String getIndexName(EsSearchQuery queryParam) {
        List<String> list = new ArrayList<>();
        String indexNames;
        if (queryParam.isEmptyObject()) {
            indexNames = "ob-*";
            list.add(indexNames);
        } else {
            List<String> nodeIdList = queryParam.getNodeId();
            List<String> logTypeList = queryParam.getLogType();
            if (logTypeList != null && logTypeList.size() > 0) {
                for (String logType : logTypeList) {
                    if (nodeIdList != null && nodeIdList.size() > 0) {
                        for (String nodeId : nodeIdList) {
                            indexNames = "ob-" + logType + "-" + nodeId;
                            list.add(indexNames);
                        }
                    } else {
                        indexNames = "ob-" + logType + "-*";
                        list.add(indexNames);
                    }

                }
            } else {
                if (nodeIdList != null && nodeIdList.size() > 0) {
                    for (String nodeId : nodeIdList) {
                        indexNames = "ob-*-" + nodeId;
                        list.add(indexNames);
                    }
                } else {
                    indexNames = "ob-*";
                    list.add(indexNames);
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String index : list) {
            sb.append(index);
            sb.append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * ES query by id
     *
     * @param queryParam build query condition by queryParam
     * @return query Function
     */
    public Function<Query.Builder, ObjectBuilder<Query>> queryById(EsSearchQuery queryParam) {
        if (queryParam.getId() != null) {
            return q -> q.term(f -> f.field("_id").value(queryParam.getId()));
        }
        return q -> q.matchAll(f -> f);
    }

    /**
     * ES query
     *
     * @param queryParam build query condition by queryParam
     * @return query Function
     */
    public Function<Query.Builder, ObjectBuilder<Query>> query(EsSearchQuery queryParam) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        boolean isPhrase = StringUtils.isNotBlank(queryParam.getSearchPhrase());
        boolean isLogLevel = queryParam.getLogLevel() != null && queryParam.getLogLevel().size() > 0;
        List<FieldValue> fieldValues = new ArrayList<>();
        if (isLogLevel) {
            for (String logLevel : queryParam.getLogLevel()) {
                fieldValues.add(FieldValue.of(logLevel));
            }
        }
        return q -> {
            q.bool(b -> {
                if (queryParam.hasDateFilter()) {
                    b.filter(f -> {
                        if (queryParam.getStartDate() != null) {
                            return f.range(
                                    r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))));
                        }
                        if (queryParam.getEndDate() != null) {
                            return f.range(
                                    r -> r.field("@timestamp").lt(JsonData.of(sdf.format(queryParam.getEndDate()))));
                        }
                        return f;
                    });
                }
                if (isLogLevel) {
                    b.must(r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues))));
                }
                if (isPhrase) {
                    b.must(r -> r.queryString(
                            f -> f.query(queryParam.getSearchPhrase())));
                }
                return b;
            });
            return q;
        };
    }

    /**
     * ES sort
     *
     * @param queryParam query order by queryParam
     * @return query Function
     */
    public Function<SortOptions.Builder, ObjectBuilder<SortOptions>> sort(EsSearchQuery queryParam) {
        return queryParam.getOrder() != null ? sort -> sort.field(
                f -> f.field(queryParam.getSort()).order(SortOrder.Asc)) : sort -> sort.field(
                f -> f.field(queryParam.getSort()).order(SortOrder.Desc));
    }

    /**
     * ES sort
     *
     * @param queryParam query order by queryParam
     * @return query Function
     */
    public Function<SortOptions.Builder, ObjectBuilder<SortOptions>> indexsSort(EsSearchQuery queryParam) {
        return queryParam.getOrder() != null ? sort -> sort.field(
                f -> f.field("_index").order(SortOrder.Asc)) : sort -> sort.field(
                f -> f.field("_index").order(SortOrder.Desc));
    }

    /**
     * ES sort
     *
     * @param queryParam query order by queryParam
     * @return query Function
     */
    public Function<SortOptions.Builder, ObjectBuilder<SortOptions>> logSort(EsSearchQuery queryParam) {
        return queryParam.getOrder() != null ? sort -> sort.field(
                f -> f.field("log.offset").order(SortOrder.Asc)) : sort -> sort.field(
                f -> f.field("log.offset").order(SortOrder.Desc));
    }

    /**
     *
     * @return highlight Function
     */
    public Function<Highlight.Builder, ObjectBuilder<Highlight>> highlight() {
        return f -> f.fields(
                "message", m -> m.preTags("<span style=\"background-color: yellow\">").postTags("</span>"));
    }
}
