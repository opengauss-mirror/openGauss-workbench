package com.nctigba.observability.log.service.impl;

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

import co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nctigba.observability.log.config.ElasticsearchProvider;
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
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


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

    @Autowired
    private ElasticsearchProvider client;


    @Override
    public List<LogDistroMapDTO> getLogDistroMap(EsSearchQuery queryParam) {
        try {
            int nodeIdCount = 0;
            if (queryParam.getNodeId() != null) {
                nodeIdCount = queryParam.getNodeId().size();
                List<String> list = new ArrayList<>();
                for (String nodeId : queryParam.getNodeId()) {
                    if (this.getNodeId() != null && !this.getNodeId().contains(nodeId)) {
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public LogInfoDTO getLogByQuery(EsSearchQuery queryParam) {
        int nodeIdCount = 0;
        if (queryParam.getNodeId() != null) {
            nodeIdCount = queryParam.getNodeId().size();
            List<String> list = new ArrayList<>();
            for (String nodeId : queryParam.getNodeId()) {
                if (this.getNodeId() != null && !this.getNodeId().contains(nodeId)) {
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
                        list.add(logDetailInfoDTO);
                    }
                }
                if (!hits.isEmpty())
                    sorts.addAll(hits.get(hits.size() - 1).sort());
                else
                    sorts.addAll(queryParam.getSorts());
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
        try {
            List<ContextSearchDTO> aboveList = this.getAboveList(queryParam);
            if (aboveList.size() < 1) {
                return null;
            }
            ContextSearchDTO contextSearchDto = aboveList.get(aboveList.size() - 1);
            String time = (String) contextSearchDto.getLogTime();
            SimpleDateFormat stringFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date d = stringFormat.parse(time.substring(0, 19).replace("T", " "));
            long times = d.getTime();
            Date startDate = new Date();
            startDate.setTime(times - 1000 * 60 * 60 * Integer.valueOf(time.substring(time.indexOf("+") + 1, time.lastIndexOf(":"))));
            String s = stringFormat.format(startDate) + time.substring(19, 23);
            Date sdd = sFormat.parse(s);
            queryParam.setLogDate(sdd);
            ContextSearchInfoDTO belowList = this.getBelowList(queryParam);
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

    /**
     * Query log node information
     *
     * @return log node information
     */
    private List<String> getNodeId() {
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
        return nodeIdList;
    }

    /**
     * Query log aboveList information
     *
     * @return log aboveList information
     */
    @SuppressWarnings("rawtypes")
    private List<ContextSearchDTO> getAboveList(ContextSearchQuery param) {
        EsSearchQuery queryParam = new EsSearchQuery();
        queryParam.setSearchPhrase(param.getSearchPhrase());
        queryParam.setLogType(param.getLogType());
        queryParam.setLogLevel(param.getLogLevel());
        queryParam.setRowCount(param.getAboveCount());
        queryParam.setStartDate(param.getLogDate());
        queryParam.setOrder("ASC");
        int nodeIdCount = 0;
        if (queryParam.getNodeId() != null) {
            nodeIdCount = queryParam.getNodeId().size();
            List<String> list = new ArrayList<>();
            for (String nodeId : queryParam.getNodeId()) {
                if (this.getNodeId() != null && !this.getNodeId().contains(nodeId)) {
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
            try {
                SearchResponse<HashMap> searchResponse = esLogSearchUtils.queryLogInfo(queryParam);
                if (searchResponse == null) {
                    return null;
                }
                List<Hit<HashMap>> hits = searchResponse.hits().hits();
                for (var decodeBeanHit : hits) {
                    var docMap = decodeBeanHit.source();
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
                        list.add(contextSearchDto);
                    }
                }
                return list;
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
        queryParam.setEndDate(param.getLogDate());
        queryParam.setScrollId(param.getScrollId());
        int nodeIdCount = 0;
        if (queryParam.getNodeId() != null) {
            nodeIdCount = queryParam.getNodeId().size();
            List<String> list = new ArrayList<>();
            for (String nodeId : queryParam.getNodeId()) {
                if (this.getNodeId() != null && !this.getNodeId().contains(nodeId)) {
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
                if (queryParam.getScrollId() == null || "".equals(queryParam.getScrollId())) {
                    SearchResponse<HashMap> searchResponse = esLogSearchUtils.queryLogInfo(queryParam);
                    if (searchResponse == null) {
                        return null;
                    }
                    List<Hit<HashMap>> hits = searchResponse.hits().hits();
                    for (var decodeBeanHit : hits) {
                        var docMap = decodeBeanHit.source();
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
                            list.add(contextSearchDto);
                        }
                    }
                    contextSearchInfoDTO.setScrollId(searchResponse.scrollId());
                    contextSearchInfoDTO.setLogs(list);
                } else {
                    ScrollResponse scrollResponse = client.client().scroll(s -> s.scrollId(queryParam.getScrollId()).scroll(t -> t.time("10s")), HashMap.class);
                    String scrollId = scrollResponse.scrollId();
                    List<Hit<HashMap>> hit = scrollResponse.hits().hits();
                    for (var decodeBeanHit : hit) {
                        var docMap = decodeBeanHit.source();
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
                            list.add(contextSearchDto);
                        }
                    }
                    contextSearchInfoDTO.setScrollId(scrollId);
                    contextSearchInfoDTO.setLogs(list);
                }
                return contextSearchInfoDTO;
            } catch (Exception e) {
                log.info(e.getMessage());
                return null;
            }
        }
    }

}
