package com.nctigba.observability.log.service.impl;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.nctigba.observability.log.model.dto.*;
import com.nctigba.observability.log.model.query.ContextSearchQuery;
import com.nctigba.observability.log.model.query.EsSearchQuery;
import com.nctigba.observability.log.service.LogSearchService;
import com.nctigba.observability.log.util.EsLogSearchUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


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
                    startDate.setTime(time - 1000 * 60 * 30);
                    queryParam.setStartDate(stringFormat.parse(dateFormat.format(startDate)));
                    queryParam.setEndDate(stringFormat.parse(dateFormat.format(endDate)));

                }
                LocalDateTime endTime = queryParam.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime startTime = queryParam.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                long interval = Duration.between(startTime, endTime).toMillis();
                EsSearchQuery esSearchQuery = new EsSearchQuery();
                esSearchQuery.setEndDate(queryParam.getEndDate());
                esSearchQuery.setStartDate(queryParam.getStartDate());
                esSearchQuery.setInterval(interval / 6);
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
                    listType = this.creatLogTypeTree(slist);
                } else {
                    listType = this.getLogType();
                }
                for (int i = 0; i < list.size(); i++) {
                    LogDistroMapDTO logDistroMapDTO = new LogDistroMapDTO();
                    logDistroMapDTO.setDateTime(list.get(i).keyAsString());
                    List<LogDistroMapInfoDTO> logDistroMapInfoDTOList = new ArrayList<>();
                    for (LogTypeTreeDTO logTypeTreeDTO : listType) {
                        long totalCount = 0;
                        LogDistroMapInfoDTO logDistroMapInfoDTO = new LogDistroMapInfoDTO();
                        logDistroMapInfoDTO.setLogType(logTypeTreeDTO.getTypeName());
                        List<LogDistroMapInfoDTO> logDistroMapList = new ArrayList<>();
                        for (int index = 0; index < logTypeTreeDTO.getChildren().size(); index++) {
                            List<StringTermsBucket> stringTermsBucket = list.get(i).aggregations().get("agg2").sterms().buckets().array();
                            for (int j = 0; j < stringTermsBucket.size(); j++) {
                                StringTermsBucket stringTermsBucket1 = stringTermsBucket.get(j);
                                if (stringTermsBucket1.key().contains(logTypeTreeDTO.getTypeName() + "-" + logTypeTreeDTO.getChildren().get(index).getTypeName())) {
                                    totalCount += stringTermsBucket1.docCount();
                                } else {
                                    continue;
                                }
                                LogDistroMapInfoDTO logDistMapInfo = new LogDistroMapInfoDTO();
                                logDistMapInfo.setLogType(logTypeTreeDTO.getChildren().get(index).getTypeName());
                                logDistMapInfo.setLogCount(stringTermsBucket1.docCount());
                                logDistroMapList.add(logDistMapInfo);
                            }
                        }
                        logDistroMapInfoDTO.setLogCount(totalCount);
                        logDistroMapInfoDTO.setChildren(logDistroMapList);
                        logDistroMapInfoDTOList.add(logDistroMapInfoDTO);
                        logDistroMapDTO.setLogCounts(logDistroMapInfoDTOList);
                        if (lList == null || !lList.contains(logDistroMapDTO)) {
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
                    if (docMap != null) {
                        LogDetailInfoDTO logDetailInfoDTO = new LogDetailInfoDTO();
                        logDetailInfoDTO.setLogTime(docMap.get("@timestamp"));
                        Map logTypeMap = null;
                        if (docMap.get("fields") instanceof Map) {
                            logTypeMap = (Map) docMap.get("fields");
                        }
                        logDetailInfoDTO.setLogType(logTypeMap.get("log_type"));
                        logDetailInfoDTO.setLogLevel(docMap.get("log_level"));
                        logDetailInfoDTO.setLogData(docMap.get("message"));
                        logDetailInfoDTO.setLogClusterId(docMap.get("clusterId"));
                        logDetailInfoDTO.setLogNodeId(docMap.get("nodeId"));
                        logDetailInfoDTO.setId(id);
                        list.add(logDetailInfoDTO);
                    }
                }
                if (!hits.isEmpty()) sorts.addAll(hits.get(hits.size() - 1).sort());
                else sorts.addAll(queryParam.getSorts());
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
            return this.creatLogTypeTree(list);
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
        try {
            if (StringUtils.isEmpty(queryParam.getSorts()) && !StringUtils.isEmpty(queryParam.getId())) {
                HashMap<List<String>, ContextSearchDTO> map = this.getSorts(queryParam);
                List<String> sortList = new ArrayList<>();
                for (List<String> list : map.keySet()) {
                    sortList = list;
                    contextSearchDTO = map.get(list);
                }
                queryParam.setSorts(sortList);
                queryParam.setId(null);
                HashMap<String, List<String>> sortMap = this.getAboveList(queryParam);
                List<String> sortsList = new ArrayList<>();
                for (String key : sortMap.keySet()) {
                    id = key;
                    sortsList = sortMap.get(key);
                }
                queryParam.setSorts(sortsList);
            }
            ContextSearchInfoDTO belowList = this.getBelowList(queryParam);
            List<ContextSearchDTO> list = belowList.getLogs();
            String contextSourceId = contextSearchDTO.getId();
            if (list.stream().filter(z -> z.getId().equals(contextSourceId)).count() == 0) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId().equals(id)) {
                        list.add(i + 1, contextSearchDTO);
                    }
                }
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
    private List<LogTypeTreeDTO> creatLogTypeTree(List<String> logTypes) {
        List<LogTypeTreeDTO> logTypeList = new ArrayList<>();
        int start;
        int end;
        StringBuilder sb = new StringBuilder(" ");
        for (String logType1 : logTypes) {
            final LogTypeTreeDTO tree = new LogTypeTreeDTO();
            start = logType1.indexOf("-") + 1;
            end = logType1.indexOf("-", start + 1);
            if (sb.indexOf(logType1.substring(start, end)) > 0) {
                continue;
            }
            sb.append(logType1, start, end);
            tree.setTypeName(logType1.substring(start, end));
            List<LogTypeTreeDTO> list = new ArrayList<>();
            StringBuilder ss = new StringBuilder(" ");
            for (String logType2 : logTypes) {
                start = logType2.indexOf("-") + 1;
                end = logType2.indexOf("-", start + 1);
                if (logType2.substring(start, end).equals(tree.getTypeName())) {
                    start = end + 1;
                    end = logType2.indexOf("-", logType2.indexOf("-", start) + 1);
                    LogTypeTreeDTO logTypeTreeDTO = new LogTypeTreeDTO();
                    if (end <= 0) {
                        end = logType2.indexOf("-", logType2.indexOf("-", start));
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
        if (nodeList != null && System.currentTimeMillis() - id < 60000) {
            return nodeList;
        }
        synchronized (this) {
            if (nodeList != null && System.currentTimeMillis() - id < 60000) {
                return nodeList;
            }
            Set<String> indexs = esLogSearchUtils.indexList("ob-*");
            if (indexs == null || indexs.size() < 1) {
                return null;
            }
            List<String> nodeIdList = new ArrayList<>();
            for (String index : indexs) {
                String prefix = index.substring(index.indexOf("-") + 1);
                String dataType = prefix.substring(prefix.indexOf("-") + 1);
                String logType = dataType.substring(dataType.indexOf("-") + 1);
                String nodeId = logType.substring(logType.indexOf("-") + 1);
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
            List<String> sorts = new ArrayList<>();
            if (!hits.isEmpty()) {
                sorts.addAll(hits.get(hits.size() - 1).sort());
                ContextSearchDTO contextSearchDto = new ContextSearchDTO();
                contextSearchDto.setLogTime(hits.get(0).source().get("@timestamp"));
                Map logTypeMap = null;
                if (hits.get(0).source().get("fields") instanceof Map) {
                    logTypeMap = (Map) hits.get(0).source().get("fields");
                }
                contextSearchDto.setLogType(logTypeMap.get("log_type"));
                contextSearchDto.setLogLevel(hits.get(0).source().get("log_level"));
                contextSearchDto.setLogData(hits.get(0).source().get("message"));
                contextSearchDto.setLogClusterId(hits.get(0).source().get("clusterId"));
                contextSearchDto.setLogNodeId(hits.get(0).source().get("nodeId"));
                contextSearchDto.setId(param.getId());
                map.put(sorts, contextSearchDto);
            }
            log.info(String.valueOf(sorts));
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
                if (!hits.isEmpty()) {
                    List<String> sorts = new ArrayList<>();
                    sorts.addAll(hits.get(hits.size() - 1).sort());
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    private ContextSearchInfoDTO getBelowList(ContextSearchQuery param) {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setSearchPhrase(param.getSearchPhrase());
        queryParam.setLogType(param.getLogType());
        queryParam.setLogLevel(param.getLogLevel());
        queryParam.setRowCount(param.getAboveCount() + param.getBelowCount() + 1);
        queryParam.setStartDate(param.getStartDate());
        queryParam.setEndDate(param.getEndDate());
        queryParam.setSorts(param.getSorts());
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
                        contextSearchDto.setLogTime(docMap.get("@timestamp"));
                        Map logTypeMap = null;
                        if (docMap.get("fields") instanceof Map) {
                            logTypeMap = (Map) docMap.get("fields");
                        }
                        contextSearchDto.setLogType(logTypeMap.get("log_type"));
                        contextSearchDto.setLogLevel(docMap.get("log_level"));
                        contextSearchDto.setLogData(docMap.get("message"));
                        contextSearchDto.setLogClusterId(docMap.get("clusterId"));
                        contextSearchDto.setLogNodeId(docMap.get("nodeId"));
                        contextSearchDto.setId(id);
                        list.add(contextSearchDto);
                    }
                }
                if (!hits.isEmpty()) {
                    sorts.addAll(hits.get(hits.size() - 1).sort());
                } else sorts.addAll(queryParam.getSorts());
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
