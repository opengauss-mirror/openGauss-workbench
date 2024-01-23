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
 *  AlertRecordServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/AlertRecordServiceImpl.java
 *
 *  -------------------------------------------------------------------------
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
import com.nctigba.alert.monitor.config.ElasticsearchProviderConfig;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.mapper.AlertRuleItemSrcMapper;
import com.nctigba.alert.monitor.model.dto.AlertRecordDTO;
import com.nctigba.alert.monitor.model.dto.AlertRelationDTO;
import com.nctigba.alert.monitor.model.dto.AlertStatisticsDTO;
import com.nctigba.alert.monitor.model.dto.LogDetailInfoDTO;
import com.nctigba.alert.monitor.model.dto.LogInfoDTO;
import com.nctigba.alert.monitor.model.entity.AlertRecordDO;
import com.nctigba.alert.monitor.model.entity.AlertRuleItemSrcDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleItemDO;
import com.nctigba.alert.monitor.model.entity.NctigbaEnvDO;
import com.nctigba.alert.monitor.mapper.AlertRecordMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import com.nctigba.alert.monitor.model.query.AlertRecordQuery;
import com.nctigba.alert.monitor.model.query.AlertStatisticsQuery;
import com.nctigba.alert.monitor.service.AlertAnalysisService;
import com.nctigba.alert.monitor.service.AlertRecordService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
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
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nctigba.alert.monitor.constant.CommonConstants.FIRING_STATUS;
import static com.nctigba.alert.monitor.constant.CommonConstants.READ_STATUS;
import static com.nctigba.alert.monitor.constant.CommonConstants.UNREAD_STATUS;

/**
 * AlertRecordServiceImpl
 *
 * @author wuyuebin
 * @since 2023/5/17 10:35
 */
