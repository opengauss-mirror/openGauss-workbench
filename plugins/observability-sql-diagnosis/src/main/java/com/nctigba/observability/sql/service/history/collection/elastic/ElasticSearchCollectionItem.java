/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.elastic;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.ElasticSearchCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.LogDetailInfoDTO;
import com.nctigba.observability.sql.model.history.dto.LogInfoDTO;
import com.nctigba.observability.sql.model.history.query.EsSearchQuery;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.util.EsLogSearchUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

/**
 * ElasticSearchCollectionItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
public abstract class ElasticSearchCollectionItem implements CollectionItem<LogInfoDTO> {
    @Autowired
    private EsLogSearchUtils utils;

    @Override
    public LogInfoDTO collectData(HisDiagnosisTask task) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat stringFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        List<String> nodes = new ArrayList<>();
        nodes.add(task.getNodeId());
        EsSearchQuery query = new EsSearchQuery();
        try {
            query.setStartDate(stringFormat.parse(dateFormat.format(task.getHisDataStartTime())));
            query.setEndDate(stringFormat.parse(dateFormat.format(task.getHisDataEndTime())));
        } catch (ParseException e) {
            throw new HisDiagnosisException("error:", e);
        }
        query.setNodeId(nodes);
        query.setSearchPhrase(getQueryParam());
        return getLogByQuery(query).orElse(null);
    }

    @Override
    public LogInfoDTO queryData(HisDiagnosisTask task) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat stringFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        List<String> nodes = new ArrayList<>();
        nodes.add(task.getNodeId());
        EsSearchQuery query = new EsSearchQuery();
        query.setNodeId(nodes);
        try {
            query.setStartDate(stringFormat.parse(dateFormat.format(task.getHisDataStartTime())));
            query.setEndDate(stringFormat.parse(dateFormat.format(task.getHisDataEndTime())));
        } catch (ParseException e) {
            throw new HisDiagnosisException("error", e);
        }
        query.setSearchPhrase(getQueryParam());
        return getLogByQuery(query).orElse(null);
    }

    abstract String getQueryParam();

    private Optional<LogInfoDTO> getLogByQuery(EsSearchQuery queryParam) {
        List<String> logType = new ArrayList<>();
        logType.add("opengauss-error-log");
        queryParam.setLogType(logType);
        int nodeIdCount = 0;
        if (queryParam.getNodeId() != null) {
            nodeIdCount = queryParam.getNodeId().size();
            List<String> list = new ArrayList<>();
            List<String> nodes = this.getNodeId().orElse(null);
            for (String nodeId : queryParam.getNodeId()) {
                if (nodes != null && !nodes.contains(nodeId)) {
                    list.add(nodeId);
                }
            }
            if (list.size() > 0) {
                queryParam.getNodeId().removeAll(list);
            }
        }
        if (nodeIdCount > 0 && queryParam.getNodeId().size() == 0) {
            return Optional.empty();
        } else {
            var list = new ArrayList<LogDetailInfoDTO>();
            SearchResponse<HashMap> searchResponse = utils.queryLogInfo(queryParam).orElse(null);
            if (searchResponse == null) {
                return Optional.empty();
            }
            List<Hit<HashMap>> hits = searchResponse.hits().hits();
            for (var decodeBeanHit : hits) {
                String ids = decodeBeanHit.id();
                var docMap = decodeBeanHit.source();
                if (docMap != null) {
                    LogDetailInfoDTO logDetailInfoDTO = new LogDetailInfoDTO();
                    logDetailInfoDTO.setLogTime(docMap.get(ElasticSearchCommon.TIMESTAMP));
                    Map logTypeMap = null;
                    if (docMap.get(ElasticSearchCommon.FIELDS) instanceof Map) {
                        logTypeMap = (Map) docMap.get(ElasticSearchCommon.FIELDS);
                    }
                    if (logTypeMap != null) {
                        logDetailInfoDTO.setLogType(logTypeMap.get(ElasticSearchCommon.LOG_TYPE));
                    }
                    logDetailInfoDTO.setLogLevel(docMap.get(ElasticSearchCommon.LOG_LEVEL));
                    logDetailInfoDTO.setLogData(docMap.get(ElasticSearchCommon.MESSAGE));
                    logDetailInfoDTO.setLogClusterId(docMap.get(ElasticSearchCommon.CLUSTER_ID));
                    logDetailInfoDTO.setLogNodeId(docMap.get(ElasticSearchCommon.NODE_ID));
                    logDetailInfoDTO.setId(ids);
                    list.add(logDetailInfoDTO);
                }
            }
            List<String> sorts = new ArrayList<>();
            if (!hits.isEmpty()) {
                sorts.addAll(hits.get(hits.size() - 1).sort());
            } else {
                sorts.addAll(queryParam.getSorts());
            }
            LogInfoDTO logInfoDTO = new LogInfoDTO();
            logInfoDTO.setSorts(sorts);
            logInfoDTO.setLogs(list);
            return Optional.of(logInfoDTO);
        }
    }

    private static List<String> nodeList = null;
    private static long id;

    private Optional<List<String>> getNodeId() {
        if (nodeList != null && System.currentTimeMillis() - id < 60000) {
            return Optional.of(nodeList);
        }
        synchronized (this) {
            if (nodeList != null && System.currentTimeMillis() - id < 60000) {
                return Optional.of(nodeList);
            }
            Set<String> indexs = utils.indexList("ob-opengauss-error-log-*").orElse(null);
            if (indexs == null || indexs.size() < 1) {
                return Optional.empty();
            }
            List<String> nodeIdList = new ArrayList<>();
            for (String index : indexs) {
                String prefix = index.substring(index.indexOf(ElasticSearchCommon.HYPHEN) + 1);
                String dataType = prefix.substring(prefix.indexOf(ElasticSearchCommon.HYPHEN) + 1);
                String logType = dataType.substring(dataType.indexOf(ElasticSearchCommon.HYPHEN) + 1);
                String nodeId = logType.substring(logType.indexOf(ElasticSearchCommon.HYPHEN) + 1);
                nodeIdList.add(nodeId);
            }
            id = System.currentTimeMillis();
            nodeList = nodeIdList;
            return Optional.of(nodeIdList);
        }
    }
}
