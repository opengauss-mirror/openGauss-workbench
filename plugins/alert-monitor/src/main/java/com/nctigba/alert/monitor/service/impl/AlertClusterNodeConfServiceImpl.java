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
 *  AlertClusterNodeConfServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/AlertClusterNodeConfServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.mapper.AlertPluginInfoMapper;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
import com.nctigba.alert.monitor.model.entity.AlertPluginInfoDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.service.AlertScheduleService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.dto.AlertClusterNodeConfDTO;
import com.nctigba.alert.monitor.mapper.AlertClusterNodeConfMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateMapper;
import com.nctigba.alert.monitor.model.query.AlertClusterNodeAndTemplateQuery;
import com.nctigba.alert.monitor.model.query.AlertClusterNodeConfQuery;
import com.nctigba.alert.monitor.model.query.AlertTemplateQuery;
import com.nctigba.alert.monitor.service.AlertClusterNodeConfService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/4/26 16:43
 * @description
 */
@Service
public class AlertClusterNodeConfServiceImpl
    extends ServiceImpl<AlertClusterNodeConfMapper, AlertClusterNodeConfDO>
    implements AlertClusterNodeConfService {
    @Autowired
    private AlertTemplateMapper templateMapper;

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
    private PrometheusServiceImpl prometheusService;

    @Autowired
    private AlertTemplateService templateService;

    @Autowired
    private AlertTemplateRuleService templateRuleService;

    @Autowired
    private AlertScheduleService alertScheduleService;

    @Autowired
    private AlertPluginInfoMapper alertPluginInfoMapper;

    @Transactional
    public void saveClusterNodeConf(AlertClusterNodeConfQuery alertClusterNodeConfQuery) {
        Long templateId = alertClusterNodeConfQuery.getTemplateId();
        String clusterNodeIds = alertClusterNodeConfQuery.getClusterNodeIds();
        String type = alertClusterNodeConfQuery.getType();
        List<String> clusterNodeIdList = Arrays.asList(clusterNodeIds.split(CommonConstants.DELIMITER));
        // save new data
        List<AlertClusterNodeConfDO> oldListByTemplateId =
            this.baseMapper.selectList(Wrappers.<AlertClusterNodeConfDO>lambdaQuery()
                .eq(AlertClusterNodeConfDO::getTemplateId, templateId).eq(AlertClusterNodeConfDO::getType, type)
                .eq(AlertClusterNodeConfDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        List<String> oldNodeIdListByTemplateId = oldListByTemplateId.stream().map(
            item -> item.getClusterNodeId()).collect(Collectors.toList());
        List<String> newNodeIdList = clusterNodeIdList.stream().filter(
            item -> !oldNodeIdListByTemplateId.contains(item)).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(newNodeIdList)) {
            return;
        }
        List<AlertClusterNodeConfDO> alertClusterNodeConfDOList = newNodeIdList.stream().map(
            item -> new AlertClusterNodeConfDO().setClusterNodeId(item).setType(type).setTemplateId(templateId)
                .setCreateTime(LocalDateTime.now()).setIsDeleted(CommonConstants.IS_NOT_DELETE)).collect(
            Collectors.toList());
        this.saveBatch(alertClusterNodeConfDOList);
        if (type.equalsIgnoreCase(CommonConstants.NONINSTANCE)) {
            delOldClusterNodeConfigs(templateId, clusterNodeIdList);
            return;
        }
        Map<Long, String> ruleConfigMap = new HashMap<>();  // use to update the prometheus rule configuration
        oldNodeIdListByTemplateId.addAll(newNodeIdList);
        ruleConfigMap.put(templateId, String.join(CommonConstants.DELIMITER, oldNodeIdListByTemplateId));
        Map<Long, String> updateRuleConfigMap = getUpdateRuleConfigMap(templateId, clusterNodeIdList);
        ruleConfigMap.putAll(updateRuleConfigMap);

        // delete old data by clusterNodeIdList, which exclude templateId
        delOldClusterNodeConfigs(templateId, clusterNodeIdList);
        // update prometheus rule config
        prometheusService.updateRuleConfig(ruleConfigMap);
        // add log task
        List<AlertTemplateRuleDO> templateRuleList = templateRuleService
            .list(Wrappers.<AlertTemplateRuleDO>lambdaQuery().eq(AlertTemplateRuleDO::getTemplateId, templateId)
                .eq(AlertTemplateRuleDO::getRuleType, CommonConstants.LOG_RULE)
                .eq(AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        Set<Long> ruleIdSet = templateRuleList.stream().map(item -> item.getRuleId()).collect(Collectors.toSet());
        alertScheduleService.addTasks(ruleIdSet);
        // remove old log task
        List<Long> oldTemplateIds = updateRuleConfigMap.keySet().stream().filter(
            item -> StrUtil.isBlank(updateRuleConfigMap.get(item))).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(oldTemplateIds)) {
            return;
        }
        removeLogTasks(oldTemplateIds, clusterNodeIdList, ruleIdSet);
    }

    // delete old data by clusterNodeIdList, which exclude templateId
    private void delOldClusterNodeConfigs(Long templateId, List<String> clusterNodeIdList) {
        LambdaUpdateWrapper<AlertClusterNodeConfDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(AlertClusterNodeConfDO::getIsDeleted, CommonConstants.IS_DELETE)
            .set(AlertClusterNodeConfDO::getUpdateTime, new Timestamp(System.currentTimeMillis()))
            .ne(AlertClusterNodeConfDO::getTemplateId, templateId).in(AlertClusterNodeConfDO::getClusterNodeId,
                clusterNodeIdList).eq(AlertClusterNodeConfDO::getIsDeleted, CommonConstants.IS_NOT_DELETE);
        this.update(null, updateWrapper);
    }

    private void removeLogTasks(List<Long> oldTemplateIds, List<String> excludeNodeIds, Set<Long> excludeRuleIds) {
        List<AlertTemplateRuleDO> templateRuleList = templateRuleService.list(
            Wrappers.<AlertTemplateRuleDO>lambdaQuery()
                .in(AlertTemplateRuleDO::getTemplateId, oldTemplateIds)
                .eq(AlertTemplateRuleDO::getRuleType, CommonConstants.LOG_RULE)
                .eq(AlertTemplateRuleDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        Set<Long> ruleIds = templateRuleList.stream().filter(item -> !excludeRuleIds.contains(item.getRuleId()))
            .map(item -> item.getRuleId()).collect(Collectors.toSet());
        if (CollectionUtil.isEmpty(ruleIds)) {
            return;
        }
        List<Long> ruleIdList = baseMapper.getRuleIdExcludeNoIds(
            new QueryWrapper<>().notIn("t1.cluster_node_id", excludeNodeIds)
                .in("t2.rule_id", ruleIds));
        List<Long> removeRuleIds =
            ruleIds.stream().filter(item -> !ruleIdList.contains(item)).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(removeRuleIds)) {
            return;
        }
        alertScheduleService.removeTasks(removeRuleIds);
    }

    private Map<Long, String> getUpdateRuleConfigMap(Long templateId, List<String> clusterNodeIdList) {
        // set the old rule configuration params which should be updated
        List<AlertClusterNodeConfDO> oldListByNodeIds = this.baseMapper.selectList(
            Wrappers.<AlertClusterNodeConfDO>lambdaQuery().ne(AlertClusterNodeConfDO::getTemplateId, templateId).in(
                    AlertClusterNodeConfDO::getClusterNodeId, clusterNodeIdList)
                .eq(AlertClusterNodeConfDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        Map<Long, String> ruleConfigMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(oldListByNodeIds)) {
            Map<Long, List<String>> listMap = oldListByNodeIds.stream().collect(
                Collectors.groupingBy(AlertClusterNodeConfDO::getTemplateId,
                    Collectors.mapping(AlertClusterNodeConfDO::getClusterNodeId, Collectors.toList())));
            listMap.forEach((templateId0, nodeIdList0) -> {
                List<AlertClusterNodeConfDO> alertClusterNodeConfDOS = this.baseMapper.selectList(
                    Wrappers.<AlertClusterNodeConfDO>lambdaQuery().eq(AlertClusterNodeConfDO::getTemplateId,
                            templateId0).notIn(AlertClusterNodeConfDO::getClusterNodeId, nodeIdList0)
                        .eq(AlertClusterNodeConfDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
                if (CollectionUtil.isEmpty(alertClusterNodeConfDOS)) {
                    ruleConfigMap.put(templateId0, "");
                } else {
                    ruleConfigMap.put(templateId0,
                        alertClusterNodeConfDOS.stream().map(item -> item.getClusterNodeId()).collect(
                            Collectors.joining(CommonConstants.DELIMITER)));
                }
            });
        }
        return ruleConfigMap;
    }

    @Override
    public AlertClusterNodeConfDO getByClusterNodeId(String clusterNodeId, String type) {
        List<AlertClusterNodeConfDO> alertClusterNodeConfDOS = this.baseMapper.selectList(
            Wrappers.<AlertClusterNodeConfDO>lambdaQuery().eq(AlertClusterNodeConfDO::getClusterNodeId, clusterNodeId)
                .eq(AlertClusterNodeConfDO::getType, type)
                .eq(AlertClusterNodeConfDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isEmpty(alertClusterNodeConfDOS)) {
            return new AlertClusterNodeConfDO();
        }
        return alertClusterNodeConfDOS.get(0);
    }

    @Override
    @Transactional
    public void saveAlertTemplateAndConfig(AlertClusterNodeAndTemplateQuery clusterNodeAndTemplateReq) {
        AlertTemplateQuery templateReq = new AlertTemplateQuery();
        BeanUtil.copyProperties(clusterNodeAndTemplateReq, templateReq);
        AlertTemplateDO alertTemplateDO = templateService.saveTemplate(templateReq);
        AlertClusterNodeConfQuery alertClusterNodeConfQuery = new AlertClusterNodeConfQuery();
        alertClusterNodeConfQuery.setTemplateId(alertTemplateDO.getId());
        alertClusterNodeConfQuery.setClusterNodeIds(clusterNodeAndTemplateReq.getClusterNodeIds());
        alertClusterNodeConfQuery.setType(clusterNodeAndTemplateReq.getType());
        this.saveClusterNodeConf(alertClusterNodeConfQuery);
    }

    @Override
    public List<AlertClusterNodeConfDTO> getList(String type) {
        List<AlertClusterNodeConfDTO> dtoList = new ArrayList<>();
        if (type.equalsIgnoreCase(CommonConstants.NONINSTANCE)) {
            List<AlertPluginInfoDO> pluginInfoList = alertPluginInfoMapper.selectList(new LambdaUpdateWrapper<>());
            if (CollectionUtil.isEmpty(pluginInfoList)) {
                return dtoList;
            }
            List<String> nodeIds =
                pluginInfoList.stream().map(item -> item.getId().toString()).collect(Collectors.toList());
            List<AlertClusterNodeConfDO> alertClusterNodeConfs = getNodeConfByNodeIds(nodeIds, type);
            for (AlertPluginInfoDO pluginInfo : pluginInfoList) {
                AlertClusterNodeConfDTO dto = new AlertClusterNodeConfDTO();
                dto.setClusterNodeId(pluginInfo.getId().toString()).setType(type).setNodeName(pluginInfo.getName());
                AlertClusterNodeConfDO nodeConf = alertClusterNodeConfs.stream().filter(
                    item -> item.getClusterNodeId().equals(pluginInfo.getId().toString())).findFirst().orElse(null);
                if (nodeConf != null) {
                    AlertTemplateDO alertTemplate = templateMapper.selectById(nodeConf.getTemplateId());
                    dto.setTemplateId(nodeConf.getTemplateId()).setTemplateName(alertTemplate.getTemplateName());
                }
                dtoList.add(dto);
            }
            return dtoList;
        }
        List<OpsClusterNodeEntity> list = clusterNodeService.list();
        Set<String> hostIdSet = list.stream().map(item -> item.getHostId()).collect(Collectors.toSet());
        List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIdSet);
        List<String> clusterIdList = list.stream().map(item -> item.getClusterId()).collect(Collectors.toList());
        List<OpsClusterEntity> opsClusterEntities = clusterService.listByIds(clusterIdList);
        List<String> nodeIdList = list.stream().map(item -> item.getClusterNodeId()).collect(Collectors.toList());
        List<AlertClusterNodeConfDO> alertClusterNodeConfDOS = getNodeConfByNodeIds(nodeIdList, type);
        for (OpsClusterNodeEntity clusterNode : list) {
            OpsHostEntity opsHostEntity =
                opsHostEntities.stream().filter(
                    item -> item.getHostId().equals(clusterNode.getHostId())).findFirst().get();
            OpsClusterEntity opsClusterEntity =
                opsClusterEntities.stream().filter(
                    item -> item.getClusterId().equals(clusterNode.getClusterId())).findFirst().get();
            String nodeName = opsClusterEntity.getClusterId() + "/" + opsHostEntity.getPublicIp() + ":"
                + opsClusterEntity.getPort() + "(" + clusterNode.getClusterRole() + ")";
            AlertClusterNodeConfDTO clusterNodeConfDto = new AlertClusterNodeConfDTO();
            clusterNodeConfDto.setClusterNodeId(clusterNode.getClusterNodeId()).setNodeName(nodeName);
            List<AlertClusterNodeConfDO> clusterNodeConfs = alertClusterNodeConfDOS.stream().filter(
                item -> item.getClusterNodeId().equals(clusterNode.getClusterNodeId())).collect(
                Collectors.toList());
            if (CollectionUtil.isNotEmpty(clusterNodeConfs)) {
                AlertClusterNodeConfDO alertClusterNodeConfDO = clusterNodeConfs.get(0);
                AlertTemplateDO alertTemplateDO = templateMapper.selectById(alertClusterNodeConfDO.getTemplateId());
                clusterNodeConfDto.setTemplateId(alertClusterNodeConfDO.getTemplateId()).setTemplateName(
                    alertTemplateDO.getTemplateName());
            }
            dtoList.add(clusterNodeConfDto);
        }
        return dtoList;
    }

    private List<AlertClusterNodeConfDO> getNodeConfByNodeIds(List<String> nodeIds, String type) {
        return this.baseMapper.selectList(Wrappers.<AlertClusterNodeConfDO>lambdaQuery()
            .in(CollectionUtil.isNotEmpty(nodeIds), AlertClusterNodeConfDO::getClusterNodeId, nodeIds)
            .eq(AlertClusterNodeConfDO::getType, type)
            .eq(AlertClusterNodeConfDO::getIsDeleted, CommonConstants.IS_NOT_DELETE));
    }

    @Override
    @Transactional
    public void unbindByIds(String clusterNodeIds, String type) {
        List<String> nodeIdList = Arrays.asList(clusterNodeIds.split(","));
        List<AlertClusterNodeConfDO> alertClusterNodeConfDOS =
            this.list(Wrappers.<AlertClusterNodeConfDO>lambdaQuery().in(AlertClusterNodeConfDO::getClusterNodeId,
                nodeIdList).eq(AlertClusterNodeConfDO::getType, type).eq(AlertClusterNodeConfDO::getIsDeleted,
                CommonConstants.IS_NOT_DELETE));
        alertClusterNodeConfDOS.forEach(item -> item.setIsDeleted(CommonConstants.IS_DELETE)
            .setUpdateTime(LocalDateTime.now()));
        this.updateBatchById(alertClusterNodeConfDOS);
        if (type.equalsIgnoreCase(CommonConstants.NONINSTANCE)) {
            return;
        }

        Map<Long, String> ruleConfigMap = new HashMap<>();
        List<Long> removeRuleIds = new ArrayList<>();
        Set<Long> templateIds = alertClusterNodeConfDOS.stream().map(item -> item.getTemplateId()).collect(
            Collectors.toSet());
        for (Long templateId : templateIds) {
            Set<String> nodeIdSet = alertClusterNodeConfDOS.stream()
                .filter(item -> item.getTemplateId().equals(templateId))
                .map(item -> item.getClusterNodeId()).collect(Collectors.toSet());
            String nodeIds = this.baseMapper.selectList(Wrappers.<AlertClusterNodeConfDO>lambdaQuery()
                    .eq(AlertClusterNodeConfDO::getTemplateId, templateId)
                    .eq(AlertClusterNodeConfDO::getIsDeleted, CommonConstants.IS_NOT_DELETE)
                    .notIn(AlertClusterNodeConfDO::getClusterNodeId, nodeIdSet))
                .stream().map(item -> item.getClusterNodeId()).collect(Collectors.joining(CommonConstants.DELIMITER));
            ruleConfigMap.put(templateId, nodeIds);
            if (StrUtil.isNotBlank(nodeIds)) {
                continue;
            }
            List<AlertTemplateRuleDO> templateRules = templateRuleService.list(
                Wrappers.<AlertTemplateRuleDO>lambdaQuery().eq(AlertTemplateRuleDO::getIsDeleted,
                        CommonConstants.IS_NOT_DELETE).eq(AlertTemplateRuleDO::getTemplateId, templateId)
                    .eq(AlertTemplateRuleDO::getRuleType, CommonConstants.LOG_RULE));
            if (CollectionUtil.isEmpty(templateRules)) {
                continue;
            }
            Set<Long> ruleIds = templateRules.stream().map(item -> item.getRuleId()).collect(Collectors.toSet());
            for (Long ruleId : ruleIds) {
                long count = templateRuleService.count(Wrappers.<AlertTemplateRuleDO>lambdaQuery()
                    .eq(AlertTemplateRuleDO::getRuleId, ruleId).eq(AlertTemplateRuleDO::getIsDeleted,
                        CommonConstants.IS_NOT_DELETE).ne(AlertTemplateRuleDO::getTemplateId, templateId));
                if (count == 0) {
                    removeRuleIds.add(ruleId);
                }
            }
        }
        prometheusService.updateRuleConfig(ruleConfigMap);
        alertScheduleService.removeTasks(removeRuleIds);
    }
}