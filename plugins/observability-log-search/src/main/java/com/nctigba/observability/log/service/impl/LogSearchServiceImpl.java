package com.nctigba.observability.log.service.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.nctigba.observability.log.model.dto.*;
import com.nctigba.observability.log.model.query.EsSearchQuery;
import com.nctigba.observability.log.service.LogSearchService;
import com.nctigba.observability.log.util.EsLogSearchUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
    private ElasticsearchClient client;

    @Override
    public List<LogDistroMapDTO> getLogDistroMap(EsSearchQuery queryParam) throws ParseException {
        int nodeIdCount = 0;
        if (queryParam.getNodeId() != null) {
            nodeIdCount = queryParam.getNodeId().size();
            List<String> list = new ArrayList<>();
            for (String nodeId : queryParam.getNodeId()) {
                if (!this.getNodeId().contains(nodeId)) {
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
            long hour = Duration.between(startTime, endTime).toHours();
            List<LogDistroMapDTO> mapList = new ArrayList<>();
            List<Date> ponits = new ArrayList<>();
            ponits.add(Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()));
            if (hour <= 1) {
                long second = Duration.between(startTime, endTime).getSeconds();
                for (long i = second / 6; i <= second; i += second / 6) {
                    ponits.add(Date.from(endTime.minusSeconds(i).atZone(ZoneId.systemDefault()).toInstant()));

                }
            } else if (hour <= 24) {
                long minute = Duration.between(startTime, endTime).toMinutes();
                for (long i = minute / 6; i <= minute; i += minute / 6) {
                    ponits.add(Date.from(endTime.minusMinutes(i).atZone(ZoneId.systemDefault()).toInstant()));
                }
            } else if (hour <= 7 * 24) {
                for (long i = hour / 6; i <= hour; i += hour / 6) {
                    ponits.add(Date.from(endTime.minusHours(i).atZone(ZoneId.systemDefault()).toInstant()));
                }
            } else {
                long day = Duration.between(startTime, endTime).toDays();
                for (long i = day / 6; i <= day; i += day / 6) {
                    ponits.add(Date.from(endTime.minusDays(i).atZone(ZoneId.systemDefault()).toInstant()));
                }
            }
            for (int i = ponits.size() - 1; i >= 0; i--) {
                for (int j = ponits.size() - 2; j >= 0; j--) {
                    if (i - j != 1) {
                        continue;
                    }
                    queryParam.setStartDate(ponits.get(i));
                    queryParam.setEndDate(ponits.get(j));
                    List<LogTypeTreeDTO> list;
                    if (queryParam.getLogType() != null) {
                        List<String> slist = new ArrayList<>();
                        for (String logType : queryParam.getLogType()) {
                            slist.add("ob-" + logType + "-");
                        }
                        list = this.creatLogTypeTree(slist);
                    } else {
                        list = this.getLogType();
                    }
                    List<LogDistroMapInfoDTO> logDistroMapInfoDTOList = new ArrayList<>();
                    for (LogTypeTreeDTO logTypeTreeDTO : list) {
                        long totalCount = 0;
                        LogDistroMapInfoDTO logDistroMapInfoDTO = new LogDistroMapInfoDTO();
                        logDistroMapInfoDTO.setLogType(logTypeTreeDTO.getTypeName());
                        List<LogDistroMapInfoDTO> logDistroMapList = new ArrayList<>();
                        for (int index = 0; index < logTypeTreeDTO.getChildren().size(); index++) {
                            List<String> logList = new ArrayList<>();
                            logList.add(logTypeTreeDTO.getTypeName() + "-" + logTypeTreeDTO.getChildren().get(index).getTypeName());
                            EsSearchQuery esSearchQuery = new EsSearchQuery();
                            esSearchQuery.setEndDate(ponits.get(j));
                            esSearchQuery.setStartDate(ponits.get(i));
                            esSearchQuery.setLogType(logList);
                            if (!queryParam.isEmptyObject()) {
                                esSearchQuery.setLogLevel(queryParam.getLogLevel());
                                esSearchQuery.setNodeId(queryParam.getNodeId());
                                esSearchQuery.setSearchPhrase(queryParam.getSearchPhrase());
                            }
                            long count = esLogSearchUtils.queryLogCount(esSearchQuery);
                            totalCount += count;
                            LogDistroMapInfoDTO logDistMapInfo = new LogDistroMapInfoDTO();
                            logDistMapInfo.setLogType(logTypeTreeDTO.getChildren().get(index).getTypeName());
                            logDistMapInfo.setLogCount(count);
                            logDistroMapList.add(logDistMapInfo);
                        }
                        logDistroMapInfoDTO.setLogCount(totalCount);
                        logDistroMapInfoDTO.setChildren(logDistroMapList);
                        logDistroMapInfoDTOList.add(logDistroMapInfoDTO);
                    }
                    LogDistroMapDTO logDistroMapDTO = new LogDistroMapDTO();
                    logDistroMapDTO.setDateTime(ponits.get(i));
                    logDistroMapDTO.setLogCounts(logDistroMapInfoDTOList);
                    mapList.add(logDistroMapDTO);
                }

            }
            return mapList;
        }

    }


    @Override
    public LogInfoDTO getLogByQuery(EsSearchQuery queryParam) {
        int nodeIdCount = 0;
        if (queryParam.getNodeId() != null) {
            nodeIdCount = queryParam.getNodeId().size();
            List<String> list = new ArrayList<>();
            for (String nodeId : queryParam.getNodeId()) {
                if (!this.getNodeId().contains(nodeId)) {
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
                if (queryParam.getScollId() == null) {
                    SearchResponse<HashMap> searchResponse = esLogSearchUtils.queryLogInfo(queryParam);
                    List<Hit<HashMap>> hits = searchResponse.hits().hits();
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
                    logInfoDTO.setScrollId(searchResponse.scrollId());
                    logInfoDTO.setLogs(list);
                } else {
                    ScrollResponse scrollResponse = client.scroll(s -> s.scrollId(queryParam.getScollId()).scroll(t -> t.time("10s")), HashMap.class);
                    String scollId = scrollResponse.scrollId();
                    List<Hit<HashMap>> hit = scrollResponse.hits().hits();
                    for (var decodeBeanHit : hit) {
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
                    logInfoDTO.setScrollId(scollId);
                    logInfoDTO.setLogs(list);
                }
            } catch (ElasticsearchException e) {
                log.info(e.getMessage());
            } catch (IOException e) {
                log.info(e.getMessage());
            }
            return logInfoDTO;
        }
    }


    @Override
    public List<LogTypeTreeDTO> getLogType() {
        Set<String> logTypes = esLogSearchUtils.indexList("ob-*");
        List<String> list = new ArrayList<>(logTypes);
        return this.creatLogTypeTree(list);
    }

    @Override
    public List<String> getLogLevel() {
        EsSearchQuery queryParam = new EsSearchQuery();
        List<String> logLevelList = new ArrayList<>();
        try {
            var searchResponse = esLogSearchUtils.queryLogLevel(queryParam);
            Map<String, Aggregate> aggregations = searchResponse.aggregations();
            StringTermsAggregate logLevels = aggregations.get("LogLevelMap").sterms();
            List<StringTermsBucket> termsBucketList = logLevels.buckets().array();
            for (StringTermsBucket bucket : termsBucketList) {
                logLevelList.add(bucket.key());
            }
        } catch (ElasticsearchException e) {
            log.info(e.getMessage());
        }
        return logLevelList;
    }

    /**
     * Create logType tree information
     *
     * @param logTypes log type
     * @return logType tree information
     */
    public List<LogTypeTreeDTO> creatLogTypeTree(List<String> logTypes) {
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
    public List<String> getNodeId() {
        Set<String> indexs = esLogSearchUtils.indexList("ob-*");
        List<String> nodeIdList = new ArrayList<>();
        for (String index : indexs) {
            nodeIdList.add(index.substring(index.lastIndexOf("-") + 1));
        }
        return nodeIdList;
    }

}
