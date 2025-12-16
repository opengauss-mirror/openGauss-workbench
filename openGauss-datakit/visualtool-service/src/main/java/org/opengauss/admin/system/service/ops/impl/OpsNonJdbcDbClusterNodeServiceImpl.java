/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.system.service.ops.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.client.RestClient;
import org.opengauss.admin.common.core.domain.entity.ops.OpsNonJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsNonJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.system.utils.ElasticsearchUtils;
import org.opengauss.admin.system.utils.MilvusUtils;
import org.opengauss.admin.common.utils.ops.WsUtil;
import org.opengauss.admin.system.mapper.ops.OpsNonJdbcDbClusterNodeMapper;
import org.opengauss.admin.system.service.ops.IOpsNonJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsNonJdbcDbClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * OpsNonJdbcDbClusterNodeServiceImpl
 *
 * @since 2025/11/6
 */
@Slf4j
@Service
public class OpsNonJdbcDbClusterNodeServiceImpl
        extends ServiceImpl<OpsNonJdbcDbClusterNodeMapper, OpsNonJdbcDbClusterNodeEntity>
        implements IOpsNonJdbcDbClusterNodeService {
    @Autowired
    private EncryptionUtils encryptionUtils;

    @Lazy
    @Autowired
    private IOpsNonJdbcDbClusterService opsNonJdbcDbClusterService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private WsConnectorManager wsConnectorManager;

    @Autowired
    private WsUtil wsUtil;

    @Override
    public boolean ping(JdbcDbClusterNodeInputDto clusterNodeInput) {
        DbTypeEnum dbType = clusterNodeInput.getDbType();
        if (dbType == null) {
            throw new OpsException("DbType cannot be null");
        }

        if (DbTypeEnum.MILVUS.equals(dbType)) {
            return pingMilvus(clusterNodeInput);
        } else if (DbTypeEnum.ELASTICSEARCH.equals(dbType)) {
            return pingElasticsearch(clusterNodeInput);
        } else {
            throw new OpsException("DbTypeEnum " + dbType + " is not a supported non-JDBC db type");
        }
    }

    @Override
    public void deleteByClusterId(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            return;
        }

        LambdaQueryWrapper<OpsNonJdbcDbClusterNodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OpsNonJdbcDbClusterNodeEntity::getClusterId, clusterId);
        remove(queryWrapper);
    }

    @Override
    public void deleteByClusterIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<OpsNonJdbcDbClusterNodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OpsNonJdbcDbClusterNodeEntity::getClusterId, ids);
        remove(queryWrapper);
    }

    @Override
    public List<OpsNonJdbcDbClusterNodeEntity> listByClusterIds(List<String> clusterIds) {
        LambdaQueryWrapper<OpsNonJdbcDbClusterNodeEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (clusterIds != null && !clusterIds.isEmpty()) {
            queryWrapper.in(OpsNonJdbcDbClusterNodeEntity::getClusterId, clusterIds);
        }

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<OpsNonJdbcDbClusterNodeEntity> listByIpAndClusterIds(String ip, List<String> clusterIds) {
        LambdaQueryWrapper<OpsNonJdbcDbClusterNodeEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (!StrUtil.isEmpty(ip)) {
            queryWrapper.like(OpsNonJdbcDbClusterNodeEntity::getIp, ip);
        }
        if (clusterIds != null && !clusterIds.isEmpty()) {
            queryWrapper.in(OpsNonJdbcDbClusterNodeEntity::getClusterId, clusterIds);
        }

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public OpsNonJdbcDbClusterNodeEntity getOneByClusterId(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            throw new IllegalArgumentException("clusterId cannot be empty");
        }

        LambdaQueryWrapper<OpsNonJdbcDbClusterNodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OpsNonJdbcDbClusterNodeEntity::getClusterId, clusterId);
        OpsNonJdbcDbClusterNodeEntity clusterNodeEntity = baseMapper.selectOne(queryWrapper);
        if (clusterNodeEntity == null) {
            throw new OpsException("Cluster node not found for clusterId: " + clusterId);
        }
        return clusterNodeEntity;
    }

    @Override
    public void monitor(DbTypeEnum dbType, String clusterNodeId, String businessId) {
        if (StrUtil.isEmpty(clusterNodeId) || StrUtil.isEmpty(businessId)) {
            throw new IllegalArgumentException("Cluster node id and business id cannot be empty");
        }

        OpsNonJdbcDbClusterNodeEntity clusterNodeEntity = getById(clusterNodeId);
        if (clusterNodeEntity == null) {
            throw new OpsException("Non-Jdbc cluster node does not exist, clusterNodeId: " + clusterNodeId);
        }

        String clusterId = clusterNodeEntity.getClusterId();
        OpsNonJdbcDbClusterEntity clusterEntity = opsNonJdbcDbClusterService.getById(clusterId);
        if (clusterEntity == null) {
            throw new OpsException("Non-Jdbc cluster does not exist, clusterId: " + clusterId);
        }
        if (!clusterEntity.getDbType().equals(dbType)) {
            throw new OpsException("Non-Jdbc cluster database type does not match, dbType: " + dbType);
        }

        WsSession wsSession = wsConnectorManager.getSession(businessId)
                .orElseThrow(() -> new OpsException("WebSocket session does not exist, businessId: " + businessId));
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            try {
                doMonitor(wsSession, clusterNodeEntity, dbType);
            } finally {
                wsUtil.close(wsSession);
            }
        });
        TaskManager.registry(businessId, future);
    }

    private void doMonitor(WsSession wsSession, OpsNonJdbcDbClusterNodeEntity nodeEntity, DbTypeEnum dbType) {
        try {
            if (DbTypeEnum.MILVUS.equals(dbType)) {
                doMilvusMonitor(wsSession, nodeEntity);
            } else if (DbTypeEnum.ELASTICSEARCH.equals(dbType)) {
                doElasticsearchMonitor(wsSession, nodeEntity);
            } else {
                throw new OpsException("Unsupported database type for monitoring: " + dbType);
            }
        } catch (Exception e) {
            log.error("Monitor {} occurred exception, node id: {}", dbType, nodeEntity.getClusterNodeId(), e);
            if (wsSession.getSession().isOpen()) {
                HashMap<String, String> monitorResult = new HashMap<>();
                monitorResult.put("status", "error");
                monitorResult.put("error", e.getClass().getName() + e.getMessage());
                wsUtil.sendText(wsSession, JSON.toJSONString(monitorResult));
            }
        }
    }

    private void doElasticsearchMonitor(WsSession wsSession, OpsNonJdbcDbClusterNodeEntity nodeEntity)
            throws IOException {
        String ip = nodeEntity.getIp();
        int port = Integer.parseInt(nodeEntity.getPort());
        String username = nodeEntity.getUsername();
        String password = nodeEntity.getPassword() != null ? encryptionUtils.decrypt(nodeEntity.getPassword()) : null;

        RestClient restClient = null;
        try {
            restClient = ElasticsearchUtils.createRestClient(ip, port, username, password);
            while (wsSession.getSession().isOpen()) {
                HashMap<String, String> monitorResult = new HashMap<>();
                monitorResult.put("status", "success");
                Map<String, Object> healthInfo = ElasticsearchUtils.healthInfo(restClient);
                Object status = healthInfo.get("status");
                if (status != null) {
                    monitorResult.put("clusterStatus", status.toString());
                } else {
                    monitorResult.put("clusterStatus", "");
                }
                Object unassignedShards = healthInfo.get("unassigned_shards");
                if (unassignedShards != null) {
                    monitorResult.put("unassigned_shards", unassignedShards.toString());
                } else {
                    monitorResult.put("unassigned_shards", "");
                }
                Object activeShardsPercentAsNumber = healthInfo.get("active_shards_percent_as_number");
                if (activeShardsPercentAsNumber != null) {
                    monitorResult.put("active_shards_percent_as_number", activeShardsPercentAsNumber.toString());
                } else {
                    monitorResult.put("active_shards_percent_as_number", "");
                }

                wsUtil.sendText(wsSession, JSON.toJSONString(monitorResult));

                ThreadUtil.safeSleep(5000L);
            }
        } finally {
            if (restClient != null) {
                try {
                    restClient.close();
                } catch (IOException e) {
                    log.warn("Close elasticsearch rest client occurred exception");
                }
            }
        }
    }

    private void doMilvusMonitor(WsSession wsSession, OpsNonJdbcDbClusterNodeEntity nodeEntity) {
        String ip = nodeEntity.getIp();
        int port = Integer.parseInt(nodeEntity.getPort());
        String username = nodeEntity.getUsername();
        String password = nodeEntity.getPassword() != null ? encryptionUtils.decrypt(nodeEntity.getPassword()) : null;

        MilvusClientV2 milvusClientV2 = null;
        try {
            milvusClientV2 = MilvusUtils.createMilvusClientV2(ip, port, null, username, password);
            while (wsSession.getSession().isOpen()) {
                HashMap<String, String> monitorResult = new HashMap<>();
                monitorResult.put("status", "success");
                monitorResult.put("collectionNum", collectionNum(milvusClientV2));
                monitorResult.put("apiResponseDelay", apiResponseDelay(milvusClientV2));

                wsUtil.sendText(wsSession, JSON.toJSONString(monitorResult));

                try {
                    TimeUnit.SECONDS.sleep(5L);
                } catch (InterruptedException e) {
                    log.warn("Monitor milvus cluster node thread is interrupted");
                }
            }
        } finally {
            if (milvusClientV2 != null) {
                MilvusUtils.closeMilvusClientV2(milvusClientV2);
            }
        }
    }

    private String collectionNum(MilvusClientV2 milvusClientV2) {
        int collectionNum = milvusClientV2.listCollections().getCollectionInfos().size();
        return String.valueOf(collectionNum);
    }

    private String apiResponseDelay(MilvusClientV2 milvusClientV2) {
        long start = System.currentTimeMillis();
        milvusClientV2.hasCollection(HasCollectionReq.builder().collectionName("test").build());
        long delay = System.currentTimeMillis() - start;
        return String.valueOf(delay);
    }

    private boolean pingMilvus(JdbcDbClusterNodeInputDto clusterNodeInput) {
        URL url = MilvusUtils.parseUrl(clusterNodeInput.getUrl());
        String ip = url.getHost();
        int port = url.getPort();
        String username = clusterNodeInput.getUsername();
        String password = clusterNodeInput.getPassword() != null
                ? encryptionUtils.decrypt(clusterNodeInput.getPassword()) : null;
        MilvusClientV2 milvusClientV2 = null;
        try {
            milvusClientV2 = MilvusUtils.createMilvusClientV2(ip, port, null, username, password);
        } catch (Exception e) {
            log.error("Milvus health check failed", e);
            throw new OpsException(e.getClass().getName() + e.getMessage());
        } finally {
            if (milvusClientV2 != null) {
                MilvusUtils.closeMilvusClientV2(milvusClientV2);
            }
        }
        return true;
    }

    private boolean pingElasticsearch(JdbcDbClusterNodeInputDto clusterNodeInput) {
        URL url = ElasticsearchUtils.parseUrl(clusterNodeInput.getUrl());
        String ip = url.getHost();
        int port = url.getPort();
        String username = clusterNodeInput.getUsername();
        String password = clusterNodeInput.getPassword();
        RestClient restClient = null;
        try {
            if (StrUtil.isEmpty(username) || StrUtil.isEmpty(password)) {
                restClient = ElasticsearchUtils.createRestClient(ip, port, null, null);
            } else {
                restClient = ElasticsearchUtils.createRestClient(ip, port, username, encryptionUtils.decrypt(password));
            }
            int healthStatusCode = ElasticsearchUtils.healthStatusCode(restClient);
            if (healthStatusCode < 200 || healthStatusCode >= 300) {
                log.error("Elasticsearch health check failed, status code: {}", healthStatusCode);
                throw new OpsException("Elasticsearch health check failed, status code: " + healthStatusCode);
            }
        } catch (IOException e) {
            log.error("Elasticsearch health check failed", e);
            throw new OpsException(e.getClass().getName() + e.getMessage());
        } finally {
            if (restClient != null) {
                try {
                    ElasticsearchUtils.closeClient(restClient);
                } catch (IOException e) {
                    log.debug("Elasticsearch close client failed", e);
                }
            }
        }
        return true;
    }
}
