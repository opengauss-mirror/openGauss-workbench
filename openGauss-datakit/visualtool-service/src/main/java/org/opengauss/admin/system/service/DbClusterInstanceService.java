/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsNonJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.PageHelper;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterService;
import org.opengauss.admin.system.service.ops.IOpsNonJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsNonJdbcDbClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * DbClusterInstanceService
 *
 * @since 2025/11/6
 */
@Slf4j
@Service
public class DbClusterInstanceService extends BaseController {
    @Autowired
    private IOpsJdbcDbClusterService jdbcDbClusterService;

    @Autowired
    private IOpsJdbcDbClusterNodeService jdbcDbClusterNodeService;

    @Autowired
    private IOpsNonJdbcDbClusterService nonJdbcDbClusterService;

    @Autowired
    private IOpsNonJdbcDbClusterNodeService nonJdbcDbClusterNodeService;

    /**
     * Test cluster instance connection
     *
     * @param clusterNodeInput cluster node input
     * @return true if connection success
     */
    public boolean ping(JdbcDbClusterNodeInputDto clusterNodeInput) {
        if (clusterNodeInput == null) {
            throw new IllegalArgumentException("Cluster node input cannot be null");
        }

        DbTypeEnum dbType = clusterNodeInput.getDbType();
        if (DbTypeEnum.MILVUS.equals(dbType) || DbTypeEnum.ELASTICSEARCH.equals(dbType)) {
            return nonJdbcDbClusterNodeService.ping(clusterNodeInput);
        } else {
            return jdbcDbClusterNodeService.ping(clusterNodeInput);
        }
    }

    /**
     * Add cluster
     *
     * @param clusterInput cluster input
     * @return ajax result
     */
    public AjaxResult add(JdbcDbClusterInputDto clusterInput) {
        if (clusterInput == null) {
            throw new IllegalArgumentException("Cluster input cannot be null");
        }

        DbTypeEnum dbType = clusterInput.getDbType();
        if (DbTypeEnum.MILVUS.equals(dbType) || DbTypeEnum.ELASTICSEARCH.equals(dbType)) {
            nonJdbcDbClusterService.add(clusterInput);
        } else {
            jdbcDbClusterService.add(clusterInput);
        }
        return AjaxResult.success();
    }

    /**
     * Delete cluster by id
     *
     * @param clusterId cluster id
     */
    public void delete(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            return;
        }

        nonJdbcDbClusterService.delete(clusterId);
        jdbcDbClusterService.delete(clusterId);
    }

    /**
     * Batch delete clusters
     *
     * @param ids cluster ids
     */
    public void batchDelete(List<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return;
        }

        nonJdbcDbClusterService.batchDelete(ids);
        jdbcDbClusterService.batchDelete(ids);
    }

    /**
     * Update cluster
     *
     * @param clusterId cluster id
     * @param clusterInput cluster input
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(String clusterId, JdbcDbClusterInputDto clusterInput) {
        if (StrUtil.isEmpty(clusterId)) {
            throw new IllegalArgumentException("Cluster id cannot be empty");
        }

        OpsJdbcDbClusterEntity clusterEntity = jdbcDbClusterService.getById(clusterId);
        if (clusterEntity == null) {
            OpsNonJdbcDbClusterEntity nonJdbcClusterEntity = nonJdbcDbClusterService.getById(clusterId);
            if (nonJdbcClusterEntity == null) {
                throw new OpsException("Cluster information does not exist, cluster id: " + clusterId);
            }
        }

        delete(clusterId);
        add(clusterInput);
    }

    /**
     * Select cluster page list
     *
     * @param name cluster name
     * @param ip cluster node ip
     * @param type database type
     * @return page info
     */
    public TableDataInfo page(
            @RequestParam(required = false, value = "name") String name,
            @RequestParam(required = false, value = "ip") String ip,
            @RequestParam(required = false, value = "type") String type
    ) {
        if (type == null || type.isEmpty()) {
            List<JdbcDbClusterVO> dbClusterInstanceVos = new ArrayList<>();
            dbClusterInstanceVos.addAll(jdbcDbClusterService.getJdbcClusterVosByNameAndIp(name, ip));
            dbClusterInstanceVos.addAll(nonJdbcDbClusterService.getJdbcClusterVosByNameAndIp(name, ip));

            dbClusterInstanceVos.sort(Comparator.comparing(JdbcDbClusterVO::getClusterId).reversed());

            Page<JdbcDbClusterVO> resultPage = PageHelper.getPageFromList(dbClusterInstanceVos, startPage());
            return getDataTable(resultPage);
        }

        if (DbTypeEnum.MILVUS.name().equalsIgnoreCase(type) || DbTypeEnum.ELASTICSEARCH.name().equalsIgnoreCase(type)) {
            List<JdbcDbClusterVO> dbClusters = nonJdbcDbClusterService.getJdbcClusterVosByNameIpAndType(name, ip, type);
            dbClusters.sort(Comparator.comparing(JdbcDbClusterVO::getClusterId).reversed());
            Page<JdbcDbClusterVO> resultPage = PageHelper.getPageFromList(dbClusters, startPage());
            return getDataTable(resultPage);
        }

        List<JdbcDbClusterVO> dbClusters = jdbcDbClusterService.getJdbcClusterVosByNameIpAndType(name, ip, type);
        dbClusters.sort(Comparator.comparing(JdbcDbClusterVO::getClusterId).reversed());
        Page<JdbcDbClusterVO> resultPage = PageHelper.getPageFromList(dbClusters, startPage());

        return getDataTable(resultPage);
    }

    /**
     * Get cluster instance version
     *
     * @param clusterId cluster id
     * @param dbType database type
     * @return cluster instance version
     */
    public String version(String clusterId, DbTypeEnum dbType) {
        if (dbType == null) {
            throw new IllegalArgumentException("DbTypeEnum cannot be null");
        }
        if (StrUtil.isEmpty(clusterId)) {
            throw new IllegalArgumentException("Cluster id cannot be empty");
        }

        if (dbType.isJdbcDriver()) {
            return jdbcDbClusterService.version(clusterId, dbType);
        } else {
            return nonJdbcDbClusterService.version(clusterId, dbType);
        }
    }

    /**
     * Monitor cluster node
     *
     * @param dbType database type
     * @param clusterNodeId cluster node id
     * @param businessId business id
     */
    public void monitor(DbTypeEnum dbType, String clusterNodeId, String businessId) {
        if (dbType == null) {
            throw new IllegalArgumentException("Database type cannot be null");
        }

        if (dbType.isJdbcDriver()) {
            jdbcDbClusterNodeService.monitor(dbType, clusterNodeId, businessId);
        } else {
            nonJdbcDbClusterNodeService.monitor(dbType, clusterNodeId, businessId);
        }
    }
}
