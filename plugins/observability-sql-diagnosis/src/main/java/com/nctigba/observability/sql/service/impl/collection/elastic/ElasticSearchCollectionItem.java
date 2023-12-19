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
 *  ElasticSearchCollectionItem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/collection/elastic/ElasticSearchCollectionItem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.collection.elastic;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.ElasticSearchConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.EsSearchVO;
import com.nctigba.observability.sql.model.vo.point.LogDetailInfoVO;
import com.nctigba.observability.sql.model.vo.point.LogInfoVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.util.EsLogSearchUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
public abstract class ElasticSearchCollectionItem implements CollectionItem<Object> {
    @Autowired
    private EsLogSearchUtils utils;

    @Override
    public Object collectData(DiagnosisTaskDO task) {
        return query(task);
    }

    @Override
    public Object queryData(DiagnosisTaskDO task) {
        return query(task);
    }

    /**
     * Get query param
     *
     * @return String
     */
    public abstract String getQueryParam();

    private Object query(DiagnosisTaskDO task) {
        if (task.getHisDataEndTime() == null) {
            Date endDate = new Date();
            task.setHisDataEndTime(endDate);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat stringFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        List<String> nodes = new ArrayList<>();
        nodes.add(task.getNodeId());
        EsSearchVO query = new EsSearchVO();
        query.setNodeId(nodes);
        query.setSearchPhrase(getQueryParam());
        LogInfoVO logInfoVO;
        try {
            query.setStartDate(stringFormat.parse(dateFormat.format(task.getHisDataStartTime())));
            query.setEndDate(stringFormat.parse(dateFormat.format(task.getHisDataEndTime())));
            logInfoVO = getLogByQuery(query).orElse(null);
        } catch (ParseException | HisDiagnosisException e) {
            return "error" + e.getMessage();
        }
        return logInfoVO;
    }

    private Optional<LogInfoVO> getLogByQuery(EsSearchVO queryParam) {
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
            var list = new ArrayList<LogDetailInfoVO>();
            SearchResponse<HashMap> searchResponse;
            try {
                searchResponse = utils.queryLogInfo(queryParam).orElse(null);
            } catch (HisDiagnosisException e) {
                throw new HisDiagnosisException("error" + e.getMessage());
            }
            if (searchResponse == null) {
                return Optional.empty();
            }
            List<Hit<HashMap>> hits = searchResponse.hits().hits();
            for (var decodeBeanHit : hits) {
                String ids = decodeBeanHit.id();
                var docMap = decodeBeanHit.source();
                if (docMap != null) {
                    LogDetailInfoVO logDetailInfoVO = new LogDetailInfoVO();
                    logDetailInfoVO.setLogTime(docMap.get(ElasticSearchConstants.TIMESTAMP));
                    Map logTypeMap = null;
                    if (docMap.get(ElasticSearchConstants.FIELDS) instanceof Map) {
                        logTypeMap = (Map) docMap.get(ElasticSearchConstants.FIELDS);
                    }
                    if (logTypeMap != null) {
                        logDetailInfoVO.setLogType(logTypeMap.get(ElasticSearchConstants.LOG_TYPE));
                    }
                    logDetailInfoVO.setLogLevel(docMap.get(ElasticSearchConstants.LOG_LEVEL));
                    logDetailInfoVO.setLogData(docMap.get(ElasticSearchConstants.MESSAGE));
                    logDetailInfoVO.setLogClusterId(docMap.get(ElasticSearchConstants.CLUSTER_ID));
                    logDetailInfoVO.setLogNodeId(docMap.get(ElasticSearchConstants.NODE_ID));
                    logDetailInfoVO.setId(ids);
                    list.add(logDetailInfoVO);
                }
            }
            List<String> sorts = new ArrayList<>();
            if (!hits.isEmpty()) {
                sorts.addAll(hits.get(hits.size() - 1).sort());
            }
            LogInfoVO logInfoVO = new LogInfoVO();
            logInfoVO.setSorts(sorts);
            logInfoVO.setLogs(list);
            return Optional.of(logInfoVO);
        }
    }

    private static List<String> nodeList = null;
    private static long id;

    private Optional<List<String>> getNodeId() {
        int cacheTime = 60000;
        if (nodeList != null && System.currentTimeMillis() - id < cacheTime) {
            return Optional.of(nodeList);
        }
        synchronized (this) {
            if (nodeList != null && System.currentTimeMillis() - id < cacheTime) {
                return Optional.of(nodeList);
            }
            Set<String> indexs = utils.indexList("ob-opengauss-error-log-*").orElse(null);
            if (indexs == null || indexs.size() < 1) {
                return Optional.empty();
            }
            List<String> nodeIdList = new ArrayList<>();
            for (String index : indexs) {
                String prefix = index.substring(index.indexOf(ElasticSearchConstants.HYPHEN) + 1);
                String dataType = prefix.substring(prefix.indexOf(ElasticSearchConstants.HYPHEN) + 1);
                String logType = dataType.substring(dataType.indexOf(ElasticSearchConstants.HYPHEN) + 1);
                String nodeId = logType.substring(logType.indexOf(ElasticSearchConstants.HYPHEN) + 1);
                nodeIdList.add(nodeId);
            }
            id = System.currentTimeMillis();
            nodeList = nodeIdList;
            return Optional.of(nodeIdList);
        }
    }
}
