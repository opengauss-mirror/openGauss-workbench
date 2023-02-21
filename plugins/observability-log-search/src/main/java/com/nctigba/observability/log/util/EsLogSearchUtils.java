package com.nctigba.observability.log.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.nctigba.observability.log.model.query.EsSearchQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;


/**
 * <p>
 * ES Util
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/11/17 09:15
 */
@Slf4j
@Component
public class EsLogSearchUtils {

    @Autowired
    private ElasticsearchClient client;

    /**
     * ES queryLogInfo
     *
     * @param queryParam query log info by queryParam
     * @return logInfo SearchResponse
     */
    public SearchResponse<HashMap> queryLogInfo(EsSearchQuery queryParam) {
        SearchResponse<HashMap> response;
        try {
            response = client.search(s -> {
                s.index(this.getIndexName(queryParam));
                s.size(queryParam.getRowCount());
                s.query(this.query(queryParam));
                s.sort(this.sort(queryParam))
                        .scroll(t -> t.time("10s"));
                return s;
            }, HashMap.class);
            return response;
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }
    }

    /**
     * ES deleteLogInfo
     *
     */
    public void deleteLogInfo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        Date endDate = new Date();
        endDate.setTime(endDate.getTime() - 1000);
        try {
            client.deleteByQuery(s -> {
                s.index("ob-*");
                s.query(f -> f.range(r ->
                        r.field("@timestamp")
                                .lt(JsonData.of(sdf.format(endDate)))));
                s.conflicts(Conflicts.Proceed);
                return s;
            });
            client.indices().clearCache();
            client.indices().forcemerge();
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
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
            return client.count(c -> {
                c.index(this.getIndexName(queryParam));
                c.query(this.query(queryParam));
                return c;
            }).count();
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
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
            GetIndexResponse getIndexResponse = client.indices().get(builder -> builder.index(indexName));
            indexs = getIndexResponse.result().keySet();
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }
        return indexs;
    }

    /**
     * ES queryLogLevel
     *
     * @param queryParam query log level by queryParam
     * @return logLevel SearchResponse
     */
    public SearchResponse<Map> queryLogLevel(EsSearchQuery queryParam) {
        try {
            return client.search(s -> {
                s.index(this.getIndexName(queryParam));
                s.size(5000);
                s.query(this.query(queryParam));
                s.aggregations("LogLevelMap", a -> a
                        .terms(b -> b
                                .field("log_level.keyword")));
                return s;
            }, Map.class);
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
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
        if (!isPhrase && !queryParam.hasDateFilter() && !isLogLevel) {
            return q -> q.matchAll(f -> f);
        }
        if (isLogLevel) {
            for (String logLevel : queryParam.getLogLevel()) {
                fieldValues.add(FieldValue.of(logLevel));
            }
        }
        if (isLogLevel && queryParam.hasDateFilter() && !isPhrase) {
            if(queryParam.getStartDate()!=null && queryParam.getEndDate()==null){
                return q -> {
                    q.bool(b -> b.filter(f -> f
                                    .range(r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate())))))
                            .must(r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues))))
                    );
                    return q;
                };
            }else if(queryParam.getEndDate()!=null && queryParam.getStartDate()==null){
                return q -> {
                    q.bool(b -> b.filter(f -> f
                                    .range(r -> r.field("@timestamp").lt(JsonData.of(sdf.format(queryParam.getEndDate())))))
                            .must(r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues))))
                    );
                    return q;
                };
            }else{
                return q -> {
                    q.bool(b -> b.filter(f -> f
                                    .range(r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))).lt(JsonData.of(sdf.format(queryParam.getEndDate())))))
                            .must(r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues))))
                    );
                    return q;
                };
            }
        } else if (isLogLevel && queryParam.hasDateFilter() && isPhrase) {
            if(queryParam.getStartDate()!=null && queryParam.getEndDate()==null){
                return q -> {
                    q.bool(b -> b.filter(f -> f
                                    .range(r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate())))))
                            .must(r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues))))
                            .must(r -> r.matchPhrase(f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase())))
                    );
                    return q;
                };
            }else if(queryParam.getEndDate()!=null && queryParam.getStartDate()==null){
                return q -> {
                    q.bool(b -> b.filter(f -> f
                                    .range(r -> r.field("@timestamp").lt(JsonData.of(sdf.format(queryParam.getEndDate())))))
                            .must(r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues))))
                            .must(r -> r.matchPhrase(f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase())))
                    );
                    return q;
                };
            }else{
                return q -> {
                    q.bool(b -> b.filter(f -> f
                                    .range(r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))).lt(JsonData.of(sdf.format(queryParam.getEndDate())))))
                            .must(r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues))))
                            .must(r -> r.matchPhrase(f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase())))
                    );
                    return q;
                };
            }
        } else if (!isLogLevel && queryParam.hasDateFilter() && isPhrase) {
            if(queryParam.getStartDate()!=null && queryParam.getEndDate()==null){
                return q -> {
                    q.bool(b -> b.filter(f -> f
                                    .range(r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate())))))
                            .must(r -> r.matchPhrase(f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase())))
                    );
                    return q;
                };
            }else if(queryParam.getEndDate()!=null && queryParam.getStartDate()==null){
                return q -> {
                    q.bool(b -> b.filter(f -> f
                                    .range(r -> r.field("@timestamp").lt(JsonData.of(sdf.format(queryParam.getEndDate())))))
                            .must(r -> r.matchPhrase(f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase())))
                    );
                    return q;
                };
            }else{
                return q -> {
                    q.bool(b -> b.filter(f -> f
                                    .range(r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))).lt(JsonData.of(sdf.format(queryParam.getEndDate())))))
                            .must(r -> r.matchPhrase(f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase())))
                    );
                    return q;
                };
            }
        } else if (isLogLevel && !queryParam.hasDateFilter() && isPhrase) {
            return q -> {
                q.bool(b -> b.must(r -> r.terms(f -> f.field("log_level.keyword").terms(g -> g.value(fieldValues))))
                        .must(r -> r.matchPhrase(f -> f.field("message").query(queryParam.getSearchPhrase().toLowerCase())))
                );
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
            if(queryParam.getStartDate()!=null && queryParam.getEndDate()==null){
                return q -> {
                    q.bool(b -> b.filter(f -> f
                            .range(r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate())))))
                    );
                    return q;
                };
            }else if(queryParam.getEndDate()!=null && queryParam.getStartDate()==null){
                return q -> {
                    q.bool(b -> b.filter(f -> f
                            .range(r -> r.field("@timestamp").lt(JsonData.of(sdf.format(queryParam.getEndDate())))))
                    );
                    return q;
                };
            }else{
                return q -> {
                    q.bool(b -> b.filter(f -> f
                            .range(r -> r.field("@timestamp").gte(JsonData.of(sdf.format(queryParam.getStartDate()))).lt(JsonData.of(sdf.format(queryParam.getEndDate())))))
                    );
                    return q;
                };
            }
        }

    }

    /**
     * ES sort
     *
     * @param queryParam query order by queryParam
     */
    public Function<SortOptions.Builder, ObjectBuilder<SortOptions>> sort(EsSearchQuery queryParam) {
        return queryParam.getOrder()!=null?sort -> sort.field(f -> f.field(queryParam.getSort()).order(SortOrder.Asc)):sort -> sort.field(f -> f.field(queryParam.getSort()).order(SortOrder.Desc));
    }
}
