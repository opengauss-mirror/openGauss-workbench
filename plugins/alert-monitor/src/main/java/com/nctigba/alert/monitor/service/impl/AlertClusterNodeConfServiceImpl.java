/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.AlertClusterNodeConfDto;
import com.nctigba.alert.monitor.entity.AlertClusterNodeConf;
import com.nctigba.alert.monitor.entity.AlertTemplate;
import com.nctigba.alert.monitor.mapper.AlertClusterNodeConfMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateMapper;
import com.nctigba.alert.monitor.model.AlertClusterNodeAndTemplateReq;
import com.nctigba.alert.monitor.model.AlertClusterNodeConfReq;
import com.nctigba.alert.monitor.model.AlertTemplateReq;
import com.nctigba.alert.monitor.service.AlertClusterNodeConfService;
import com.nctigba.alert.monitor.service.AlertTemplateService;
import com.nctigba.alert.monitor.service.PrometheusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
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
    extends ServiceImpl<AlertClusterNodeConfMapper, AlertClusterNodeConf>
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
    private PrometheusService prometheusService;

    @Autowired
    private AlertTemplateService templateService;


    @Transactional
    public void saveClusterNodeConf(AlertClusterNodeConfReq alertClusterNodeConfReq) {
        Long templateId = alertClusterNodeConfReq.getTemplateId();
        String clusterNodeIds = alertClusterNodeConfReq.getClusterNodeIds();
        List<String> clusterNodeIdList = Arrays.asList(clusterNodeIds.split(CommonConstants.DELIMITER));
        // save new data
        List<AlertClusterNodeConf> oldListByTemplateId =
            this.baseMapper.selectList(Wrappers.<AlertClusterNodeConf>lambdaQuery()
                .eq(AlertClusterNodeConf::getTemplateId, templateId)
                .eq(AlertClusterNodeConf::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        List<String> oldNodeIdListByTemplateId = oldListByTemplateId.stream().map(
            item -> item.getClusterNodeId()).collect(
            Collectors.toList());
        List<String> newNodeIdList = clusterNodeIdList.stream().filter(
            item -> !oldNodeIdListByTemplateId.contains(item)).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(newNodeIdList)) {
            return;
        }
        List<AlertClusterNodeConf> alertClusterNodeConfList = newNodeIdList.stream().map(
            item -> new AlertClusterNodeConf().setClusterNodeId(item).setTemplateId(templateId).setCreateTime(
                new Timestamp(System.currentTimeMillis())).setIsDeleted(CommonConstants.IS_NOT_DELETE)).collect(
            Collectors.toList());
        this.saveBatch(alertClusterNodeConfList);
        Map<Long, String> ruleConfigMap = new HashMap<>();  // use to update the prometheus rule configuration
        oldNodeIdListByTemplateId.addAll(newNodeIdList);
        ruleConfigMap.put(templateId, String.join(CommonConstants.DELIMITER, oldNodeIdListByTemplateId));
        ruleConfigMap.putAll(getUpdateRuleConfigMap(templateId, clusterNodeIdList));

        // delete old data by clusterNodeIdList, which exclude templateId
        LambdaUpdateWrapper<AlertClusterNodeConf> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(AlertClusterNodeConf::getIsDeleted, CommonConstants.IS_DELETE)
            .set(AlertClusterNodeConf::getUpdateTime, new Timestamp(System.currentTimeMillis()))
            .ne(AlertClusterNodeConf::getTemplateId, templateId).in(AlertClusterNodeConf::getClusterNodeId,
                clusterNodeIdList).eq(AlertClusterNodeConf::getIsDeleted, CommonConstants.IS_NOT_DELETE);
        this.update(null, updateWrapper);
        // update prometheus rule config
        prometheusService.updateRuleConfig(ruleConfigMap);
    }

    private Map<Long, String> getUpdateRuleConfigMap(Long templateId, List<String> clusterNodeIdList) {
        // set the old rule configuration params which should be updated
        List<AlertClusterNodeConf> oldListByNodeIds = this.baseMapper.selectList(
            Wrappers.<AlertClusterNodeConf>lambdaQuery().ne(AlertClusterNodeConf::getTemplateId, templateId).in(
                AlertClusterNodeConf::getClusterNodeId,
                clusterNodeIdList).eq(AlertClusterNodeConf::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        Map<Long, String> ruleConfigMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(oldListByNodeIds)) {
            Map<Long, List<String>> listMap = oldListByNodeIds.stream().collect(
                Collectors.groupingBy(AlertClusterNodeConf::getTemplateId,
                    Collectors.mapping(AlertClusterNodeConf::getClusterNodeId, Collectors.toList())));
            listMap.forEach((templateId0, nodeIdList0) -> {
                List<AlertClusterNodeConf> alertClusterNodeConfs = this.baseMapper.selectList(
                    Wrappers.<AlertClusterNodeConf>lambdaQuery().eq(AlertClusterNodeConf::getTemplateId,
                            templateId0).notIn(AlertClusterNodeConf::getClusterNodeId, nodeIdList0)
                        .eq(AlertClusterNodeConf::getIsDeleted, CommonConstants.IS_NOT_DELETE));
                if (CollectionUtil.isEmpty(alertClusterNodeConfs)) {
                    ruleConfigMap.put(templateId0, "");
                } else {
                    ruleConfigMap.put(templateId0,
                        alertClusterNodeConfs.stream().map(item -> item.getClusterNodeId()).collect(
                            Collectors.joining(CommonConstants.DELIMITER)));
                }
            });
        }
        return ruleConfigMap;
    }

    @Override
    public AlertClusterNodeConf getByClusterNodeId(String clusterNodeId) {
        List<AlertClusterNodeConf> alertClusterNodeConfs = this.baseMapper.selectList(
            Wrappers.<AlertClusterNodeConf>lambdaQuery().eq(AlertClusterNodeConf::getClusterNodeId,
                clusterNodeId).eq(AlertClusterNodeConf::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isEmpty(alertClusterNodeConfs)) {
            return new AlertClusterNodeConf();
        }
        return alertClusterNodeConfs.get(0);
    }

    @Override
    @Transactional
    public void saveAlertTemplateAndConfig(AlertClusterNodeAndTemplateReq clusterNodeAndTemplateReq) {
        AlertTemplateReq templateReq = new AlertTemplateReq();
        BeanUtil.copyProperties(clusterNodeAndTemplateReq, templateReq);
        AlertTemplate alertTemplate = templateService.saveTemplate(templateReq);
        AlertClusterNodeConfReq alertClusterNodeConfReq = new AlertClusterNodeConfReq();
        alertClusterNodeConfReq.setTemplateId(alertTemplate.getId());
        alertClusterNodeConfReq.setClusterNodeIds(clusterNodeAndTemplateReq.getClusterNodeIds());
        this.saveClusterNodeConf(alertClusterNodeConfReq);
    }

    @Override
    public List<AlertClusterNodeConfDto> getList() {
        List<AlertClusterNodeConfDto> dtoList = new ArrayList<>();
        List<OpsClusterNodeEntity> list = clusterNodeService.list();
        Set<String> hostIdSet = list.stream().map(item -> item.getHostId()).collect(Collectors.toSet());
        List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIdSet);
        List<String> clusterIdList = list.stream().map(item -> item.getClusterId()).collect(Collectors.toList());
        List<OpsClusterEntity> opsClusterEntities = clusterService.listByIds(clusterIdList);
        List<String> nodeIdList = list.stream().map(item -> item.getClusterNodeId()).collect(Collectors.toList());
        List<AlertClusterNodeConf> alertClusterNodeConfs = this.baseMapper.selectList(
            Wrappers.<AlertClusterNodeConf>lambdaQuery().in(CollectionUtil.isNotEmpty(nodeIdList),
                AlertClusterNodeConf::getClusterNodeId, nodeIdList).eq(AlertClusterNodeConf::getIsDeleted,
                CommonConstants.IS_NOT_DELETE));
        for (OpsClusterNodeEntity clusterNode : list) {
            OpsHostEntity opsHostEntity =
                opsHostEntities.stream().filter(
                    item -> item.getHostId().equals(clusterNode.getHostId())).findFirst().get();
            OpsClusterEntity opsClusterEntity =
                opsClusterEntities.stream().filter(
                    item -> item.getClusterId().equals(clusterNode.getClusterId())).findFirst().get();
            String nodeName = opsClusterEntity.getClusterId() + "/" + opsHostEntity.getPublicIp() + ":"
                + opsClusterEntity.getPort() + "(" + clusterNode.getClusterRole() + ")";
            AlertClusterNodeConfDto clusterNodeConfDto = new AlertClusterNodeConfDto();
            clusterNodeConfDto.setClusterNodeId(clusterNode.getClusterNodeId()).setNodeName(nodeName);
            List<AlertClusterNodeConf> clusterNodeConfs = alertClusterNodeConfs.stream().filter(
                item -> item.getClusterNodeId().equals(clusterNode.getClusterNodeId())).collect(
                Collectors.toList());
            if (CollectionUtil.isNotEmpty(clusterNodeConfs)) {
                AlertClusterNodeConf alertClusterNodeConf = clusterNodeConfs.get(0);
                AlertTemplate alertTemplate = templateMapper.selectById(alertClusterNodeConf.getTemplateId());
                clusterNodeConfDto.setTemplateId(alertClusterNodeConf.getTemplateId()).setTemplateName(
                    alertTemplate.getTemplateName());
            }
            dtoList.add(clusterNodeConfDto);
        }
        return dtoList;
    }
}
