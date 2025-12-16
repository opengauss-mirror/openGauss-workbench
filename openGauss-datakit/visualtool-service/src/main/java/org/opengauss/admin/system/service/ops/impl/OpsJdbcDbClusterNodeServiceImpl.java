/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * OpsJdbcDbClusterNodeServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/impl/OpsJdbcDbClusterNodeServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcInfo;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ops.JdbcUtil;
import org.opengauss.admin.common.utils.ops.WsUtil;
import org.opengauss.admin.system.mapper.ops.OpsJdbcDbClusterNodeMapper;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2023/1/13 11:09
 **/
@Slf4j
@Service
public class OpsJdbcDbClusterNodeServiceImpl extends ServiceImpl<OpsJdbcDbClusterNodeMapper, OpsJdbcDbClusterNodeEntity> implements IOpsJdbcDbClusterNodeService {
    @Autowired
    private IOpsJdbcDbClusterService opsJdbcDbClusterService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private WsConnectorManager wsConnectorManager;
    @Autowired
    private WsUtil wsUtil;
    @Autowired
    private EncryptionUtils encryptionUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(String clusterNodeId) {
        OpsJdbcDbClusterNodeEntity clusterNodeEntity = getById(clusterNodeId);
        if (Objects.isNull(clusterNodeEntity)) {
            throw new OpsException("Node information does not exist");
        }

        String clusterId = clusterNodeEntity.getClusterId();
        List<OpsJdbcDbClusterNodeEntity> nodes = listNodeByClusterId(clusterId);
        if (CollUtil.isNotEmpty(nodes) && nodes.size() == 1) {
            opsJdbcDbClusterService.delete(clusterNodeEntity.getClusterId());
        } else {
            removeById(clusterNodeId);
        }
    }

