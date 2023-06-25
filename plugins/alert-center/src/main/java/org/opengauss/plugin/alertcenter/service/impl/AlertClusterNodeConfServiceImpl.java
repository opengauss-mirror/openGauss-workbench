/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.opengauss.plugin.alertcenter.constant.CommonConstants;
import org.opengauss.plugin.alertcenter.dto.AlertClusterNodeConfDto;
import org.opengauss.plugin.alertcenter.entity.AlertClusterNodeConf;
import org.opengauss.plugin.alertcenter.entity.AlertTemplate;
import org.opengauss.plugin.alertcenter.mapper.AlertClusterNodeConfMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertConfigMapper;
import org.opengauss.plugin.alertcenter.mapper.AlertTemplateMapper;
import org.opengauss.plugin.alertcenter.mapper.NctigbaEnvMapper;
import org.opengauss.plugin.alertcenter.model.AlertClusterNodeAndTemplateReq;
import org.opengauss.plugin.alertcenter.model.AlertClusterNodeConfReq;
import org.opengauss.plugin.alertcenter.model.AlertTemplateReq;
import org.opengauss.plugin.alertcenter.service.AlertClusterNodeConfService;
import org.opengauss.plugin.alertcenter.service.AlertTemplateService;
import org.opengauss.plugin.alertcenter.service.PrometheusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    private NctigbaEnvMapper envMapper;

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

    @Autowired
    private AlertConfigMapper alertConfigMapper;

    @Transactional
    public void saveClusterNodeConf(AlertClusterNodeConfReq alertClusterNodeConfReq) {
        Long templateId = alertClusterNodeConfReq.getTemplateId();
        String clusterNodeIds = alertClusterNodeConfReq.getClusterNodeIds();
        String[] clusterNodeIdArr = clusterNodeIds.split(CommonConstants.DELIMITER);
        List<String> notNeedToUpdatedNodeIdList = new ArrayList<>();
        Set<Long> needUpdatedTemplateIdSet = new HashSet<>();
        Set<String> delNodeConfIds = new HashSet<>();
        List<AlertClusterNodeConf> oldAlertClusterNodeConfList = this.baseMapper.selectList(
                Wrappers.<AlertClusterNodeConf>lambdaQuery().in(AlertClusterNodeConf::getClusterNodeId,
                        clusterNodeIdArr).eq(AlertClusterNodeConf::getIsDeleted, CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isNotEmpty(oldAlertClusterNodeConfList)) {
            // remove the relation between the cluster node and the template
            List<AlertClusterNodeConf> delConfList = oldAlertClusterNodeConfList.stream().filter(
                    item -> !item.getTemplateId().equals(templateId)).map(item -> {
                item.setIsDeleted(CommonConstants.IS_DELETE);
                return item;
            }).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(delConfList)) {
                Set<Long> set = delConfList.stream().map(item -> item.getTemplateId()).collect(Collectors.toSet());
                needUpdatedTemplateIdSet.addAll(set);
                Set<String> confIdSet =
                    delConfList.stream().map(item -> item.getClusterNodeId()).collect(Collectors.toSet());
                delNodeConfIds.addAll(confIdSet);
                this.saveOrUpdateBatch(delConfList);
            }
            // get the cluster node which is not need to update
            List<String> nodeIdList = oldAlertClusterNodeConfList.stream().filter(
                    item -> item.getTemplateId().equals(templateId)).map(item -> item.getClusterNodeId()).collect(
                    Collectors.toList());
            notNeedToUpdatedNodeIdList.addAll(nodeIdList);
        }
        List<AlertClusterNodeConf> alertClusterNodeConfList = Arrays.stream(clusterNodeIdArr).filter(
                item -> !notNeedToUpdatedNodeIdList.contains(item)).map(
                item -> new AlertClusterNodeConf().setClusterNodeId(item)
                        .setTemplateId(alertClusterNodeConfReq.getTemplateId())
                        .setCreateTime(new Timestamp(System.currentTimeMillis())).setIsDeleted(
                                CommonConstants.IS_NOT_DELETE)).collect(Collectors.toList());

        if (CollectionUtil.isEmpty(alertClusterNodeConfList)) {
            return;
        }
        needUpdatedTemplateIdSet.add(templateId);
        // 保存数据库
        this.saveBatch(alertClusterNodeConfList);
        // 更新prometheus规则配置文件
        prometheusService.updateRuleConfig(needUpdatedTemplateIdSet, delNodeConfIds);
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
