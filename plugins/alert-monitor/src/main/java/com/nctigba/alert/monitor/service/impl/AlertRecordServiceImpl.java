/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.AlertRecordDto;
import com.nctigba.alert.monitor.dto.AlertRelationDto;
import com.nctigba.alert.monitor.dto.AlertStatisticsDto;
import com.nctigba.alert.monitor.entity.AlertRecord;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
import com.nctigba.alert.monitor.entity.NctigbaEnv;
import com.nctigba.alert.monitor.mapper.AlertRecordMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import com.nctigba.alert.monitor.model.AlertRecordReq;
import com.nctigba.alert.monitor.model.AlertStatisticsReq;
import com.nctigba.alert.monitor.service.AlertRecordService;
import com.nctigba.alert.monitor.service.PrometheusService;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private AlertTemplateRuleItemMapper templateRuleItemMapper;

    @Autowired
    private PrometheusService prometheusService;

    @Autowired
    private NctigbaEnvMapper envMapper;

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
}
