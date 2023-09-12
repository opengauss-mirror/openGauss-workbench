/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.config.ElasticsearchProvider;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.AlertRecordDto;
import com.nctigba.alert.monitor.dto.AlertRelationDto;
import com.nctigba.alert.monitor.dto.AlertStatisticsDto;
import com.nctigba.alert.monitor.dto.LogDetailInfoDTO;
import com.nctigba.alert.monitor.dto.LogInfoDTO;
import com.nctigba.alert.monitor.entity.AlertRecord;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
import com.nctigba.alert.monitor.entity.NctigbaEnv;
import com.nctigba.alert.monitor.mapper.AlertRecordMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import com.nctigba.alert.monitor.model.AlertRecordReq;
import com.nctigba.alert.monitor.model.AlertStatisticsReq;
import com.nctigba.alert.monitor.service.AlertRecordService;
import com.nctigba.alert.monitor.service.PrometheusService;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nctigba.alert.monitor.constant.CommonConstants.FIRING_STATUS;
import static com.nctigba.alert.monitor.constant.CommonConstants.READ_STATUS;
import static com.nctigba.alert.monitor.constant.CommonConstants.UNREAD_STATUS;

/**
 * @author wuyuebin
 * @date 2023/5/17 10:35
 * @description
 */
@Service
public class AlertRecordServiceImpl extends ServiceImpl<AlertRecordMapper, AlertRecord> implements AlertRecordService {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterService clusterService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterNodeService clusterNodeService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    @Autowired
    private AlertTemplateRuleMapper templateRuleMapper;

    @Autowired
    private AlertTemplateRuleItemMapper templateRuleItemMapper;

    @Autowired
    private PrometheusService prometheusService;

    @Autowired
    private NctigbaEnvMapper envMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private ElasticsearchProvider clientProvider;

