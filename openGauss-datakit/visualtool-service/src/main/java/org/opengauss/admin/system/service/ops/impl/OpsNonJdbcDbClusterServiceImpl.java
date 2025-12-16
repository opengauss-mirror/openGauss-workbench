/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.system.service.ops.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.util.StrUtil;
import io.milvus.v2.client.MilvusClientV2;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.client.RestClient;
import org.opengauss.admin.common.core.domain.entity.ops.OpsNonJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsNonJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.system.utils.ElasticsearchUtils;
import org.opengauss.admin.system.utils.MilvusUtils;
import org.opengauss.admin.common.utils.SecurityUtils;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.system.mapper.ops.OpsNonJdbcDbClusterMapper;
import org.opengauss.admin.system.service.ops.IOpsNonJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsNonJdbcDbClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * OpsNonJdbcDbClusterServiceImpl
 *
 * @since 2025/11/6
 */
@Slf4j
@Service
public class OpsNonJdbcDbClusterServiceImpl extends ServiceImpl<OpsNonJdbcDbClusterMapper, OpsNonJdbcDbClusterEntity>
        implements IOpsNonJdbcDbClusterService {
    @Autowired
    private IOpsNonJdbcDbClusterNodeService nonJdbcDbClusterNodeService;

    @Autowired
    private EncryptionUtils encryptionUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(JdbcDbClusterInputDto clusterInput) {
        DbTypeEnum dbTypeEnum = clusterInput.getDbType();
        if (dbTypeEnum == null) {
            throw new OpsException("Database type cannot be empty");
        }
        if (dbTypeEnum != DbTypeEnum.MILVUS && dbTypeEnum != DbTypeEnum.ELASTICSEARCH) {
            throw new OpsException("Non-JDBC database type only supports Milvus and ElasticSearch");
        }

        List<JdbcDbClusterNodeInputDto> nodes = clusterInput.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            throw new OpsException("Cluster node information does not exist");
        }

        String clusterName = clusterInput.getClusterName();
        if (StringUtils.isEmpty(clusterName)) {
            throw new OpsException("Cluster name cannot be empty");
        }

        LambdaQueryWrapper<OpsNonJdbcDbClusterEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OpsNonJdbcDbClusterEntity::getName, clusterName);
        List<OpsNonJdbcDbClusterEntity> clusterEntityList = list(queryWrapper);
        if (clusterEntityList != null && !clusterEntityList.isEmpty()) {
            throw new OpsException("Cluster name already exists");
        }

        OpsNonJdbcDbClusterEntity nonJdbcDbClusterEntity = saveNonJdbcCluster(clusterInput);
        saveNonJdbcClusterNodes(clusterInput, nonJdbcDbClusterEntity.getClusterId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            return;
        }

        nonJdbcDbClusterNodeService.deleteByClusterId(clusterId);
        removeById(clusterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        nonJdbcDbClusterNodeService.deleteByClusterIds(ids);
        removeByIds(ids);
    }

    @Override
    public List<OpsNonJdbcDbClusterEntity> listByName(String name) {
        if (StrUtil.isEmpty(name)) {
            return list();
        }

        LambdaQueryWrapper<OpsNonJdbcDbClusterEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(OpsNonJdbcDbClusterEntity::getName, name);
        return list(queryWrapper);
    }

    @Override
    public List<JdbcDbClusterVO> listByType(DbTypeEnum dbType) {
        List<OpsNonJdbcDbClusterEntity> nonJdbcClusters;
        if (dbType == null) {
            nonJdbcClusters = list();
        } else {
            LambdaQueryWrapper<OpsNonJdbcDbClusterEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OpsNonJdbcDbClusterEntity::getDbType, dbType);
            nonJdbcClusters = list(queryWrapper);
        }

        List<OpsNonJdbcDbClusterNodeEntity> nonJdbcNodes = nonJdbcDbClusterNodeService.listByClusterIds(
                nonJdbcClusters.stream().map(OpsNonJdbcDbClusterEntity::getClusterId).toList());
        return buildJdbcClusterVos(nonJdbcClusters, nonJdbcNodes);
    }

    @Override
    public List<OpsNonJdbcDbClusterEntity> listByNameAndType(String name, String type) {
        LambdaQueryWrapper<OpsNonJdbcDbClusterEntity> wrapper = new LambdaQueryWrapper<>();
        if (!StrUtil.isEmpty(name)) {
            wrapper.like(OpsNonJdbcDbClusterEntity::getName, name);
        }
        if (!StrUtil.isEmpty(type)) {
            wrapper.like(OpsNonJdbcDbClusterEntity::getDbType, type.toUpperCase(Locale.ROOT));
        }

        return list(wrapper);
    }

    @Override
    public List<JdbcDbClusterVO> getJdbcClusterVosByNameAndIp(String name, String ip) {
        List<OpsNonJdbcDbClusterEntity> nonJdbcClusters = listByName(name);
        List<OpsNonJdbcDbClusterNodeEntity> nonJdbcNodes = nonJdbcDbClusterNodeService.listByIpAndClusterIds(
                ip, nonJdbcClusters.stream().map(OpsNonJdbcDbClusterEntity::getClusterId).toList());
        List<String> nonJdbcIds = nonJdbcNodes.stream().map(OpsNonJdbcDbClusterNodeEntity::getClusterId).toList();
        List<OpsNonJdbcDbClusterEntity> nonJdbcClustersFiltered = nonJdbcClusters.stream()
                .filter(cluster -> nonJdbcIds.contains(cluster.getClusterId()))
                .toList();

        return buildJdbcClusterVos(nonJdbcClustersFiltered, nonJdbcNodes);
    }

    @Override
    public List<JdbcDbClusterVO> getJdbcClusterVosByNameIpAndType(String name, String ip, String type) {
        List<OpsNonJdbcDbClusterEntity> nonJdbcClusters = listByNameAndType(name, type);
        List<OpsNonJdbcDbClusterNodeEntity> nonJdbcNodes = nonJdbcDbClusterNodeService.listByIpAndClusterIds(
                ip, nonJdbcClusters.stream().map(OpsNonJdbcDbClusterEntity::getClusterId).toList());
        List<String> nonJdbcIds = nonJdbcNodes.stream().map(OpsNonJdbcDbClusterNodeEntity::getClusterId).toList();
        List<OpsNonJdbcDbClusterEntity> nonJdbcClustersFiltered = nonJdbcClusters.stream()
                .filter(cluster -> nonJdbcIds.contains(cluster.getClusterId()))
                .toList();

        return buildJdbcClusterVos(nonJdbcClustersFiltered, nonJdbcNodes);
    }

    @Override
    public String version(String clusterId, DbTypeEnum dbType) {
        if (StrUtil.isEmpty(clusterId)) {
            throw new IllegalArgumentException("Cluster id cannot be empty");
        }

        LambdaQueryWrapper<OpsNonJdbcDbClusterEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OpsNonJdbcDbClusterEntity::getClusterId, clusterId);
        if (dbType != null) {
            queryWrapper.eq(OpsNonJdbcDbClusterEntity::getDbType, dbType);
        }
        OpsNonJdbcDbClusterEntity clusterEntity = getOne(queryWrapper);
        if (clusterEntity == null) {
            log.error("Cluster does not exist, cluster id: {}", clusterId);
            throw new OpsException("Cluster does not exist, cluster id: " + clusterId);
        }

        String versionNum = clusterEntity.getVersionNum();
        if (!StrUtil.isEmpty(versionNum)) {
            return versionNum;
        }

        DbTypeEnum currentDbType = clusterEntity.getDbType();
        OpsNonJdbcDbClusterNodeEntity node = nonJdbcDbClusterNodeService.getOneByClusterId(clusterId);
        if (DbTypeEnum.MILVUS.equals(currentDbType)) {
            versionNum = getMilvusVersionNum(node.getUrl(), node.getUsername(), node.getPassword());
        } else if (DbTypeEnum.ELASTICSEARCH.equals(currentDbType)) {
            versionNum = getElasticsearchVersionNum(node.getUrl(), node.getUsername(), node.getPassword());
        } else {
            throw new OpsException("Unsupported database type: " + currentDbType);
        }

        clusterEntity.setVersionNum(versionNum);
        updateById(clusterEntity);
        return versionNum;
    }

    private List<JdbcDbClusterVO> buildJdbcClusterVos(
            List<OpsNonJdbcDbClusterEntity> nonJdbcClusters, List<OpsNonJdbcDbClusterNodeEntity> nonJdbcNodes) {
        ArrayList<JdbcDbClusterVO> jdbcDbClusterVos = new ArrayList<>();
        for (OpsNonJdbcDbClusterEntity cluster : nonJdbcClusters) {
            List<JdbcDbClusterNodeVO> nodes = nonJdbcNodes.stream()
                    .filter(node -> node.getClusterId().equals(cluster.getClusterId()))
                    .map(JdbcDbClusterNodeVO::of)
                    .toList();
            jdbcDbClusterVos.add(JdbcDbClusterVO.of(cluster, nodes));
        }
        return jdbcDbClusterVos;
    }

    private void saveNonJdbcClusterNodes(JdbcDbClusterInputDto clusterInput, String clusterId) {
        List<JdbcDbClusterNodeInputDto> inputNodes = clusterInput.getNodes();
        Date now = new Date();
        String createUser = SecurityUtils.getUsername();
        List<OpsNonJdbcDbClusterNodeEntity> clusterNodeEntryList = new ArrayList<>();

        URL parsedUrl;
        for (JdbcDbClusterNodeInputDto inputNode : inputNodes) {
            parsedUrl = MilvusUtils.parseUrl(inputNode.getUrl());
            OpsNonJdbcDbClusterNodeEntity nodeEntity = new OpsNonJdbcDbClusterNodeEntity();
            nodeEntity.setClusterId(clusterId);
            nodeEntity.setIp(parsedUrl.getHost());
            nodeEntity.setPort(String.valueOf(parsedUrl.getPort()));

            String username = inputNode.getUsername();
            String password = inputNode.getPassword();
            if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
                nodeEntity.setUsername(username);
                nodeEntity.setPassword(encryptionUtils.encrypt(encryptionUtils.decrypt(password)));
            }

            nodeEntity.setUrl(inputNode.getUrl());
            nodeEntity.setCreateTime(now);
            nodeEntity.setUpdateTime(now);
            nodeEntity.setCreateBy(createUser);
            nodeEntity.setUpdateBy(createUser);
            clusterNodeEntryList.add(nodeEntity);
        }
        nonJdbcDbClusterNodeService.saveBatch(clusterNodeEntryList);
    }

    private OpsNonJdbcDbClusterEntity saveNonJdbcCluster(JdbcDbClusterInputDto clusterInput) {
        OpsNonJdbcDbClusterEntity clusterEntity = new OpsNonJdbcDbClusterEntity();
        clusterEntity.setName(clusterInput.getClusterName());
        clusterEntity.setDbType(clusterInput.getDbType());
        clusterEntity.setRemark(clusterInput.getRemark());

        int nodeSize = clusterInput.getNodes().size();
        if (nodeSize == 1) {
            clusterEntity.setDeployType(DeployTypeEnum.SINGLE_NODE);
        } else {
            clusterEntity.setDeployType(DeployTypeEnum.CLUSTER);
        }

        clusterEntity.setVersionNum(getClusterVersionNum(clusterInput));

        Date now = new Date();
        clusterEntity.setCreateTime(now);
        clusterEntity.setUpdateTime(now);
        String username = SecurityUtils.getUsername();
        clusterEntity.setCreateBy(username);
        clusterEntity.setUpdateBy(username);

        save(clusterEntity);
        return clusterEntity;
    }

    private String getClusterVersionNum(JdbcDbClusterInputDto clusterInput) {
        List<JdbcDbClusterNodeInputDto> inputNodes = clusterInput.getNodes();
        if (inputNodes == null || inputNodes.isEmpty()) {
            throw new IllegalArgumentException("Cluster nodes cannot be empty");
        }

        JdbcDbClusterNodeInputDto clusterNodeInputDto = inputNodes.get(0);
        DbTypeEnum dbType = clusterInput.getDbType();
        if (DbTypeEnum.MILVUS.equals(dbType)) {
            return getMilvusVersionNum(clusterNodeInputDto.getUrl(), clusterNodeInputDto.getUsername(),
                    clusterNodeInputDto.getPassword());
        } else if (DbTypeEnum.ELASTICSEARCH.equals(dbType)) {
            return getElasticsearchVersionNum(clusterNodeInputDto.getUrl(), clusterNodeInputDto.getUsername(),
                    clusterNodeInputDto.getPassword());
        } else {
            throw new IllegalArgumentException("Unsupported db type: " + dbType);
        }
    }

    private String getMilvusVersionNum(String url, String username, String password) {
        URL parsedUrl = MilvusUtils.parseUrl(url);
        String ip = parsedUrl.getHost();
        int port = parsedUrl.getPort();
        MilvusClientV2 milvusClientV2 = null;
        try {
            if (StrUtil.isEmpty(username) || StrUtil.isEmpty(password)) {
                milvusClientV2 = MilvusUtils.createMilvusClientV2(ip, port, null, null, null);
            } else {
                milvusClientV2 = MilvusUtils.createMilvusClientV2(
                        ip, port, null, username, encryptionUtils.decrypt(password));
            }
            return MilvusUtils.getMilvusVersion(milvusClientV2);
        } catch (Exception e) {
            log.error("Failed to get Milvus version", e);
            throw new OpsException(e.getClass().getName() + e.getMessage());
        } finally {
            if (milvusClientV2 != null) {
                MilvusUtils.closeMilvusClientV2(milvusClientV2);
            }
        }
    }

    private String getElasticsearchVersionNum(String url, String username, String password) {
        URL parsedUrl = ElasticsearchUtils.parseUrl(url);
        String ip = parsedUrl.getHost();
        int port = parsedUrl.getPort();
        RestClient restClient = null;
        try {
            if (StrUtil.isEmpty(username) || StrUtil.isEmpty(password)) {
                restClient = ElasticsearchUtils.createRestClient(ip, port, null, null);
            } else {
                restClient = ElasticsearchUtils.createRestClient(ip, port, username, encryptionUtils.decrypt(password));
            }
            return ElasticsearchUtils.getElasticsearchVersion(restClient);
        } catch (IOException e) {
            log.error("Failed to get Elasticsearch version", e);
            throw new OpsException(e.getClass().getName() + e.getMessage());
        } finally {
            if (restClient != null) {
                try {
                    ElasticsearchUtils.closeClient(restClient);
                } catch (IOException e) {
                    log.debug("Failed to close ElasticSearch client", e);
                }
            }
        }
    }
}
