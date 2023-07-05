/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.util;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.config.ElasticsearchProvider;
import com.nctigba.observability.sql.model.history.query.EsSearchQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    public Optional<SearchResponse<HashMap>> queryLogInfo(EsSearchQuery queryParam) {
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
                return s;
            }, HashMap.class);
            return Optional.of(response);
        } catch (IOException | HisDiagnosisException e) {
            throw new HisDiagnosisException("error" + e.getMessage());
        }
    }

    /**
     * ES indexList
     *
     * @param indexName query ES indexes by Fuzzy matching
     * @return indexes set
     */
    public Optional<Set<String>> indexList(String indexName) {
        Set<String> indexs;
        try {
            var client = clientProvider.client();
            GetIndexResponse getIndexResponse = client.indices().get(builder -> builder.index(indexName));
            indexs = getIndexResponse.result().keySet();
        } catch (IOException | HisDiagnosisException e) {
            log.info(e.getMessage());
            return Optional.empty();
        }
        return Optional.of(indexs);
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
     * ES query
     *
     * @param queryParam build query condition by queryParam
     * @return query Function
     */
    public Function<Query.Builder, ObjectBuilder<Query>> query(EsSearchQuery queryParam) {
        boolean isPhrase = StringUtils.isNotBlank(queryParam.getSearchPhrase());
        boolean isLogLevel = queryParam.getLogLevel() != null && queryParam.getLogLevel().size() > 0;
        List<FieldValue> fieldValues = new ArrayList<>();
        if (!isPhrase && !queryParam.hasDateFilter() && !isLogLevel) {
            return q -> q.matchAll(f -> f);
        }
        if (isLogLevel) {
            for (String logLevel : queryParam.getLogLevel()) {
                fieldValues.add(FieldValue.of(logLevel));
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        if (isLogLevel && queryParam.hasDateFilter() && !isPhrase) {
            if (queryParam.getStartDate() != null && queryParam.getEndDate() == null) {
                return q -> {
                    q.bool(b -> b.filter(f -> f.range(
                            r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))))).must(
                            r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues)))));
                    return q;
                };
            } else if (queryParam.getEndDate() != null && queryParam.getStartDate() == null) {
                return q -> {
                    q.bool(b -> b.filter(f -> f.range(
                            r -> r.field("@timestamp").lt(JsonData.of(sdf.format(queryParam.getEndDate()))))).must(
                            r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues)))));
                    return q;
                };
            } else {
                return q -> {
                    q.bool(b -> b.filter(f -> f.range(
                            r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))).lt(
                                    JsonData.of(sdf.format(queryParam.getEndDate()))))).must(
                            r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues)))));
                    return q;
                };
            }
        } else if (isLogLevel && queryParam.hasDateFilter() && isPhrase) {
            if (queryParam.getStartDate() != null && queryParam.getEndDate() == null) {
                return q -> {
                    q.bool(b -> b.filter(f -> f.range(
                            r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))))).must(
                            r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues)))).must(
                            r -> r.matchPhrase(
                                    f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase()))));
                    return q;
                };
            } else if (queryParam.getEndDate() != null && queryParam.getStartDate() == null) {
                return q -> {
                    q.bool(b -> b.filter(f -> f.range(
                            r -> r.field("@timestamp").lt(JsonData.of(sdf.format(queryParam.getEndDate()))))).must(
                            r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues)))).must(
                            r -> r.matchPhrase(
                                    f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase()))));
                    return q;
                };
            } else {
                return q -> {
                    q.bool(b -> b.filter(f -> f.range(
                            r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))).lt(
                                    JsonData.of(sdf.format(queryParam.getEndDate()))))).must(
                            r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues)))).must(
                            r -> r.matchPhrase(
                                    f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase()))));
                    return q;
                };
            }
        } else if (!isLogLevel && queryParam.hasDateFilter() && isPhrase) {
            if (queryParam.getStartDate() != null && queryParam.getEndDate() == null) {
                return q -> {
                    q.bool(b -> b.filter(f -> f.range(
                            r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))))).must(
                            r -> r.matchPhrase(
                                    f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase()))));
                    return q;
                };
            } else if (queryParam.getEndDate() != null && queryParam.getStartDate() == null) {
                return q -> {
                    q.bool(b -> b.filter(f -> f.range(
                            r -> r.field("@timestamp").lt(JsonData.of(sdf.format(queryParam.getEndDate()))))).must(
                            r -> r.matchPhrase(
                                    f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase()))));
                    return q;
                };
            } else {
                return q -> {
                    q.bool(b -> b.filter(f -> f.range(
                            r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))).lt(
                                    JsonData.of(sdf.format(queryParam.getEndDate()))))).must(r -> r.matchPhrase(
                            f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase()))));
                    return q;
                };
            }
        } else if (isLogLevel && !queryParam.hasDateFilter() && isPhrase) {
            return q -> {
                q.bool(b -> b.must(
                        r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues)))).must(
                        r -> r.matchPhrase(f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase()))));
                return q;
            };
        } else if (isLogLevel && !queryParam.hasDateFilter() && !isPhrase) {
            return q -> {
                q.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues)));
                return q;
            };
        } else if (!isLogLevel && !queryParam.hasDateFilter() && isPhrase) {
            return q -> {
                q.matchPhrase(f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase()));
                return q;
            };
        } else {
            if (queryParam.getStartDate() != null && queryParam.getEndDate() == null) {
                return q -> {
                    q.bool(b -> b.filter(f -> f.range(
                            r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))))));
                    return q;
                };
            } else if (queryParam.getEndDate() != null && queryParam.getStartDate() == null) {
                return q -> {
                    q.bool(b -> b.filter(f -> f.range(
                            r -> r.field("@timestamp").lt(JsonData.of(sdf.format(queryParam.getEndDate()))))));
                    return q;
                };
            } else {
                return q -> {
                    q.bool(b -> b.filter(f -> f.range(
                            r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))).lt(
                                    JsonData.of(sdf.format(queryParam.getEndDate()))))));
                    return q;
                };
            }
        }
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
}