    @Override
    public Page<AlertRecordDto> getListPage(AlertRecordReq alertRecordReq, Page page) {
        LocalDateTime startTime = StrUtil.isNotBlank(alertRecordReq.getStartTime()) ? LocalDateTime.parse(
            alertRecordReq.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        LocalDateTime endTime = StrUtil.isNotBlank(alertRecordReq.getEndTime()) ? LocalDateTime.parse(
            alertRecordReq.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        String clusterNodeId = alertRecordReq.getClusterNodeId();
        Page<AlertRecord> recordPage = this.baseMapper.selectPage(page, Wrappers.<AlertRecord>lambdaQuery()
            .eq(StrUtil.isNotBlank(clusterNodeId), AlertRecord::getClusterNodeId, clusterNodeId)
            .in(StrUtil.isNotBlank(alertRecordReq.getAlertStatus()), AlertRecord::getAlertStatus,
                StrUtil.isNotBlank(alertRecordReq.getAlertStatus())
                    ? alertRecordReq.getAlertStatus().split(CommonConstants.DELIMITER) : new String[]{})
            .in(StrUtil.isNotBlank(alertRecordReq.getRecordStatus()), AlertRecord::getRecordStatus,
                StrUtil.isNotBlank(alertRecordReq.getRecordStatus())
                    ? alertRecordReq.getRecordStatus().split(CommonConstants.DELIMITER) : new String[]{})
            .in(StrUtil.isNotBlank(alertRecordReq.getAlertLevel()), AlertRecord::getLevel,
                StrUtil.isNotBlank(alertRecordReq.getAlertLevel())
                    ? alertRecordReq.getAlertLevel().split(CommonConstants.DELIMITER) : new String[]{})
            .ge(startTime != null, AlertRecord::getStartTime, startTime)
            .le(endTime != null, AlertRecord::getEndTime, endTime).orderByDesc(AlertRecord::getId));
        Page<AlertRecordDto> recordDtoPage = new Page<>();
        List<AlertRecord> records = recordPage.getRecords();
        recordDtoPage.setTotal(recordPage.getTotal()).setCurrent(recordPage.getCurrent()).setSize(recordPage.getSize());
        if (CollectionUtil.isEmpty(records)) {
            recordDtoPage.setRecords(new ArrayList<>());
            return recordDtoPage;
        }
        List<String> clusterNodeIdList = records.stream().map(item -> item.getClusterNodeId()).collect(
            Collectors.toList());
        List<OpsClusterNodeEntity> opsClusterNodeEntities = clusterNodeService.listByIds(clusterNodeIdList);
        List<String> hostIds = opsClusterNodeEntities.stream().map(item -> item.getHostId()).collect(
            Collectors.toList());
        List<String> clusterIds = opsClusterNodeEntities.stream().map(item -> item.getClusterId()).collect(
            Collectors.toList());
        List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIds);
        List<OpsClusterEntity> opsClusterEntities = clusterService.listByIds(clusterIds);

        List<AlertRecordDto> list = new ArrayList<>();
        for (AlertRecord record : records) {
            list.add(alertRecordToDto(record, opsClusterNodeEntities, opsHostEntities, opsClusterEntities));
        }
        recordDtoPage.setRecords(list);
        return recordDtoPage;
    }

    private AlertRecordDto alertRecordToDto(
        AlertRecord record, List<OpsClusterNodeEntity> opsClusterNodeEntities,
        List<OpsHostEntity> opsHostEntities, List<OpsClusterEntity> opsClusterEntities) {
        AlertRecordDto alertRecordDto = new AlertRecordDto();
        BeanUtil.copyProperties(record, alertRecordDto);
        String clusterNodeId0 = record.getClusterNodeId();
        OpsClusterNodeEntity opsClusterNodeEntity = opsClusterNodeEntities.stream().filter(
            item -> item.getClusterNodeId().equals(clusterNodeId0)).findFirst().get();
        OpsHostEntity opsHostEntity = opsHostEntities.stream().filter(
            item -> item.getHostId().equals(opsClusterNodeEntity.getHostId())).findFirst().get();
        OpsClusterEntity opsClusterEntity = opsClusterEntities.stream().filter(
            item -> item.getClusterId().equals(opsClusterNodeEntity.getClusterId())).findFirst().get();
        String nodeName = opsClusterEntity.getClusterId() + "/" + opsHostEntity.getPublicIp() + ":"
            + opsClusterEntity.getPort() + "(" + opsClusterNodeEntity.getClusterRole() + ")";
        alertRecordDto.setClusterNodeName(nodeName).setClusterId(opsClusterEntity.getClusterId()).setHostIpAndPort(
            opsHostEntity.getPublicIp() + ":" + opsClusterEntity.getPort()).setNodeRole(
            opsClusterNodeEntity.getClusterRole().name());
        return alertRecordDto;
    }

    @Override
    public AlertStatisticsDto alertRecordStatistics(AlertStatisticsReq alertStatisticsReq) {
        LocalDateTime startTime = StrUtil.isNotBlank(alertStatisticsReq.getStartTime()) ? LocalDateTime.parse(
            alertStatisticsReq.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        LocalDateTime endTime = StrUtil.isNotBlank(alertStatisticsReq.getEndTime()) ? LocalDateTime.parse(
            alertStatisticsReq.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        Long total = this.baseMapper.selectCount(
            Wrappers.<AlertRecord>lambdaQuery().eq(StrUtil.isNotBlank(alertStatisticsReq.getClusterNodeId()),
                    AlertRecord::getClusterNodeId, alertStatisticsReq.getClusterNodeId())
                .ge(startTime != null, AlertRecord::getStartTime, startTime)
                .le(endTime != null, AlertRecord::getEndTime, endTime));

        AlertStatisticsDto alertStatisticsDto = new AlertStatisticsDto();
        alertStatisticsDto.setTotalNum(total.intValue());
        AlertStatisticsDto statusCountStatistics = setAndGetAlertStatusCount(alertStatisticsReq.getClusterNodeId(),
            startTime, endTime);
        alertStatisticsDto.setFiringNum(statusCountStatistics.getFiringNum()).setRecoverNum(
            statusCountStatistics.getRecoverNum());

        AlertStatisticsDto recordStatusCountStatistics =
            setAndGetRecordStatusCount(alertStatisticsReq.getClusterNodeId(), startTime, endTime);
        alertStatisticsDto.setUnReadNum(recordStatusCountStatistics.getUnReadNum()).setReadNum(
            recordStatusCountStatistics.getReadNum());

        AlertStatisticsDto levelCountStatistics = setAndGetLevelCount(alertStatisticsReq.getClusterNodeId(),
            startTime, endTime);
        alertStatisticsDto.setSeriousNum(levelCountStatistics.getSeriousNum()).setWarnNum(
            levelCountStatistics.getWarnNum()).setInfoNum(levelCountStatistics.getInfoNum());

        return alertStatisticsDto;
    }

    private AlertStatisticsDto setAndGetAlertStatusCount(
        String clusterNodeId, LocalDateTime startTime,
        LocalDateTime endTime) {
        QueryWrapper<AlertRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("alert_status as alertStatus,count(1) count").eq(
                StrUtil.isNotBlank(clusterNodeId), "cluster_node_id", clusterNodeId)
            .ge(startTime != null, "start_time", startTime)
            .le(endTime != null, "end_time", endTime).groupBy("alert_status");
        List<Map<String, Object>> alertStatusMaps = this.listMaps(queryWrapper);
        AlertStatisticsDto alertStatisticsDto = new AlertStatisticsDto();
        for (Map<String, Object> alertStatusMap : alertStatusMaps) {
            if (alertStatusMap.get("alertstatus") == null) {
                continue;
            }
            int count = 0;
            if (alertStatusMap.get("count") instanceof Long) {
                count = ((Long) alertStatusMap.get("count")).intValue();
            }
            if (alertStatusMap.get("alertstatus").equals(FIRING_STATUS)) {
                alertStatisticsDto.setFiringNum(count);
            } else {
                alertStatisticsDto.setRecoverNum(count);
            }
        }
        return alertStatisticsDto;
    }

    private AlertStatisticsDto setAndGetRecordStatusCount(
        String clusterNodeId, LocalDateTime startTime,
        LocalDateTime endTime) {
        QueryWrapper<AlertRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("record_status as recordStatus,count(1) count").eq(
                StrUtil.isNotBlank(clusterNodeId), "cluster_node_id",
                clusterNodeId)
            .ge(startTime != null, "start_time", startTime)
            .le(endTime != null, "end_time", endTime).groupBy("record_status");
        List<Map<String, Object>> recordStatusMaps = this.listMaps(queryWrapper);
        AlertStatisticsDto alertStatisticsDto = new AlertStatisticsDto();
        for (Map<String, Object> recordStatusMap : recordStatusMaps) {
            if (recordStatusMap.get("recordstatus") == null) {
                continue;
            }
            int count = 0;
            if (recordStatusMap.get("count") instanceof Long) {
                count = ((Long) recordStatusMap.get("count")).intValue();
            }
            if (recordStatusMap.get("recordstatus").equals(UNREAD_STATUS)) {
                alertStatisticsDto.setUnReadNum(count);
            } else {
                alertStatisticsDto.setReadNum(count);
            }
        }
        return alertStatisticsDto;
    }

    private AlertStatisticsDto setAndGetLevelCount(
        String clusterNodeId, LocalDateTime startTime,
        LocalDateTime endTime) {
        QueryWrapper<AlertRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("level ,count(1) count").eq(StrUtil.isNotBlank(clusterNodeId),
                "cluster_node_id", clusterNodeId)
            .ge(startTime != null, "start_time", startTime)
            .le(endTime != null, "end_time", endTime).groupBy("level");
        List<Map<String, Object>> levelMaps = this.listMaps(queryWrapper);
        AlertStatisticsDto alertStatisticsDto = new AlertStatisticsDto();
        for (Map<String, Object> levelMap : levelMaps) {
            if (levelMap.get("level") == null) {
                continue;
            }
            int count = 0;
            if (levelMap.get("count") instanceof Long) {
                count = ((Long) levelMap.get("count")).intValue();
            }
            if (levelMap.get("level").equals(CommonConstants.SERIOUS)) {
                alertStatisticsDto.setSeriousNum(count);
            } else if (levelMap.get("level").equals(CommonConstants.WARN)) {
                alertStatisticsDto.setWarnNum(count);
            } else {
                alertStatisticsDto.setInfoNum(count);
            }
        }
        return alertStatisticsDto;
    }

    @Override
    public String markAsRead(String ids) {
        String[] idArr = ids.split(CommonConstants.DELIMITER);
        LambdaUpdateWrapper<AlertRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(AlertRecord::getId, idArr);
        updateWrapper.set(AlertRecord::getRecordStatus, READ_STATUS).set(AlertRecord::getUpdateTime,
            LocalDateTime.now());
        update(null, updateWrapper);
        return "success";
    }

    @Override
    public AlertRecordDto getById(Long id) {
        AlertRecordDto alertRecordDto = new AlertRecordDto();
        AlertRecord alertRecord = this.baseMapper.selectById(id);
        if (alertRecord == null) {
            return alertRecordDto;
        }
        BeanUtil.copyProperties(alertRecord, alertRecordDto);
        String clusterNodeId = alertRecordDto.getClusterNodeId();
        OpsHostEntity opsHostEntity = new OpsHostEntity();
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        OpsClusterNodeEntity opsClusterNodeEntity = clusterNodeService.getById(clusterNodeId);
        if (opsClusterNodeEntity == null) {
            return alertRecordDto;
        }
        if (StrUtil.isNotBlank(opsClusterNodeEntity.getHostId())) {
            opsHostEntity = hostFacade.getById(opsClusterNodeEntity.getHostId());
        }
        if (StrUtil.isNotBlank(opsClusterNodeEntity.getClusterId())) {
            opsClusterEntity = clusterService.getById(opsClusterNodeEntity.getClusterId());
        }
        String nodeName =
            opsClusterEntity.getClusterId() + "/" + opsHostEntity.getPublicIp() + ":" + opsClusterEntity.getPort()
                + "(" + opsClusterNodeEntity.getClusterRole() + ")";
        alertRecordDto.setClusterNodeName(nodeName).setHostIpAndPort(
            opsHostEntity.getPublicIp() + ":" + opsClusterEntity.getPort()).setClusterId(
            opsClusterEntity.getClusterId()).setNodeRole(opsClusterNodeEntity.getClusterRole() != null
            ? opsClusterNodeEntity.getClusterRole().name() : "");
        return alertRecordDto;
    }

    @Override
    public List<AlertRelationDto> getRelationData(Long id) {
        AlertRecord alertRecord = this.baseMapper.selectById(id);
        if (StrUtil.isBlank(alertRecord.getTemplateRuleType())
            || !alertRecord.getTemplateRuleType().equals(CommonConstants.INDEX_RULE)) {
            return new ArrayList<>();
        }
        Long templateRuleId = alertRecord.getTemplateRuleId();
        List<AlertTemplateRuleItem> templateRuleItems = templateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItem>lambdaQuery().eq(AlertTemplateRuleItem::getTemplateRuleId,
                templateRuleId));
        List<AlertRelationDto> relationDtoList = new ArrayList<>();
        NctigbaEnv promEnv = envMapper
            .selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, NctigbaEnv.Type.PROMETHEUS));
        OpsHostEntity opsHostEntity = hostFacade.getById(promEnv.getHostid());
        for (AlertTemplateRuleItem templateRuleItem : templateRuleItems) {
            AlertRelationDto relationDto = new AlertRelationDto();
            relationDto.setName(MessageSourceUtil.get(templateRuleItem.getRuleExpName())).setUnit(
                templateRuleItem.getUnit()).setStartTime(alertRecord.getStartTime()).setEndTime(
                alertRecord.getEndTime());

            LocalDateTime minTime = alertRecord.getStartTime().minusMinutes(30L);
            LocalDateTime maxTime = alertRecord.getEndTime().plusMinutes(30L);
            relationDto.setMinTime(minTime).setMaxTime(maxTime);
            String ruleExp = templateRuleItem.getRuleExp();
            ruleExp = ruleExp.replace("${instances}", alertRecord.getClusterNodeId());
            Number[][] datas = prometheusService.queryRange(opsHostEntity.getPublicIp(), promEnv.getPort().toString(),
                ruleExp, minTime, maxTime);
            relationDto.setDatas(datas).setLimitValue(templateRuleItem.getLimitValue());
            relationDtoList.add(relationDto);
        }
        return relationDtoList;
    }

