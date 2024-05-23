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
 *  LogSearchServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/service/impl/LogSearchServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.service.impl;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.nctigba.observability.log.constants.CommonConstants;
import com.nctigba.observability.log.model.dto.ContextSearchDTO;
import com.nctigba.observability.log.model.dto.ContextSearchInfoDTO;
import com.nctigba.observability.log.model.dto.LogDetailInfoDTO;
import com.nctigba.observability.log.model.dto.LogDistroMapDTO;
import com.nctigba.observability.log.model.dto.LogDistroMapInfoDTO;
import com.nctigba.observability.log.model.dto.LogInfoDTO;
import com.nctigba.observability.log.model.dto.LogTypeTreeDTO;
import com.nctigba.observability.log.model.query.ContextSearchQuery;
import com.nctigba.observability.log.model.query.EsSearchQuery;
import com.nctigba.observability.log.service.LogSearchService;
import com.nctigba.observability.log.util.EsLogSearchUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;


/**
 * <p>
 * Log-Search Service Impl
 * </p>
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/11/17 09:10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogSearchServiceImpl implements LogSearchService {
    private static final long SECOND = 1000L;
    private static final long MINUTE = SECOND * 60L;
    private static final long HALF_HOUR = MINUTE * 30L;
    private static final int DEFAULT_INTERVAL_NUM = 6;
    @Autowired
    private EsLogSearchUtils esLogSearchUtils;

    @SuppressWarnings("rawtypes")
    @Override
    public List<LogDistroMapDTO> getLogDistroMap(EsSearchQuery queryParam) {
        try {
            int nodeIdCount = 0;
            if (queryParam.getNodeId() != null) {
                nodeIdCount = queryParam.getNodeId().size();
                List<String> list = new ArrayList<>();
                List<String> nodes = this.getNodeId();
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
                return null;
            } else {
                if (queryParam.getEndDate() == null && queryParam.getStartDate() == null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    SimpleDateFormat stringFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                    Date endDate = new Date();
                    long time = endDate.getTime();
                    Date startDate = new Date();
                    startDate.setTime(time - HALF_HOUR);
                    queryParam.setStartDate(stringFormat.parse(dateFormat.format(startDate)));
                    queryParam.setEndDate(stringFormat.parse(dateFormat.format(endDate)));

                }
                LocalDateTime endTime = queryParam.getEndDate().toInstant().atZone(
                        ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime startTime = queryParam.getStartDate().toInstant().atZone(
                        ZoneId.systemDefault()).toLocalDateTime();
                long interval = Duration.between(startTime, endTime).toMillis();
                EsSearchQuery esSearchQuery = new EsSearchQuery();
                esSearchQuery.setEndDate(queryParam.getEndDate());
                esSearchQuery.setStartDate(queryParam.getStartDate());
                esSearchQuery.setInterval(Math.max(interval / DEFAULT_INTERVAL_NUM, MINUTE));
                if (!queryParam.isEmptyObject()) {
                    esSearchQuery.setLogType(queryParam.getLogType());
                    esSearchQuery.setLogLevel(queryParam.getLogLevel());
                    esSearchQuery.setNodeId(queryParam.getNodeId());
                    esSearchQuery.setSearchPhrase(queryParam.getSearchPhrase());
                }
                SearchResponse<HashMap> searchResponse = esLogSearchUtils.queryLogCounts(esSearchQuery);
                Map<String, Aggregate> s = searchResponse.aggregations();
                List<HistogramBucket> list = s.get("agg1").histogram().buckets().array();
                List<LogDistroMapDTO> lList = new ArrayList<>();
                List<LogTypeTreeDTO> listType;
                if (queryParam.getLogType() != null) {
                    List<String> slist = new ArrayList<>();
                    for (String logType : queryParam.getLogType()) {
                        slist.add("ob-" + logType + "-");
                    }
                    listType = this.createLogTypeTree(slist);
                } else {
                    listType = this.getLogType();
                }
                for (HistogramBucket histogramBucket : list) {
                    LogDistroMapDTO logDistroMapDTO = new LogDistroMapDTO();
                    if (lList.size() > 0) {
                        logDistroMapDTO.setDateTime(histogramBucket.keyAsString());
                    } else {
                        SimpleDateFormat stringFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        logDistroMapDTO.setDateTime(stringFormat.format(esSearchQuery.getStartDate()));
                    }
                    List<LogDistroMapInfoDTO> logDistroMapInfoDTOList = new ArrayList<>();
                    for (LogTypeTreeDTO logTypeTreeDTO : listType) {
                        long totalCount = 0;
                        LogDistroMapInfoDTO logDistroMapInfoDTO = new LogDistroMapInfoDTO();
                        logDistroMapInfoDTO.setLogType(logTypeTreeDTO.getTypeName());
                        List<LogDistroMapInfoDTO> logDistroMapList = new ArrayList<>();
                        for (int index = 0; index < logTypeTreeDTO.getChildren().size(); index++) {
                            List<StringTermsBucket> stringTermsBucket = histogramBucket.aggregations().get(
                                    "agg2").sterms().buckets().array();
                            for (StringTermsBucket stringTermsBucket1 : stringTermsBucket) {
                                if (stringTermsBucket1.key().contains(
                                        logTypeTreeDTO.getTypeName() + "-" + logTypeTreeDTO.getChildren().get(
                                                index).getTypeName())) {
                                    totalCount += stringTermsBucket1.docCount();
                                } else {
                                    continue;
                                }
                                LogDistroMapInfoDTO logDistMapInfo = new LogDistroMapInfoDTO();
                                logDistMapInfo.setLogType(logTypeTreeDTO.getChildren().get(index).getTypeName());
                                logDistMapInfo.setLogCount(stringTermsBucket1.docCount());
                                boolean isExist = logDistroMapList.stream().anyMatch(
                                        dto -> dto.getLogType().equals(logDistMapInfo.getLogType()));
                                if (isExist) {
                                    logDistroMapList.forEach(f -> {
                                        if (f.getLogType().equals(logDistMapInfo.getLogType())) {
                                            f.setLogCount(f.getLogCount() + logDistMapInfo.getLogCount());
                                        }
                                    });
                                } else {
                                    logDistroMapList.add(logDistMapInfo);
                                }
                            }
                        }
                        logDistroMapInfoDTO.setLogCount(totalCount);
                        logDistroMapInfoDTO.setChildren(logDistroMapList);
                        logDistroMapInfoDTOList.add(logDistroMapInfoDTO);
                        logDistroMapDTO.setLogCounts(logDistroMapInfoDTOList);
                        if (CollectionUtils.isEmpty(lList) || !lList.contains(logDistroMapDTO)) {
                            lList.add(logDistroMapDTO);
                        }
                    }
                }
                return lList;
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public LogInfoDTO getLogByQuery(EsSearchQuery queryParam) {
        int nodeIdCount = 0;
        if (queryParam.getNodeId() != null) {
            nodeIdCount = queryParam.getNodeId().size();
            List<String> list = new ArrayList<>();
            List<String> nodes = this.getNodeId();
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
            return null;
        } else {
            var list = new ArrayList<LogDetailInfoDTO>();
            LogInfoDTO logInfoDTO = new LogInfoDTO();
            try {
                SearchResponse<HashMap> searchResponse = esLogSearchUtils.queryLogInfo(queryParam);
                if (searchResponse == null) {
                    return null;
                }
                List<Hit<HashMap>> hits = searchResponse.hits().hits();
                List<String> sorts = new ArrayList<>();
                for (var decodeBeanHit : hits) {
                    String id = decodeBeanHit.id();
                    var docMap = decodeBeanHit.source();
                    LogDetailInfoDTO logDetailInfoDTO = new LogDetailInfoDTO();
                    if (docMap != null) {
                        logDetailInfoDTO.setLogTime(docMap.get(CommonConstants.TIMESTAMP));
                        Map logTypeMap = null;
                        if (docMap.get(CommonConstants.FIELDS) instanceof Map) {
                            logTypeMap = (Map) docMap.get(CommonConstants.FIELDS);
                        }
                        if (logTypeMap != null) {
                            logDetailInfoDTO.setLogType(logTypeMap.get(CommonConstants.LOG_TYPE));
                        }
                        logDetailInfoDTO.setLogLevel(docMap.get(CommonConstants.LOG_LEVEL));
                        logDetailInfoDTO.setLogClusterId(docMap.get(CommonConstants.CLUSTER_ID));
                        logDetailInfoDTO.setLogNodeId(docMap.get(CommonConstants.NODE_ID));
                        logDetailInfoDTO.setId(id);
                        Object message = docMap.get(CommonConstants.MESSAGE);
                        Map<String, List<String>> highlightData = decodeBeanHit.highlight();
                        if (!CollectionUtils.isEmpty(highlightData) && !CollectionUtils.isEmpty(
                                highlightData.get(CommonConstants.MESSAGE))) {
                            List<String> data = highlightData.get(CommonConstants.MESSAGE);
                            String messageStr = message.toString();
                            for (String highlightStr : data) {
                                String targetStr = highlightStr.replace(CommonConstants.PRE_TAGS, "")
                                        .replace(CommonConstants.POST_TAGS, "");
                                messageStr = messageStr.replace(targetStr, highlightStr);

                            }
                            logDetailInfoDTO.setLogData(messageStr);
                        } else {
                            logDetailInfoDTO.setLogData(message);
                        }
                        list.add(logDetailInfoDTO);
                    }
                }
                if (!hits.isEmpty()) {
                    sorts.addAll(hits.get(hits.size() - 1).sort());
                } else {
                    sorts.addAll(queryParam.getSorts());
                }
                logInfoDTO.setSorts(sorts);
                logInfoDTO.setLogs(list);
            } catch (Exception e) {
                log.info(e.getMessage());
                return null;
            }
            return logInfoDTO;
        }
    }


    @Override
    public List<LogTypeTreeDTO> getLogType() {
        Set<String> logTypes = esLogSearchUtils.indexList("ob-*");
        if (logTypes != null && logTypes.size() > 0) {
            List<String> list = new ArrayList<>(logTypes);
            return this.createLogTypeTree(list);
        } else {
            return null;
        }
    }

    @Override
    public List<String> getLogLevel() {
        EsSearchQuery queryParam = new EsSearchQuery();
        List<String> logLevelList = new ArrayList<>();
        try {
            var searchResponse = esLogSearchUtils.queryLogLevel(queryParam);
            if (searchResponse == null) {
                return null;
            }
            Map<String, Aggregate> aggregations = searchResponse.aggregations();
            StringTermsAggregate logLevels = aggregations.get("LogLevelMap").sterms();
            List<StringTermsBucket> termsBucketList = logLevels.buckets().array();
            for (StringTermsBucket bucket : termsBucketList) {
                logLevelList.add(bucket.key());
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
        return logLevelList;
    }

    @Override
    public ContextSearchInfoDTO getContextSearch(ContextSearchQuery queryParam) {
        ContextSearchDTO contextSearchDTO = new ContextSearchDTO();
        String id = null;
        String addId = null;
        List<String> sortList = new ArrayList<>();
        HashMap<String, List<String>> sortMap = new HashMap<>();
        try {
            boolean isSortEmpty = queryParam.getSorts() == null || queryParam.getSorts().size() == 0;
            boolean isIdEmpty = queryParam.getId() == null || "".equals(queryParam.getId());
            if (isSortEmpty && !isIdEmpty) {
                HashMap<List<String>, ContextSearchDTO> map = this.getSorts(queryParam);
                if (!CollectionUtils.isEmpty(map)) {
                    for (List<String> list : map.keySet()) {
                        sortList = list;
                        contextSearchDTO = map.get(list);
                    }
                }
                queryParam.setSorts(sortList);
                queryParam.setId(null);
                sortMap = this.getAboveList(queryParam);
                List<String> sortsList = new ArrayList<>();
                if (sortMap != null) {
                    for (String key : sortMap.keySet()) {
                        if (sortMap.get(key) != null) {
                            id = key;
                            sortsList = sortMap.get(key);
                        } else {
                            addId = key;
                        }
                    }
                    queryParam.setSorts(sortsList);
                } else {
                    queryParam.setSorts(sortList);
                }

            }
            ContextSearchInfoDTO belowList = this.getBelowList(queryParam);
            List<ContextSearchDTO> list = new ArrayList<>();
            if (belowList != null) {
                list = belowList.getLogs();
            }

            String contextSourceId = contextSearchDTO.getId();
            if (list.stream().noneMatch(z -> z.getId().equals(contextSourceId))) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId().equals(id)) {
                        list.add(i + 1, contextSearchDTO);
                    }
                }
            }
            if (addId != null) {
                ContextSearchQuery queryById = new ContextSearchQuery();
                queryById.setId(addId);
                HashMap<List<String>, ContextSearchDTO> addMap = this.getSorts(queryById);
                ContextSearchDTO contextDto = new ContextSearchDTO();
                if (addMap != null) {
                    for (List<String> addList : addMap.keySet()) {
                        contextDto = addMap.get(addList);
                    }
                }

                list.add(0, contextDto);
            }
            if (sortMap == null) {
                list.add(0, contextSearchDTO);
            }
            return belowList;
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }

    }

    /**
     * Create logType tree information
     *
     * @param logTypes log type
     * @return logType tree information
     */
    private List<LogTypeTreeDTO> createLogTypeTree(List<String> logTypes) {
        List<LogTypeTreeDTO> logTypeList = new ArrayList<>();
        int start;
        int end;
        StringBuilder sb = new StringBuilder(" ");
        for (String logType1 : logTypes) {
            final LogTypeTreeDTO tree = new LogTypeTreeDTO();
            start = logType1.indexOf(CommonConstants.HYPHEN) + 1;
            end = logType1.indexOf(CommonConstants.HYPHEN, start + 1);
            if (sb.indexOf(logType1.substring(start, end)) > 0) {
                continue;
            }
            sb.append(logType1, start, end);
            tree.setTypeName(logType1.substring(start, end));
            List<LogTypeTreeDTO> list = new ArrayList<>();
            StringBuilder ss = new StringBuilder(" ");
            for (String logType2 : logTypes) {
                start = logType2.indexOf(CommonConstants.HYPHEN) + 1;
                end = logType2.indexOf(CommonConstants.HYPHEN, start + 1);
                if (logType2.substring(start, end).equals(tree.getTypeName())) {
                    start = end + 1;
                    end = logType2.indexOf(CommonConstants.HYPHEN, logType2.indexOf(CommonConstants.HYPHEN, start) + 1);
                    LogTypeTreeDTO logTypeTreeDTO = new LogTypeTreeDTO();
                    if (end <= 0) {
                        end = logType2.indexOf(CommonConstants.HYPHEN, logType2.indexOf(CommonConstants.HYPHEN, start));
                    }
                    if (ss.indexOf(logType2.substring(start, end)) > 0) {
                        continue;
                    }
                    ss.append(logType2, start, end);
                    logTypeTreeDTO.setTypeName(logType2.substring(start, end));
                    list.add(logTypeTreeDTO);
                }
            }
            tree.setChildren(list);
            logTypeList.add(tree);
        }
        return logTypeList;
    }

    private static List<String> nodeList = null;
    private static long id;

    /**
     * Query log node information
     *
     * @return log node information
     */
    private List<String> getNodeId() {
        if (nodeList != null && System.currentTimeMillis() - id < MINUTE) {
            return nodeList;
        }
        synchronized (this) {
            if (nodeList != null && System.currentTimeMillis() - id < MINUTE) {
                return nodeList;
            }
            Set<String> indexs = esLogSearchUtils.indexList("ob-*");
            if (indexs == null || indexs.size() < 1) {
                return null;
            }
            List<String> nodeIdList = new ArrayList<>();
            for (String index : indexs) {
                String prefix = index.substring(index.indexOf(CommonConstants.HYPHEN) + 1);
                String dataType = prefix.substring(prefix.indexOf(CommonConstants.HYPHEN) + 1);
                String logType = dataType.substring(dataType.indexOf(CommonConstants.HYPHEN) + 1);
                String nodeId = logType.substring(logType.indexOf(CommonConstants.HYPHEN) + 1);
                nodeIdList.add(nodeId);
            }
            id = System.currentTimeMillis();
            nodeList = nodeIdList;
            return nodeIdList;
        }
    }

    /**
     * Query sorts by id
     *
     * @return sorts
     */
    @SuppressWarnings("rawtypes")
    private HashMap<List<String>, ContextSearchDTO> getSorts(ContextSearchQuery param) {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setId(param.getId());
        HashMap<List<String>, ContextSearchDTO> map = new HashMap<>();
        try {
            SearchResponse<HashMap> searchResponse = esLogSearchUtils.queryLogInfoById(queryParam);
            if (searchResponse == null) {
                return null;
            }
            List<Hit<HashMap>> hits = searchResponse.hits().hits();
            if (!CollectionUtils.isEmpty(hits)) {
                ContextSearchDTO contextSearchDto = new ContextSearchDTO();
                HashMap data = hits.get(0).source();
                if (!CollectionUtils.isEmpty(data)) {
                    contextSearchDto.setLogTime(data.get(CommonConstants.TIMESTAMP));
                    Map logTypeMap = null;
                    if (data.get(CommonConstants.FIELDS) instanceof Map) {
                        logTypeMap = (Map) data.get(CommonConstants.FIELDS);
                    }
                    if (logTypeMap != null) {
                        contextSearchDto.setLogType(logTypeMap.get(CommonConstants.LOG_TYPE));
                    }
                    contextSearchDto.setLogLevel(data.get(CommonConstants.LOG_LEVEL));
                    contextSearchDto.setLogData(data.get(CommonConstants.MESSAGE));
                    contextSearchDto.setLogClusterId(data.get(CommonConstants.CLUSTER_ID));
                    contextSearchDto.setLogNodeId(data.get(CommonConstants.NODE_ID));
                    contextSearchDto.setId(param.getId());
                }
                List<String> sorts = new ArrayList<>(hits.get(hits.size() - 1).sort());
                map.put(sorts, contextSearchDto);
            }
            return map;
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }


    /**
     * Query log aboveList information
     *
     * @return log aboveList information
     */
    @SuppressWarnings("rawtypes")
    private HashMap<String, List<String>> getAboveList(ContextSearchQuery param) {
        HashMap<String, List<String>> map = new HashMap<>();
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setSearchPhrase(param.getSearchPhrase());
        queryParam.setLogType(param.getLogType());
        queryParam.setLogLevel(param.getLogLevel());
        queryParam.setRowCount(param.getAboveCount() + 1);
        queryParam.setStartDate(param.getStartDate());
        queryParam.setEndDate(param.getEndDate());
        queryParam.setId(param.getId());
        queryParam.setOrder("ASC");
        queryParam.setSorts(param.getSorts());
        queryParam.setNodeId(param.getNodeId());
        int nodeIdCount = 0;
        if (queryParam.getNodeId() != null) {
            nodeIdCount = queryParam.getNodeId().size();
            List<String> list = new ArrayList<>();
            List<String> nodes = this.getNodeId();
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
            return null;
        } else {
            try {
                SearchResponse<HashMap> searchResponse = esLogSearchUtils.queryLogInfo(queryParam);
                if (searchResponse == null) {
                    return null;
                }
                List<Hit<HashMap>> hits = searchResponse.hits().hits();
                if (hits.size() < param.getAboveCount() + 1) {
                    map.put(hits.get(hits.size() - 1).id(), null);
                }
                if (!hits.isEmpty()) {
                    List<String> sorts = new ArrayList<>(hits.get(hits.size() - 1).sort());
                    map.put(hits.get(0).id(), sorts);
                }
                return map;
            } catch (Exception e) {
                log.info(e.getMessage());
                return null;
            }
        }
    }

    /**
     * Query log belowList information
     *
     * @return log belowList information
     */
    @SuppressWarnings({"rawtypes"})
    private ContextSearchInfoDTO getBelowList(ContextSearchQuery param) {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setSearchPhrase(param.getSearchPhrase());
        queryParam.setLogType(param.getLogType());
        queryParam.setLogLevel(param.getLogLevel());
        queryParam.setRowCount(param.getAboveCount() + param.getBelowCount() + 1);
        queryParam.setStartDate(param.getStartDate());
        queryParam.setEndDate(param.getEndDate());
        queryParam.setSorts(param.getSorts());
        queryParam.setNodeId(param.getNodeId());
        int nodeIdCount = 0;
        if (queryParam.getNodeId() != null) {
            nodeIdCount = queryParam.getNodeId().size();
            List<String> list = new ArrayList<>();
            List<String> nodes = this.getNodeId();
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
            return null;
        } else {
            var list = new ArrayList<ContextSearchDTO>();
            ContextSearchInfoDTO contextSearchInfoDTO = new ContextSearchInfoDTO();
            try {
                SearchResponse<HashMap> searchResponse = esLogSearchUtils.queryLogInfo(queryParam);
                if (searchResponse == null) {
                    return null;
                }
                List<Hit<HashMap>> hits = searchResponse.hits().hits();
                List<String> sorts = new ArrayList<>();
                for (var decodeBeanHit : hits) {
                    var docMap = decodeBeanHit.source();
                    String id = decodeBeanHit.id();
                    if (docMap != null) {
                        ContextSearchDTO contextSearchDto = new ContextSearchDTO();
                        contextSearchDto.setLogTime(docMap.get(CommonConstants.TIMESTAMP));
                        Map logTypeMap = null;
                        if (docMap.get(CommonConstants.FIELDS) instanceof Map) {
                            logTypeMap = (Map) docMap.get(CommonConstants.FIELDS);
                        }
                        if (logTypeMap != null) {
                            contextSearchDto.setLogType(logTypeMap.get(CommonConstants.LOG_TYPE));
                        }
                        contextSearchDto.setLogLevel(docMap.get(CommonConstants.LOG_LEVEL));
                        contextSearchDto.setLogData(docMap.get(CommonConstants.MESSAGE));
                        contextSearchDto.setLogClusterId(docMap.get(CommonConstants.CLUSTER_ID));
                        contextSearchDto.setLogNodeId(docMap.get(CommonConstants.NODE_ID));
                        contextSearchDto.setId(id);
                        list.add(contextSearchDto);
                    }
                }
                if (!hits.isEmpty()) {
                    sorts.addAll(hits.get(hits.size() - 1).sort());
                } else {
                    sorts.addAll(queryParam.getSorts());
                }
                contextSearchInfoDTO.setSorts(sorts);
                contextSearchInfoDTO.setLogs(list);
                return contextSearchInfoDTO;
            } catch (Exception e) {
                log.info(e.getMessage());
                return null;
            }
        }
    }
}
