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
 *  AlertShieldingServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/AlertShieldingServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.mapper.AlertShieldingMapper;
import com.nctigba.alert.monitor.model.dto.AlertShieldingDTO;
import com.nctigba.alert.monitor.model.dto.ClusterNodeDTO;
import com.nctigba.alert.monitor.model.entity.AlertShieldingDO;
import com.nctigba.alert.monitor.model.query.AlertShieldingQuery;
import com.nctigba.alert.monitor.service.AlertShieldingService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AlertShieldingServiceImpl
 *
 * @author luomeng
 * @since 2024/6/30
 */
@Slf4j
@Service
public class AlertShieldingServiceImpl extends ServiceImpl<AlertShieldingMapper, AlertShieldingDO>
        implements AlertShieldingService {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterService clusterService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsClusterNodeService clusterNodeService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    @Override
    public Page<AlertShieldingDTO> selectByPage(AlertShieldingQuery shieldingQuery, Page page) {
        Page<AlertShieldingDO> doPage = this.baseMapper.selectPage(page, Wrappers.<AlertShieldingDO>lambdaQuery()
                .like(StrUtil.isNotBlank(shieldingQuery.getRuleName()), AlertShieldingDO::getRuleName,
                        shieldingQuery.getRuleName())
                .like(StrUtil.isNotBlank(shieldingQuery.getNodeId()), AlertShieldingDO::getClusterNodeIds,
                        shieldingQuery.getNodeId())
                .eq(AlertShieldingDO::getIsDeleted, CommonConstants.IS_NOT_DELETE).orderByDesc(
                        AlertShieldingDO::getId));
        Page<AlertShieldingDTO> dtoPage = new Page<>();
        dtoPage.setTotal(doPage.getTotal()).setCurrent(doPage.getCurrent()).setSize(doPage.getSize());
        List<AlertShieldingDO> records = doPage.getRecords();
        if (CollectionUtil.isEmpty(records)) {
            dtoPage.setRecords(new ArrayList<>());
            return dtoPage;
        }
        List<ClusterNodeDTO> clusterNodes = getAllClusterNodes();
        List<AlertShieldingDTO> dtoRecords = new ArrayList<>();
        for (AlertShieldingDO record : records) {
            AlertShieldingDTO dto = new AlertShieldingDTO();
            BeanUtil.copyProperties(record, dto);
            List<ClusterNodeDTO> nodeInfoList = getClusterNodesByIds(record.getClusterNodeIds(), clusterNodes);
            dto.setClusterNodeList(nodeInfoList);
            dtoRecords.add(dto);
        }
        dtoPage.setRecords(dtoRecords);
        return dtoPage;
    }

    private List<ClusterNodeDTO> getAllClusterNodes() {
        List<OpsClusterVO> listCluster = clusterService.listCluster();
        List<ClusterNodeDTO> list = new ArrayList<>();
        for (OpsClusterVO clusterVo : listCluster) {
            List<OpsClusterNodeEntity> nodes = clusterNodeService.listClusterNodeByClusterId(clusterVo.getClusterId());
            for (OpsClusterNodeEntity node : nodes) {
                OpsHostEntity host = hostFacade.getById(node.getHostId());
                ClusterNodeDTO clusterNode = new ClusterNodeDTO();
                clusterNode.setClusterNodeId(node.getClusterNodeId());
                String clusterName = StrUtil.isNotBlank(
                        clusterVo.getClusterName()) ? clusterVo.getClusterName() : clusterVo.getClusterId();
                String nodeName = clusterName + "/" + host.getPublicIp() + ":"
                        + host.getPort() + "(" + node.getClusterRole() + ")";
                clusterNode.setNodeName(nodeName).setClusterId(node.getClusterId());
                list.add(clusterNode);
            }
        }
        return list;
    }

    private List<ClusterNodeDTO> getClusterNodesByIds(String clusterNodeIds, List<ClusterNodeDTO> clusterNodes) {
        if (StrUtil.isBlank(clusterNodeIds)) {
            return new ArrayList<>();
        }
        List<ClusterNodeDTO> list = new ArrayList<>();
        for (String nodeId : clusterNodeIds.split(CommonConstants.DELIMITER)) {
            ClusterNodeDTO clusterNode = new ClusterNodeDTO();
            clusterNode.setClusterNodeId(nodeId);
            clusterNodes.forEach(f -> {
                if (f.getClusterNodeId().equals(nodeId)) {
                    clusterNode.setNodeName(f.getNodeName()).setClusterId(f.getClusterId());
                    list.add(clusterNode);
                }
            });
        }
        return list;
    }

    @Override
    public AlertShieldingDO selectById(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public void deleteById(Long id) {
        LambdaUpdateWrapper<AlertShieldingDO> ruleUpdateWrapper = new LambdaUpdateWrapper<AlertShieldingDO>()
                .set(AlertShieldingDO::getIsDeleted, CommonConstants.IS_DELETE)
                .set(AlertShieldingDO::getUpdateTime, LocalDateTime.now()).eq(AlertShieldingDO::getId, id);
        this.update(ruleUpdateWrapper);
    }

    @Override
    public void batchDelete(String ids) {
        String[] idArr = ids.split(CommonConstants.DELIMITER);
        for (String id : idArr) {
            LambdaUpdateWrapper<AlertShieldingDO> ruleUpdateWrapper = new LambdaUpdateWrapper<AlertShieldingDO>()
                    .set(AlertShieldingDO::getIsDeleted, CommonConstants.IS_DELETE)
                    .set(AlertShieldingDO::getUpdateTime, LocalDateTime.now()).eq(AlertShieldingDO::getId, id);
            this.update(ruleUpdateWrapper);
        }
    }

    @Override
    public void updateStatusById(Long id, Integer isEnable) {
        LambdaUpdateWrapper<AlertShieldingDO> ruleUpdateWrapper = new LambdaUpdateWrapper<AlertShieldingDO>()
                .set(AlertShieldingDO::getIsEnable, isEnable)
                .set(AlertShieldingDO::getUpdateTime, LocalDateTime.now()).eq(AlertShieldingDO::getId, id);
        this.update(ruleUpdateWrapper);
    }

    @Override
    public void batchUpdateStatus(String ids, Integer isEnable) {
        String[] idArr = ids.split(CommonConstants.DELIMITER);
        for (String id : idArr) {
            LambdaUpdateWrapper<AlertShieldingDO> ruleUpdateWrapper = new LambdaUpdateWrapper<AlertShieldingDO>()
                    .set(AlertShieldingDO::getIsEnable, isEnable)
                    .set(AlertShieldingDO::getUpdateTime, LocalDateTime.now()).eq(AlertShieldingDO::getId, id);
            this.update(ruleUpdateWrapper);
        }
    }
}