    private List<OpsJdbcDbClusterNodeEntity> listNodeByClusterId(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterNodeEntity.class)
                .eq(OpsJdbcDbClusterNodeEntity::getClusterId, clusterId);
        return list(queryWrapper);
    }

    @Override
    public OpsJdbcDbClusterNodeEntity getClusterNodeByIpAndPort(String ip, String port, String username) {
        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterNodeEntity.class)
                .eq(OpsJdbcDbClusterNodeEntity::getIp, ip)
                .eq(OpsJdbcDbClusterNodeEntity::getPort, port)
                .eq(StrUtil.isNotEmpty(username), OpsJdbcDbClusterNodeEntity::getUsername, username);
        return getOne(queryWrapper, false);
    }

    @Override
    public Set<String> fuzzyQueryClusterIdsByIp(String name) {
        Set<String> res = new HashSet<>();
        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterNodeEntity.class)
                .like(OpsJdbcDbClusterNodeEntity::getIp, name)
                .select(OpsJdbcDbClusterNodeEntity::getClusterId);

        List<OpsJdbcDbClusterNodeEntity> list = list(queryWrapper);
        if (CollUtil.isNotEmpty(list)) {
            res.addAll(list.stream().map(OpsJdbcDbClusterNodeEntity::getClusterId).collect(Collectors.toSet()));
        }
        return res;
    }

    @Override
    public Map<String, List<OpsJdbcDbClusterNodeEntity>> mapClusterNodesByClusterId(Set<String> clusterIds) {
        if (CollUtil.isEmpty(clusterIds)) {
            return Collections.emptyMap();
        }

        Map<String, List<OpsJdbcDbClusterNodeEntity>> res = new HashMap<>();

        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterNodeEntity.class)
                .in(OpsJdbcDbClusterNodeEntity::getClusterId, clusterIds)
                .orderByDesc(OpsJdbcDbClusterNodeEntity::getCreateTime);

        List<OpsJdbcDbClusterNodeEntity> list = list(queryWrapper);
        if (CollUtil.isNotEmpty(list)) {
            res = list.stream().collect(Collectors.groupingBy(OpsJdbcDbClusterNodeEntity::getClusterId));
        }

        return res;
    }

    @Override
    public void deleteByClusterId(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            return;
        }

        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OpsJdbcDbClusterNodeEntity::getClusterId, clusterId);
        remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByClusterIds(List<String> clusterIds) {
        if (CollUtil.isEmpty(clusterIds)) {
            return;
        }

        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OpsJdbcDbClusterNodeEntity::getClusterId, clusterIds);
        remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String clusterNodeId, JdbcDbClusterNodeInputDto clusterNodeInput) {
        OpsJdbcDbClusterNodeEntity clusterNodeEntity = getById(clusterNodeId);
        if (Objects.isNull(clusterNodeEntity)) {
            throw new OpsException("Cluster node information does not exist");
        }

        String url = clusterNodeInput.getUrl();
        JdbcInfo jdbcInfo = JdbcUtil.parseUrl(url);

        OpsJdbcDbClusterNodeEntity opsJdbcDbClusterNodeEntity = new OpsJdbcDbClusterNodeEntity();
        opsJdbcDbClusterNodeEntity.setClusterNodeId(clusterNodeEntity.getClusterNodeId());
        opsJdbcDbClusterNodeEntity.setClusterId(clusterNodeEntity.getClusterId());
        opsJdbcDbClusterNodeEntity.setName(clusterNodeInput.getName());
        opsJdbcDbClusterNodeEntity.setIp(jdbcInfo.getIp());
        opsJdbcDbClusterNodeEntity.setPort(jdbcInfo.getPort());
        opsJdbcDbClusterNodeEntity.setUsername(clusterNodeInput.getUsername());
        opsJdbcDbClusterNodeEntity.setPassword(clusterNodeInput.getPassword());
        opsJdbcDbClusterNodeEntity.setUrl(url);
        opsJdbcDbClusterNodeEntity.setUpdateTime(new Date());

        updateById(opsJdbcDbClusterNodeEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(String clusterId, JdbcDbClusterNodeInputDto clusterNodeInput) {
        OpsJdbcDbClusterEntity clusterEntity = opsJdbcDbClusterService.getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        if (StrUtil.isEmpty(clusterNodeInput.getUsername())) {
            throw new OpsException("Username can not be empty");
        }

        if (StrUtil.isEmpty(clusterNodeInput.getPassword())) {
            throw new OpsException("password can not be empty");
        }

        String url = clusterNodeInput.getUrl();
        JdbcInfo jdbcInfo = JdbcUtil.parseUrl(url);

        OpsJdbcDbClusterNodeEntity clusterNodeEntity = getClusterNodeByIpAndPort(jdbcInfo.getIp(),
                jdbcInfo.getPort(), clusterNodeInput.getUsername());
        if (Objects.nonNull(clusterNodeEntity)) {
            throw new OpsException("Cluster node information already exists");
        }

        OpsJdbcDbClusterNodeEntity opsJdbcDbClusterNodeEntity = new OpsJdbcDbClusterNodeEntity();

        opsJdbcDbClusterNodeEntity.setClusterId(clusterEntity.getClusterId());
        opsJdbcDbClusterNodeEntity.setName(clusterNodeInput.getName());
        opsJdbcDbClusterNodeEntity.setIp(jdbcInfo.getIp());
        opsJdbcDbClusterNodeEntity.setPort(jdbcInfo.getPort());
        opsJdbcDbClusterNodeEntity.setUsername(clusterNodeInput.getUsername());
        opsJdbcDbClusterNodeEntity.setPassword(clusterNodeInput.getPassword());
        opsJdbcDbClusterNodeEntity.setUrl(url);
        opsJdbcDbClusterNodeEntity.setCreateTime(new Date());

        save(opsJdbcDbClusterNodeEntity);

        if (Objects.nonNull(clusterNodeInput.getDeployType())) {
            clusterEntity.setDeployType(clusterNodeInput.getDeployType());
            opsJdbcDbClusterService.updateById(clusterEntity);
        } else {
            throw new OpsException("deployment type is empty");
        }
    }

    @Override
    public boolean ping(JdbcDbClusterNodeInputDto clusterNodeInput) {
        boolean res = false;
        try (Connection connection = JdbcUtil.getConnection(clusterNodeInput.getUrl(), clusterNodeInput.getUsername(),
                encryptionUtils.decrypt(clusterNodeInput.getPassword()))) {
            if (Objects.nonNull(connection)) {
                res = true;
            }
        } catch (SQLException e) {
            log.error("jdbc ping get link exception", e);
            throw new OpsException(e.getClass().getName() + e.getMessage());
        }
        return res;
    }

    @Override
    public List<OpsJdbcDbClusterNodeEntity> listByClusterIds(List<String> clustersIds) {
        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (clustersIds != null && !clustersIds.isEmpty()) {
            queryWrapper.in(OpsJdbcDbClusterNodeEntity::getClusterId, clustersIds);
        }

        return list(queryWrapper);
    }

    @Override
    public List<OpsJdbcDbClusterNodeEntity> listByIpAndClusterIds(String ip, List<String> clusterIds) {
        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (!StrUtil.isEmpty(ip)) {
            queryWrapper.like(OpsJdbcDbClusterNodeEntity::getIp, ip);
        }
        if (clusterIds != null && !clusterIds.isEmpty()) {
            queryWrapper.in(OpsJdbcDbClusterNodeEntity::getClusterId, clusterIds);
        }

        return list(queryWrapper);
    }

    @Override
    public OpsJdbcDbClusterNodeEntity getOneByClusterId(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            throw new IllegalArgumentException("clusterId can not be empty");
        }

        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OpsJdbcDbClusterNodeEntity::getClusterId, clusterId);
        OpsJdbcDbClusterNodeEntity nodeEntity = getOne(queryWrapper);
        if (nodeEntity == null) {
            return null;
        }

        return nodeEntity;
    }

    @Override
    public void monitor(DbTypeEnum dbType, String clusterNodeId, String businessId) {
        if (StrUtil.isEmpty(clusterNodeId) || StrUtil.isEmpty(businessId)) {
            throw new IllegalArgumentException("Cluster node id and business id cannot be empty");
        }

        OpsJdbcDbClusterNodeEntity clusterNodeEntity = getById(clusterNodeId);
        if (clusterNodeEntity == null) {
            throw new OpsException("Jdbc cluster node does not exist, clusterNodeId: " + clusterNodeId);
        }

        String clusterId = clusterNodeEntity.getClusterId();
        OpsJdbcDbClusterEntity clusterEntity = opsJdbcDbClusterService.getById(clusterId);
        if (clusterEntity == null) {
            throw new OpsException("Jdbc cluster does not exist, clusterId: " + clusterId);
        }
        if (!clusterEntity.getDbType().equals(dbType)) {
            throw new OpsException("Jdbc cluster database type does not match, dbType: " + dbType);
        }

        WsSession wsSession = wsConnectorManager.getSession(businessId).orElseThrow(() ->
                new OpsException("response session[" + businessId + "] does not exist"));

        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            try {
                doMonitor(wsSession, clusterNodeEntity, dbType);
            } finally {
                wsUtil.close(wsSession);
            }
        });
        TaskManager.registry(businessId, future);
    }

    private void doMonitor(WsSession wsSession, OpsJdbcDbClusterNodeEntity nodeEntity, DbTypeEnum dbType) {
        try (Connection connection = JdbcUtil.getConnection(nodeEntity.getUrl(), nodeEntity.getUsername(),
                encryptionUtils.decrypt(nodeEntity.getPassword()))) {
            if (DbTypeEnum.MYSQL.equals(dbType)) {
                doMysqlMonitor(wsSession, connection);
            } else if (DbTypeEnum.OPENGAUSS.equals(dbType) || DbTypeEnum.POSTGRESQL.equals(dbType)) {
                doOpenGaussMonitor(wsSession, connection);
            } else {
                throw new IllegalArgumentException("Unsupported database type to monitor, dbType: " + dbType);
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

    private void doMysqlMonitor(WsSession wsSession, Connection connection) throws SQLException {
        HashMap<String, String> monitorResult = new HashMap<>();
        while (wsSession.getSession().isOpen()) {
            monitorResult.put("status", "success");
            monitorResult.put("connNum", connNum(connection));
            monitorResult.put("qps", qps(connection));
            monitorResult.put("tps", tps(connection));
            monitorResult.put("memoryUsed", memoryUsed(connection));
            monitorResult.put("tableSpaceUsed", tableSpaceUsed(connection));

            wsUtil.sendText(wsSession, JSON.toJSONString(monitorResult));

            try {
                TimeUnit.SECONDS.sleep(5L);
            } catch (InterruptedException e) {
                log.warn("Monitor MySQL cluster node thread is interrupted");
            }
        }
    }

    private void doOpenGaussMonitor(WsSession wsSession, Connection connection) throws SQLException {
        HashMap<String, String> monitorResult = new HashMap<>();
        while (wsSession.getSession().isOpen()) {
            monitorResult.put("status", "success");
            monitorResult.put("lockNum", lock(connection));
            monitorResult.put("connNum", connectNum(connection));
            monitorResult.put("sessionNum", session(connection));

            wsUtil.sendText(wsSession, JSON.toJSONString(monitorResult));

            try {
                TimeUnit.SECONDS.sleep(5L);
            } catch (InterruptedException e) {
                log.warn("Monitor openGauss/PostgreSQL cluster node thread is interrupted");
            }
        }
    }

    private String connectNum(Connection connection) throws SQLException {
        String sql = "SELECT count(*) FROM (SELECT pg_stat_get_backend_idset() AS backendid) AS s";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getString("count");
            }
        }
        throw new SQLException("Failed to query the number of connections");
    }

    private String session(Connection connection) throws SQLException {
        String sql = "SELECT count(*) FROM pg_stat_activity";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getString("count");
            }
        }
        throw new SQLException("Failed to query the number of sessions");
    }

    private String lock(Connection connection) throws SQLException {
        String sql = "SELECT count(*) FROM pg_locks";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getString("count");
            }
        }
        throw new SQLException("Failed to query the number of locks");
    }

    private String tableSpaceUsed(Connection connection) throws SQLException {
        String sql = "SELECT SUM( table_schema_size.table_schema_size ) AS 'tableSpaceUsed' FROM "
                + "( SELECT table_schema, SUM( data_length + index_length ) AS table_schema_size FROM "
                + "information_schema.TABLES GROUP BY table_schema ) table_schema_size";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString("tableSpaceUsed");
            }
        }
        throw new SQLException("Failed to query the table space used");
    }

    private String memoryUsed(Connection connection) throws SQLException {
        String sql = "SELECT (@@key_buffer_size + @@innodb_buffer_pool_size + @@innodb_log_buffer_size + "
                + "@@max_connections * ( @@read_buffer_size + @@read_rnd_buffer_size + @@sort_buffer_size + "
                + "@@join_buffer_size + @@binlog_cache_size + @@thread_stack + @@tmp_table_size )) AS 'memoryUsed'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString("memoryUsed");
            }
        }
        throw new SQLException("Failed to query the memory used");
    }

    private String tps(Connection connection) throws SQLException {
        String commitSql = "show global status like 'Com_commit'";
        String rollbackSql = "show global status like 'Com_rollback'";
        String uptimeSql = "show global status like 'Uptime'";

        try (PreparedStatement commitPreparedStatement = connection.prepareStatement(commitSql);
             PreparedStatement rollbackPreparedStatement = connection.prepareStatement(rollbackSql);
             PreparedStatement uptimePreparedStatement = connection.prepareStatement(uptimeSql);
             ResultSet commitResultSet = commitPreparedStatement.executeQuery();
             ResultSet rollbackResultSet = rollbackPreparedStatement.executeQuery();
             ResultSet uptimeResultSet = uptimePreparedStatement.executeQuery()) {

            if (commitResultSet.next() && rollbackResultSet.next() && uptimeResultSet.next()) {
                long commit = commitResultSet.getLong("Value");
                long rollback = rollbackResultSet.getLong("Value");
                long uptime = uptimeResultSet.getLong("Value");

                return Long.valueOf((commit + rollback) / uptime).toString();
            }
        }
        throw new SQLException("Failed to query the tps");
    }

    private String qps(Connection connection) throws SQLException {
        String questionsSql = "show global status like 'Questions'";
        String uptimeSql = "show global status like 'Uptime'";

        try (PreparedStatement questionsPreparedStatement = connection.prepareStatement(questionsSql);
             PreparedStatement uptimePreparedStatement = connection.prepareStatement(uptimeSql);
             ResultSet questionsResultSet = questionsPreparedStatement.executeQuery();
             ResultSet uptimeResultSet = uptimePreparedStatement.executeQuery()) {

            if (questionsResultSet.next() && uptimeResultSet.next()) {
                long questions = questionsResultSet.getLong("Value");
                long uptime = uptimeResultSet.getLong("Value");

                return Long.valueOf(questions / uptime).toString();
            }
        }
        throw new SQLException("Failed to query the qps");
    }

    private String connNum(Connection connection) throws SQLException {
        String sql = "SHOW STATUS LIKE 'Threads_connected'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString("Value");
            }
        }
        throw new SQLException("Failed to query the number of connections");
    }
}