    /**
     * getRelationLog
     *
     * @param id recordId
     * @param isAlertLog true or false
     * @param searchAfter String
     * @return LogInfoDTO
     */
    @Override
    public LogInfoDTO getRelationLog(Long id, Boolean isAlertLog, String searchAfter) {
        AlertRecord alertRecord = this.baseMapper.selectById(id);
        if (StrUtil.isBlank(alertRecord.getTemplateRuleType())
            || !alertRecord.getTemplateRuleType().equals(CommonConstants.LOG_RULE)) {
            return new LogInfoDTO();
        }
        Long templateRuleId = alertRecord.getTemplateRuleId();
        List<AlertTemplateRuleItem> templateRuleItems = templateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItem>lambdaQuery().eq(AlertTemplateRuleItem::getTemplateRuleId,
                templateRuleId).eq(AlertTemplateRuleItem::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isEmpty(templateRuleItems)) {
            return new LogInfoDTO();
        }

        LogInfoDTO logInfoDTO = searchEs(alertRecord.getClusterNodeId(),
            getSearchParams(isAlertLog, alertRecord.getStartTime(), alertRecord.getEndTime(), templateRuleItems),
            searchAfter);
        List<Map> keyAndBlockWords = templateRuleItems.stream().map(item -> {
            Map map = new HashMap();
            map.put("keyword", item.getKeyword());
            map.put("blockWord", item.getBlockWord());
            return map;
        }).collect(Collectors.toList());
        logInfoDTO.setKeyAndBlockWords(keyAndBlockWords);
        return logInfoDTO;
    }