@Service
public class AlertRecordServiceImpl extends ServiceImpl<AlertRecordMapper, AlertRecordDO>
    implements AlertRecordService {
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
    private PrometheusServiceImpl prometheusService;

    @Autowired
    private NctigbaEnvMapper envMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private ElasticsearchProviderConfig clientProvider;

    @Autowired
    private AlertRuleItemSrcMapper ruleItemSrcMapper;
    @Autowired
    private Map<String, AlertAnalysisService> analysisServiceMap;

    @Override
    public Page<AlertRecordDTO> getListPage(AlertRecordQuery alertRecordQuery, Page page) {
        LocalDateTime startTime = StrUtil.isNotBlank(alertRecordQuery.getStartTime()) ? LocalDateTime.parse(
            alertRecordQuery.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        LocalDateTime endTime = StrUtil.isNotBlank(alertRecordQuery.getEndTime()) ? LocalDateTime.parse(
            alertRecordQuery.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        String clusterNodeId = alertRecordQuery.getClusterNodeId();
        Page<AlertRecordDO> recordPage = this.baseMapper.selectPage(page, Wrappers.<AlertRecordDO>lambdaQuery()
            .eq(StrUtil.isNotBlank(clusterNodeId), AlertRecordDO::getClusterNodeId, clusterNodeId)
            .in(StrUtil.isNotBlank(alertRecordQuery.getAlertStatus()), AlertRecordDO::getAlertStatus,
                StrUtil.isNotBlank(alertRecordQuery.getAlertStatus())
                    ? alertRecordQuery.getAlertStatus().split(CommonConstants.DELIMITER) : new String[]{})
            .in(StrUtil.isNotBlank(alertRecordQuery.getRecordStatus()), AlertRecordDO::getRecordStatus,
                StrUtil.isNotBlank(alertRecordQuery.getRecordStatus())
                    ? alertRecordQuery.getRecordStatus().split(CommonConstants.DELIMITER) : new String[]{})
            .in(StrUtil.isNotBlank(alertRecordQuery.getAlertLevel()), AlertRecordDO::getLevel,
                StrUtil.isNotBlank(alertRecordQuery.getAlertLevel())
                    ? alertRecordQuery.getAlertLevel().split(CommonConstants.DELIMITER) : new String[]{})
            .ge(startTime != null, AlertRecordDO::getStartTime, startTime)
            .le(endTime != null, AlertRecordDO::getEndTime, endTime).orderByDesc(AlertRecordDO::getId));
        Page<AlertRecordDTO> recordDtoPage = new Page<>();
        List<AlertRecordDO> records = recordPage.getRecords();
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

        List<AlertRecordDTO> list = new ArrayList<>();
        for (AlertRecordDO record : records) {
            list.add(alertRecordToDto(record, opsClusterNodeEntities, opsHostEntities, opsClusterEntities));
        }
        recordDtoPage.setRecords(list);
        return recordDtoPage;
    }

    private AlertRecordDTO alertRecordToDto(
        AlertRecordDO record, List<OpsClusterNodeEntity> opsClusterNodeEntities,
        List<OpsHostEntity> opsHostEntities, List<OpsClusterEntity> opsClusterEntities) {
        AlertRecordDTO alertRecordDto = new AlertRecordDTO();
        BeanUtil.copyProperties(record, alertRecordDto);
        String clusterNodeId0 = record.getClusterNodeId();
        OpsClusterNodeEntity opsClusterNodeEntity = opsClusterNodeEntities.stream().filter(
            item -> item.getClusterNodeId().equals(clusterNodeId0)).findFirst().orElse(null);
        if (opsClusterNodeEntity == null) {
            alertRecordDto.setClusterNodeName("").setHostIpAndPort("").setNodeRole("");
            return alertRecordDto;
        }
        OpsHostEntity opsHostEntity = opsHostEntities.stream().filter(
            item -> item.getHostId().equals(opsClusterNodeEntity.getHostId())).findFirst().get();
        OpsClusterEntity opsClusterEntity = opsClusterEntities.stream().filter(
            item -> item.getClusterId().equals(opsClusterNodeEntity.getClusterId())).findFirst().get();
        String nodeName = opsClusterEntity.getClusterId() + "/" + opsHostEntity.getPublicIp() + ":"
            + opsClusterEntity.getPort() + "(" + opsClusterNodeEntity.getClusterRole() + ")";
        alertRecordDto.setClusterNodeName(nodeName).setHostIpAndPort(
            opsHostEntity.getPublicIp() + ":" + opsClusterEntity.getPort()).setNodeRole(
            opsClusterNodeEntity.getClusterRole().name());
        return alertRecordDto;
    }

    @Override
    public AlertStatisticsDTO alertRecordStatistics(AlertStatisticsQuery alertStatisticsQuery) {
        LocalDateTime startTime = StrUtil.isNotBlank(alertStatisticsQuery.getStartTime()) ? LocalDateTime.parse(
            alertStatisticsQuery.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        LocalDateTime endTime = StrUtil.isNotBlank(alertStatisticsQuery.getEndTime()) ? LocalDateTime.parse(
            alertStatisticsQuery.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        Long total = this.baseMapper.selectCount(
            Wrappers.<AlertRecordDO>lambdaQuery().eq(StrUtil.isNotBlank(alertStatisticsQuery.getClusterNodeId()),
                    AlertRecordDO::getClusterNodeId, alertStatisticsQuery.getClusterNodeId())
                .ge(startTime != null, AlertRecordDO::getStartTime, startTime)
                .le(endTime != null, AlertRecordDO::getEndTime, endTime));

        AlertStatisticsDTO alertStatisticsDto = new AlertStatisticsDTO();
        alertStatisticsDto.setTotalNum(total.intValue());
        AlertStatisticsDTO statusCountStatistics = setAndGetAlertStatusCount(alertStatisticsQuery.getClusterNodeId(),
            startTime, endTime);
        alertStatisticsDto.setFiringNum(statusCountStatistics.getFiringNum()).setRecoverNum(
            statusCountStatistics.getRecoverNum());

        AlertStatisticsDTO recordStatusCountStatistics =
            setAndGetRecordStatusCount(alertStatisticsQuery.getClusterNodeId(), startTime, endTime);
        alertStatisticsDto.setUnReadNum(recordStatusCountStatistics.getUnReadNum()).setReadNum(
            recordStatusCountStatistics.getReadNum());

        AlertStatisticsDTO levelCountStatistics = setAndGetLevelCount(alertStatisticsQuery.getClusterNodeId(),
            startTime, endTime);
        alertStatisticsDto.setSeriousNum(levelCountStatistics.getSeriousNum()).setWarnNum(
            levelCountStatistics.getWarnNum()).setInfoNum(levelCountStatistics.getInfoNum());

        return alertStatisticsDto;
    }

    private AlertStatisticsDTO setAndGetAlertStatusCount(
        String clusterNodeId, LocalDateTime startTime,
        LocalDateTime endTime) {
        QueryWrapper<AlertRecordDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("alert_status as alertStatus,count(1) count").eq(
                StrUtil.isNotBlank(clusterNodeId), "cluster_node_id", clusterNodeId)
            .ge(startTime != null, "start_time", startTime)
            .le(endTime != null, "end_time", endTime).groupBy("alert_status");
        List<Map<String, Object>> alertStatusMaps = this.listMaps(queryWrapper);
        AlertStatisticsDTO alertStatisticsDto = new AlertStatisticsDTO();
        for (Map<String, Object> alertStatusMap : alertStatusMaps) {
            if (alertStatusMap.get("alertstatus") == null) {
                continue;
            }
            int count = 0;
            if (alertStatusMap.get("count") instanceof Number) {
                count = ((Number) alertStatusMap.get("count")).intValue();
            }
            if (alertStatusMap.get("alertstatus").equals(FIRING_STATUS)) {
                alertStatisticsDto.setFiringNum(count);
            } else {
                alertStatisticsDto.setRecoverNum(count);
            }
        }
        return alertStatisticsDto;
    }

    private AlertStatisticsDTO setAndGetRecordStatusCount(
        String clusterNodeId, LocalDateTime startTime,
        LocalDateTime endTime) {
        QueryWrapper<AlertRecordDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("record_status as recordStatus,count(1) count").eq(
                StrUtil.isNotBlank(clusterNodeId), "cluster_node_id",
                clusterNodeId)
            .ge(startTime != null, "start_time", startTime)
            .le(endTime != null, "end_time", endTime).groupBy("record_status");
        List<Map<String, Object>> recordStatusMaps = this.listMaps(queryWrapper);
        AlertStatisticsDTO alertStatisticsDto = new AlertStatisticsDTO();
        for (Map<String, Object> recordStatusMap : recordStatusMaps) {
            if (recordStatusMap.get("recordstatus") == null) {
                continue;
            }
            int count = 0;
            if (recordStatusMap.get("count") instanceof Number) {
                count = ((Number) recordStatusMap.get("count")).intValue();
            }
            if (recordStatusMap.get("recordstatus").equals(UNREAD_STATUS)) {
                alertStatisticsDto.setUnReadNum(count);
            } else {
                alertStatisticsDto.setReadNum(count);
            }
        }
        return alertStatisticsDto;
    }

    private AlertStatisticsDTO setAndGetLevelCount(
        String clusterNodeId, LocalDateTime startTime,
        LocalDateTime endTime) {
        QueryWrapper<AlertRecordDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("level ,count(1) count").eq(StrUtil.isNotBlank(clusterNodeId),
                "cluster_node_id", clusterNodeId)
            .ge(startTime != null, "start_time", startTime)
            .le(endTime != null, "end_time", endTime).groupBy("level");
        List<Map<String, Object>> levelMaps = this.listMaps(queryWrapper);
        AlertStatisticsDTO alertStatisticsDto = new AlertStatisticsDTO();
        for (Map<String, Object> levelMap : levelMaps) {
            if (levelMap.get("level") == null) {
                continue;
            }
            int count = 0;
            if (levelMap.get("count") instanceof Number) {
                count = ((Number) levelMap.get("count")).intValue();
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
        LambdaUpdateWrapper<AlertRecordDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(AlertRecordDO::getId, idArr);
        updateWrapper.set(AlertRecordDO::getRecordStatus, READ_STATUS).set(AlertRecordDO::getUpdateTime,
            LocalDateTime.now());
        update(null, updateWrapper);
        return "success";
    }

    @Override
    public AlertRecordDTO getById(Long id) {
        AlertRecordDTO alertRecordDto = new AlertRecordDTO();
        AlertRecordDO alertRecordDO = this.baseMapper.selectById(id);
        if (alertRecordDO == null) {
            return alertRecordDto;
        }
        BeanUtil.copyProperties(alertRecordDO, alertRecordDto);
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
            opsHostEntity.getPublicIp() + ":" + opsClusterEntity.getPort())
            .setNodeRole(opsClusterNodeEntity.getClusterRole() != null
            ? opsClusterNodeEntity.getClusterRole().name() : "");
        return alertRecordDto;
    }

    @Override
    public List<AlertRelationDTO> getRelationData(Long id) {
        AlertRecordDO alertRecordDO = this.baseMapper.selectById(id);
        if (StrUtil.isBlank(alertRecordDO.getTemplateRuleType())
            || !alertRecordDO.getTemplateRuleType().equals(CommonConstants.INDEX_RULE)) {
            return new ArrayList<>();
        }
        Long templateRuleId = alertRecordDO.getTemplateRuleId();
        List<AlertTemplateRuleItemDO> templateRuleItems = templateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItemDO>lambdaQuery().eq(AlertTemplateRuleItemDO::getTemplateRuleId,
                templateRuleId));
        List<AlertRelationDTO> relationDtoList = new ArrayList<>();
        NctigbaEnvDO promEnv = envMapper
            .selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, NctigbaEnvDO.Type.PROMETHEUS));
        OpsHostEntity opsHostEntity = hostFacade.getById(promEnv.getHostid());
        for (AlertTemplateRuleItemDO templateRuleItem : templateRuleItems) {
            String ruleExpName = templateRuleItem.getRuleExpName();
            AlertRuleItemSrcDO ruleItemSrc = ruleItemSrcMapper.selectList(
                Wrappers.<AlertRuleItemSrcDO>lambdaQuery().eq(AlertRuleItemSrcDO::getName,
                    ruleExpName)).stream().findFirst().orElse(null);
            if (ruleItemSrc == null) {
                throw new ServiceException("not exist the " + ruleExpName);
            }
            if (analysisServiceMap.get(ruleItemSrc.getAnalysisBeanName()) != null) {
                AlertAnalysisService analysisService = analysisServiceMap.get(ruleItemSrc.getAnalysisBeanName());
                relationDtoList.addAll(analysisService.getRelationData(alertRecordDO.getClusterNodeId(),
                    templateRuleItem.getLimitValue()));
                continue;
            }
            AlertRelationDTO relationDto = new AlertRelationDTO();
            String name = MessageSourceUtils.getLocale().equals(Locale.CHINA) ? ruleItemSrc.getNameZh()
                : ruleItemSrc.getNameEn();
            relationDto.setName(StrUtil.isNotBlank(name) ? name
                    : MessageSourceUtils.get(templateRuleItem.getRuleExpName())).setUnit(templateRuleItem.getUnit())
                .setStartTime(alertRecordDO.getStartTime()).setEndTime(alertRecordDO.getEndTime());
            LocalDateTime minTime = alertRecordDO.getStartTime().minusMinutes(30L);
            LocalDateTime maxTime = alertRecordDO.getEndTime().plusMinutes(30L);
            relationDto.setMinTime(minTime).setMaxTime(maxTime);
            String ruleExp = templateRuleItem.getRuleExp();
            ruleExp = ruleExp.replace("${instances}", alertRecordDO.getClusterNodeId());
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
        AlertRecordDO alertRecordDO = this.baseMapper.selectById(id);
        if (StrUtil.isBlank(alertRecordDO.getTemplateRuleType())
            || !alertRecordDO.getTemplateRuleType().equals(CommonConstants.LOG_RULE)) {
            return new LogInfoDTO();
        }
        Long templateRuleId = alertRecordDO.getTemplateRuleId();
        List<AlertTemplateRuleItemDO> templateRuleItems = templateRuleItemMapper.selectList(
            Wrappers.<AlertTemplateRuleItemDO>lambdaQuery().eq(AlertTemplateRuleItemDO::getTemplateRuleId,
                templateRuleId).eq(AlertTemplateRuleItemDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isEmpty(templateRuleItems)) {
            return new LogInfoDTO();
        }

        LogInfoDTO logInfoDTO = searchEs(alertRecordDO.getClusterNodeId(),
            getSearchParams(isAlertLog, alertRecordDO.getStartTime(), alertRecordDO.getEndTime(), templateRuleItems),
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

    @Override
    public List<AlertRecordDO> getList(
        String clusterNodeId, Long templateId, Long templateRuleId, LocalDateTime startTime) {
        return this.list(Wrappers.<AlertRecordDO>lambdaQuery()
            .eq(AlertRecordDO::getClusterNodeId, clusterNodeId).eq(AlertRecordDO::getTemplateId, templateId)
            .eq(AlertRecordDO::getTemplateRuleId, templateRuleId).eq(AlertRecordDO::getStartTime,
                startTime).orderByDesc(AlertRecordDO::getUpdateTime));
    }

    private BoolQuery getSearchParams(
        Boolean isAlertLog, LocalDateTime start, LocalDateTime end,
        List<AlertTemplateRuleItemDO> templateRuleItems) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        boolQuery.filter(filter -> filter.range(range -> range.field("@timestamp")
            .gte(JsonData.of(start
                .format(DateTimeFormatter.ofPattern(CommonConstants.DATETIME_FORMAT_ISO8601))))
            .lt(JsonData.of(end
                .format(DateTimeFormatter.ofPattern(CommonConstants.DATETIME_FORMAT_ISO8601))))));
        if (isAlertLog == null || !isAlertLog) {
            return boolQuery.build();
        }
        for (AlertTemplateRuleItemDO ruleItem : templateRuleItems) {
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
        LambdaUpdateWrapper<AlertRecordDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(AlertRecordDO::getId, idArr);
        updateWrapper.set(AlertRecordDO::getRecordStatus, UNREAD_STATUS).set(AlertRecordDO::getUpdateTime,
            LocalDateTime.now());
        update(null, updateWrapper);
        return "success";
    }

    /**
     * exportWorkbook
     *
     * @param alertStatisticsQuery AlertStatisticsReq
     * @return Workbook
     */
    @Override
    public Workbook exportWorkbook(AlertStatisticsQuery alertStatisticsQuery) {
        String[] thList = {"nodeName", "alertTemplate", "alertRule", "ruleType", "level", "startTime", "endTime",
            "duration", "alertStatus", "notifyWay", "recordStatus"};
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(MessageSourceUtils.get("alertRecord"));
        Row row = sheet.createRow(0);
        for (int i = 0; i < thList.length; i++) {
            row.createCell(i).setCellValue(MessageSourceUtils.get("alertRecord." + thList[i]));
        }
        List<AlertRecordDTO> list = getList(alertStatisticsQuery);
        for (int i = 0; i < list.size(); i++) {
            Row row0 = sheet.createRow(i + 1);
            int j = 0;
            row0.createCell(j++).setCellValue(list.get(i).getClusterNodeName());
            row0.createCell(j++).setCellValue(list.get(i).getTemplateName());
            row0.createCell(j++).setCellValue(list.get(i).getTemplateRuleName());
            row0.createCell(j++).setCellValue(
                MessageSourceUtils.get("alertRecord." + list.get(i).getTemplateRuleType()));
            row0.createCell(j++).setCellValue(MessageSourceUtils.get("alertRecord." + list.get(i).getLevel()));
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
            row0.createCell(j++).setCellValue(alertStatus == 0 ? MessageSourceUtils.get("alerting")
                : MessageSourceUtils.get("alerted"));
            row0.createCell(j++).setCellValue(list.get(i).getNotifyWayNames());
            Integer recordStatus = list.get(i).getRecordStatus();
            row0.createCell(j).setCellValue(recordStatus == 0 ? MessageSourceUtils.get("unread")
                : MessageSourceUtils.get("read"));
        }
        return workbook;
    }

    /**
     * exportReport
     *
     * @param alertStatisticsQuery AlertStatisticsReq
     * @return html
     */
    @Override
    public String exportReport(AlertStatisticsQuery alertStatisticsQuery) {
        AlertStatisticsDTO alertStatisticsDto = alertRecordStatistics(alertStatisticsQuery);
        Context context = new Context();
        context.setVariable("chartData", JSONUtil.parseObj(alertStatisticsDto));
        context.setVariable("recordStatusTip", MessageSourceUtils.get("alertRecord.recordStatusTip"));
        context.setVariable("alertStatusTip", MessageSourceUtils.get("alertRecord.alertStatusTip"));
        context.setVariable("levelTip", MessageSourceUtils.get("alertRecord.levelTip"));
        context.setVariable("alertStatistics", MessageSourceUtils.get("alertRecord.alertStatistics"));
        context.setVariable("alertRecordList", MessageSourceUtils.get("alertRecord.alertRecordList"));
        context.setVariable("serious", MessageSourceUtils.get("alertRecord.serious"));
        context.setVariable("warn", MessageSourceUtils.get("alertRecord.warn"));
        context.setVariable("info", MessageSourceUtils.get("alertRecord.info"));
        context.setVariable("alerting", MessageSourceUtils.get("alerting"));
        context.setVariable("alerted", MessageSourceUtils.get("alerted"));
        context.setVariable("read", MessageSourceUtils.get("read"));
        context.setVariable("unread", MessageSourceUtils.get("unread"));
        context.setVariable("alertRecord", MessageSourceUtils.get("alertRecord"));
        String[] thArr = {"nodeName", "alertTemplate", "alertRule", "ruleType", "level", "startTime", "endTime",
            "duration", "alertStatus", "notifyWay", "recordStatus"};
        List<String> thList = new ArrayList<>();
        for (String th : thArr) {
            thList.add(MessageSourceUtils.get("alertRecord." + th));
        }
        context.setVariable("thList", thList);
        List<AlertRecordDTO> list = getList(alertStatisticsQuery);
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
    private JSONArray getExportTableData(List<AlertRecordDTO> list) {
        JSONArray tableData = new JSONArray();
        for (AlertRecordDTO alertRecordDto : list) {
            JSONObject jsonObject = JSONUtil.parseObj(alertRecordDto);
            jsonObject.put("templateRuleTypeName",
                MessageSourceUtils.get("alertRecord." + alertRecordDto.getTemplateRuleType()));
            jsonObject.put("levelName", MessageSourceUtils.get("alertRecord." + alertRecordDto.getLevel()));
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
            jsonObject.put("alertStatusStr", alertStatus == 0 ? MessageSourceUtils.get("alerting")
                : MessageSourceUtils.get("alerted"));
            Integer recordStatus = alertRecordDto.getRecordStatus();
            jsonObject.put("recordStatusStr", recordStatus == 0 ? MessageSourceUtils.get("unread")
                : MessageSourceUtils.get("read"));
            tableData.add(jsonObject);
        }
        return tableData;
    }

    private List<AlertRecordDTO> getList(AlertStatisticsQuery alertStatisticsQuery) {
        LocalDateTime startTime = StrUtil.isNotBlank(alertStatisticsQuery.getStartTime()) ? LocalDateTime.parse(
            alertStatisticsQuery.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        LocalDateTime endTime = StrUtil.isNotBlank(alertStatisticsQuery.getEndTime()) ? LocalDateTime.parse(
            alertStatisticsQuery.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        String clusterNodeId = alertStatisticsQuery.getClusterNodeId();
        List<AlertRecordDO> alertRecordDOS = this.baseMapper.selectList(Wrappers.<AlertRecordDO>lambdaQuery()
            .eq(StrUtil.isNotBlank(clusterNodeId), AlertRecordDO::getClusterNodeId, clusterNodeId)
            .ge(startTime != null, AlertRecordDO::getStartTime, startTime)
            .le(endTime != null, AlertRecordDO::getEndTime, endTime).orderByDesc(AlertRecordDO::getId));
        if (CollectionUtil.isEmpty(alertRecordDOS)) {
            return new ArrayList<>();
        }
        List<String> clusterNodeIdList = alertRecordDOS.stream().map(item -> item.getClusterNodeId()).collect(
            Collectors.toList());
        List<OpsClusterNodeEntity> opsClusterNodeEntities = clusterNodeService.listByIds(clusterNodeIdList);
        List<String> hostIds = opsClusterNodeEntities.stream().map(item -> item.getHostId()).collect(
            Collectors.toList());
        List<String> clusterIds = opsClusterNodeEntities.stream().map(item -> item.getClusterId()).collect(
            Collectors.toList());
        List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIds);
        List<OpsClusterEntity> opsClusterEntities = clusterService.listByIds(clusterIds);

        List<AlertRecordDTO> list = new ArrayList<>();
        for (AlertRecordDO record : alertRecordDOS) {
            list.add(alertRecordToDto(record, opsClusterNodeEntities, opsHostEntities, opsClusterEntities));
        }
        return list;
    }
}