    private BoolQuery getSearchParams(
        Boolean isAlertLog, LocalDateTime start, LocalDateTime end,
        List<AlertTemplateRuleItem> templateRuleItems) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        boolQuery.filter(filter -> filter.range(range -> range.field("@timestamp")
            .gte(JsonData.of(start
                .format(DateTimeFormatter.ofPattern(CommonConstants.DATETIME_FORMAT_ISO8601))))
            .lt(JsonData.of(end
                .format(DateTimeFormatter.ofPattern(CommonConstants.DATETIME_FORMAT_ISO8601))))));
        if (isAlertLog == null || !isAlertLog) {
            return boolQuery.build();
        }
        for (AlertTemplateRuleItem ruleItem : templateRuleItems) {
            BoolQuery.Builder boolItemBuilder = new BoolQuery.Builder();
            String keyword = ruleItem.getKeyword();
            for (String word : keyword.split(",")) {
                boolItemBuilder.must(must -> must.matchPhrase(match -> match.field("message").query(word)));
            }
            String blockWord = ruleItem.getBlockWord();
            if (StrUtil.isNotBlank(blockWord)) {
                for (String word : blockWord.split(",")) {
                    boolItemBuilder.mustNot(mustNot ->
                        mustNot.matchPhrase(match -> match.field("message").query(word)));
                }
            }
            boolQuery.should(should -> should.bool(boolItemBuilder.build()));
        }
        boolQuery.minimumShouldMatch("1");
        return boolQuery.build();
    }

    private LogInfoDTO searchEs(String clusterNodeId, BoolQuery boolQuery, String searchAfter) {
        ElasticsearchClient client = clientProvider.client();
        try {
            SearchResponse<HashMap> response = client.search(search -> {
                search.index("ob-*-" + clusterNodeId);
                search.size(30);
                search.query(qry -> qry.bool(boolQuery));
                search.sort(sort -> sort.field(field -> field.field("@timestamp").order(SortOrder.Desc)));
                search.sort(sort -> sort.field(field -> field.field("_index").order(SortOrder.Desc)));
                search.sort(sort -> sort.field(field -> field.field("log.offset").order(SortOrder.Desc)));
                if (StrUtil.isNotBlank(searchAfter)) {
                    search.searchAfter(Arrays.asList(searchAfter.split(",")));
                }
                return search;
            }, HashMap.class);
            List<Hit<HashMap>> hits = response.hits().hits();
            return handleEsResultHits(hits, searchAfter);
        } catch (IOException e) {
            log.error("es client search failed:{}", e);
            throw new ServiceException("es client search failed");
        }
    }

    private LogInfoDTO handleEsResultHits(List<Hit<HashMap>> hits, String searchAfter) {
        List<LogDetailInfoDTO> logDetailInfoDTOList = hits.stream().map(item -> {
            Map docMap = item.source();
            LogDetailInfoDTO logDetailInfoDTO = new LogDetailInfoDTO();
            logDetailInfoDTO.parse(docMap);
            logDetailInfoDTO.setId(item.id());
            return logDetailInfoDTO;
        }).collect(Collectors.toList());
        LogInfoDTO logInfoDTO = new LogInfoDTO();
        logInfoDTO.setLogs(logDetailInfoDTOList);
        if (CollectionUtil.isEmpty(hits) || CollectionUtil.isEmpty(hits.get(hits.size() - 1).sort())) {
            logInfoDTO.setSearchAfter(searchAfter);
        } else {
            logInfoDTO.setSearchAfter(String.join(CommonConstants.DELIMITER, hits.get(hits.size() - 1).sort()));
        }
        return logInfoDTO;
    }

    @Override
    public String markAsUnread(String ids) {
        String[] idArr = ids.split(CommonConstants.DELIMITER);
        LambdaUpdateWrapper<AlertRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(AlertRecord::getId, idArr);
        updateWrapper.set(AlertRecord::getRecordStatus, UNREAD_STATUS).set(AlertRecord::getUpdateTime,
            LocalDateTime.now());
        update(null, updateWrapper);
        return "success";
    }

    /**
     * exportWorkbook
     *
     * @param alertStatisticsReq AlertStatisticsReq
     * @return Workbook
     */
    @Override
    public Workbook exportWorkbook(AlertStatisticsReq alertStatisticsReq) {
        String[] thList = {"nodeName", "alertTemplate", "alertRule", "ruleType", "level", "startTime", "endTime",
            "duration", "alertStatus", "notifyWay", "recordStatus"};
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(MessageSourceUtil.get("alertRecord"));
        Row row = sheet.createRow(0);
        for (int i = 0; i < thList.length; i++) {
            row.createCell(i).setCellValue(MessageSourceUtil.get("alertRecord." + thList[i]));
        }
        List<AlertRecordDto> list = getList(alertStatisticsReq);
        for (int i = 0; i < list.size(); i++) {
            Row row0 = sheet.createRow(i + 1);
            int j = 0;
            row0.createCell(j++).setCellValue(list.get(i).getClusterNodeName());
            row0.createCell(j++).setCellValue(list.get(i).getTemplateName());
            row0.createCell(j++).setCellValue(list.get(i).getTemplateRuleName());
            row0.createCell(j++).setCellValue(
                MessageSourceUtil.get("alertRecord." + list.get(i).getTemplateRuleType()));
            row0.createCell(j++).setCellValue(MessageSourceUtil.get("alertRecord." + list.get(i).getLevel()));
            row0.createCell(j++).setCellValue(list.get(i).getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"
                + " HH:mm:ss")));
            row0.createCell(j++).setCellValue(list.get(i).getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"
                + " HH:mm:ss")));
            Long duration = list.get(i).getDuration();
            long hour = duration / (60 * 60);
            long minute = duration / 60 - hour * 60;
            long second = duration - 3600 * hour - 60 * minute;
            String durationStr = (hour == 0 ? "00" : hour < 10 ? ("0" + hour) : hour + "") + ":"
                + (minute == 0 ? "00" : minute < 10 ? ("0" + minute) : minute + "") + ":"
                + (second == 0 ? "00" : second < 10 ? ("0" + second) : second + "");
            row0.createCell(j++).setCellValue(durationStr);
            Integer alertStatus = list.get(i).getAlertStatus();
            row0.createCell(j++).setCellValue(alertStatus == 0 ? MessageSourceUtil.get("alerting")
                : MessageSourceUtil.get("alerted"));
            row0.createCell(j++).setCellValue(list.get(i).getNotifyWayNames());
            Integer recordStatus = list.get(i).getRecordStatus();
            row0.createCell(j).setCellValue(recordStatus == 0 ? MessageSourceUtil.get("unread")
                : MessageSourceUtil.get("read"));
        }
        return workbook;
    }

    /**
     * exportReport
     *
     * @param alertStatisticsReq AlertStatisticsReq
     * @return html
     */
    @Override
    public String exportReport(AlertStatisticsReq alertStatisticsReq) {
        AlertStatisticsDto alertStatisticsDto = alertRecordStatistics(alertStatisticsReq);
        Context context = new Context();
        context.setVariable("chartData", JSONUtil.parseObj(alertStatisticsDto));
        context.setVariable("recordStatusTip", MessageSourceUtil.get("alertRecord.recordStatusTip"));
        context.setVariable("alertStatusTip", MessageSourceUtil.get("alertRecord.alertStatusTip"));
        context.setVariable("levelTip", MessageSourceUtil.get("alertRecord.levelTip"));
        context.setVariable("alertStatistics", MessageSourceUtil.get("alertRecord.alertStatistics"));
        context.setVariable("alertRecordList", MessageSourceUtil.get("alertRecord.alertRecordList"));
        context.setVariable("serious", MessageSourceUtil.get("alertRecord.serious"));
        context.setVariable("warn", MessageSourceUtil.get("alertRecord.warn"));
        context.setVariable("info", MessageSourceUtil.get("alertRecord.info"));
        context.setVariable("alerting", MessageSourceUtil.get("alerting"));
        context.setVariable("alerted", MessageSourceUtil.get("alerted"));
        context.setVariable("read", MessageSourceUtil.get("read"));
        context.setVariable("unread", MessageSourceUtil.get("unread"));
        context.setVariable("alertRecord", MessageSourceUtil.get("alertRecord"));
        String[] thArr = {"nodeName", "alertTemplate", "alertRule", "ruleType", "level", "startTime", "endTime",
            "duration", "alertStatus", "notifyWay", "recordStatus"};
        List<String> thList = new ArrayList<>();
        for (String th : thArr) {
            thList.add(MessageSourceUtil.get("alertRecord." + th));
        }
        context.setVariable("thList", thList);
        List<AlertRecordDto> list = getList(alertStatisticsReq);
        JSONArray tableData = getExportTableData(list);
        context.setVariable("tableData", tableData);
        return templateEngine.process("alertRecordReport", context);
    }

    /**
     * getExportTableData
     *
     * @param list List<AlertRecordDto>
     * @return JSONArray
     */
    private JSONArray getExportTableData(List<AlertRecordDto> list) {
        JSONArray tableData = new JSONArray();
        for (AlertRecordDto alertRecordDto : list) {
            JSONObject jsonObject = JSONUtil.parseObj(alertRecordDto);
            jsonObject.put("templateRuleTypeName",
                MessageSourceUtil.get("alertRecord." + alertRecordDto.getTemplateRuleType()));
            jsonObject.put("levelName", MessageSourceUtil.get("alertRecord." + alertRecordDto.getLevel()));
            jsonObject.put("startTimeStr", alertRecordDto.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM"
                + "-dd HH:mm:ss")));
            jsonObject.put("endTimeStr", alertRecordDto.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM"
                + "-dd HH:mm:ss")));
            Long duration = alertRecordDto.getDuration();
            long hour = duration / (60 * 60);
            long minute = duration / 60 - hour * 60;
            long second = duration - 3600 * hour - 60 * minute;
            String durationStr = (hour == 0 ? "00" : hour < 10 ? ("0" + hour) : hour + "") + ":"
                + (minute == 0 ? "00" : minute < 10 ? ("0" + minute) : minute + "") + ":"
                + (second == 0 ? "00" : second < 10 ? ("0" + second) : second + "");
            jsonObject.put("durationStr", durationStr);
            Integer alertStatus = alertRecordDto.getAlertStatus();
            jsonObject.put("alertStatusStr", alertStatus == 0 ? MessageSourceUtil.get("alerting")
                : MessageSourceUtil.get("alerted"));
            Integer recordStatus = alertRecordDto.getRecordStatus();
            jsonObject.put("recordStatusStr", recordStatus == 0 ? MessageSourceUtil.get("unread")
                : MessageSourceUtil.get("read"));
            tableData.add(jsonObject);
        }
        return tableData;
    }

    private List<AlertRecordDto> getList(AlertStatisticsReq alertStatisticsReq) {
        LocalDateTime startTime = StrUtil.isNotBlank(alertStatisticsReq.getStartTime()) ? LocalDateTime.parse(
            alertStatisticsReq.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        LocalDateTime endTime = StrUtil.isNotBlank(alertStatisticsReq.getEndTime()) ? LocalDateTime.parse(
            alertStatisticsReq.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        String clusterNodeId = alertStatisticsReq.getClusterNodeId();
        List<AlertRecord> alertRecords = this.baseMapper.selectList(Wrappers.<AlertRecord>lambdaQuery()
            .eq(StrUtil.isNotBlank(clusterNodeId), AlertRecord::getClusterNodeId, clusterNodeId)
            .ge(startTime != null, AlertRecord::getStartTime, startTime)
            .le(endTime != null, AlertRecord::getEndTime, endTime).orderByDesc(AlertRecord::getId));
        if (CollectionUtil.isEmpty(alertRecords)) {
            return new ArrayList<>();
        }
        List<String> clusterNodeIdList = alertRecords.stream().map(item -> item.getClusterNodeId()).collect(
            Collectors.toList());
        List<OpsClusterNodeEntity> opsClusterNodeEntities = clusterNodeService.listByIds(clusterNodeIdList);
        List<String> hostIds = opsClusterNodeEntities.stream().map(item -> item.getHostId()).collect(
            Collectors.toList());
        List<String> clusterIds = opsClusterNodeEntities.stream().map(item -> item.getClusterId()).collect(
            Collectors.toList());
        List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIds);
        List<OpsClusterEntity> opsClusterEntities = clusterService.listByIds(clusterIds);

        List<AlertRecordDto> list = new ArrayList<>();
        for (AlertRecord record : alertRecords) {
            list.add(alertRecordToDto(record, opsClusterNodeEntities, opsHostEntities, opsClusterEntities));
        }
        return list;
    }
}
