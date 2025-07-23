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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcInfo;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcMonitorVO;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ops.JdbcUtil;
import org.opengauss.admin.common.utils.ops.WsUtil;
import org.opengauss.admin.system.mapper.ops.OpsJdbcDbClusterNodeMapper;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
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
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2023/1/13 11:09
 **/
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
    private IOpsClusterService opsClusterService;
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
            opsJdbcDbClusterService.del(clusterNodeEntity.getClusterId());
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
    public void delByClusterId(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            return;
        }

        LambdaQueryWrapper<OpsJdbcDbClusterNodeEntity> queryWrapper = Wrappers.lambdaQuery(OpsJdbcDbClusterNodeEntity.class)
                .eq(OpsJdbcDbClusterNodeEntity::getClusterId, clusterId);

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
        } catch (Exception e) {
            log.error("jdbc ping get link exception", e);
            res = false;
        }
        return res;
    }

    @Override
    public Map<String, Object> monitor(String clusterNodeId, String businessId) {
        Map<String, Object> res = new HashMap<>();
        res.put("res", true);

        OpsJdbcDbClusterNodeEntity clusterNodeEntity = getById(clusterNodeId);
        if (Objects.isNull(clusterNodeEntity)) {
            res.put("msg", "Node information not found");
            res.put("res", false);
            return res;
        }

        String clusterId = clusterNodeEntity.getClusterId();
        OpsJdbcDbClusterEntity clusterEntity = opsJdbcDbClusterService.getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            res.put("msg", "Cluster information not found");
            res.put("res", false);
            return res;
        }

        Connection connection = null;
        try {
            connection = JdbcUtil.getConnection(clusterNodeEntity.getUrl(), clusterNodeEntity.getUsername(),
                encryptionUtils.decrypt(clusterNodeEntity.getPassword()));
            if (Objects.isNull(connection) || connection.isClosed()) {
                res.put("msg", "JDBC connection failed");
                res.put("res", false);
                return res;
            }
        } catch (Exception e) {
            res.put("msg", "JDBC connection failed");
            res.put("res", false);
            return res;
        }

        WsSession wsSession = wsConnectorManager.getSession(businessId).orElseThrow(() -> new OpsException("response session does not exist"));

        Connection finalConnection = connection;
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            try {
                doMonitor(wsSession, finalConnection, clusterEntity.getDbType());
            } finally {
                if (Objects.nonNull(finalConnection)) {
                    try {
                        finalConnection.close();
                    } catch (SQLException ignore) {

                    }
                }

                wsUtil.close(wsSession);
            }
        });
        TaskManager.registry(businessId, future);
        return res;
    }

    private void doMonitor(WsSession wsSession, Connection connection, DbTypeEnum dbType) {
        while (wsSession.getSession().isOpen()) {
            JdbcMonitorVO jdbcMonitorVO = new JdbcMonitorVO();

            if (dbType == DbTypeEnum.MYSQL) {
                doMonitorMysql(connection, jdbcMonitorVO);
            } else if (dbType == DbTypeEnum.OPENGAUSS) {
                doMonitorOpenGauss(connection, jdbcMonitorVO);
            } else {
                doMonitorPostgresql(connection, jdbcMonitorVO);
            }

            wsUtil.sendText(wsSession, JSON.toJSONString(jdbcMonitorVO));

            try {
                TimeUnit.SECONDS.sleep(5L);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
        }
    }

    private void doMonitorPostgresql(Connection connection, JdbcMonitorVO jdbcMonitorVO) {
        doMonitorOpenGauss(connection, jdbcMonitorVO);
    }

    private void doMonitorOpenGauss(Connection connection, JdbcMonitorVO jdbcMonitorVO) {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        threadPoolTaskExecutor.submit(() -> {
            try {
                jdbcMonitorVO.setLockNum(opsClusterService.lock(connection));
            } finally {
                countDownLatch.countDown();
            }
        });

        threadPoolTaskExecutor.submit(() -> {
            try {
                jdbcMonitorVO.setSessionNum(opsClusterService.session(connection));
            } finally {
                countDownLatch.countDown();
            }
        });

        threadPoolTaskExecutor.submit(() -> {
            try {
                jdbcMonitorVO.setConnNum(opsClusterService.connectNum(connection));
            } finally {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("waiting for thread to be interrupted", e);
            throw new OpsException("monitor error");
        }
    }

    private void doMonitorMysql(Connection connection, JdbcMonitorVO jdbcMonitorVO) {
        CountDownLatch countDownLatch = new CountDownLatch(6);

        threadPoolTaskExecutor.submit(() -> {
            try {
                jdbcMonitorVO.setRole(role(connection));
            } finally {
                countDownLatch.countDown();
            }
        });

        threadPoolTaskExecutor.submit(() -> {
            try {
                jdbcMonitorVO.setConnNum(connNum(connection));
            } finally {
                countDownLatch.countDown();
            }
        });

        threadPoolTaskExecutor.submit(() -> {
            try {
                jdbcMonitorVO.setQps(qps(connection));
            } finally {
                countDownLatch.countDown();
            }
        });

        threadPoolTaskExecutor.submit(() -> {
            try {
                jdbcMonitorVO.setTps(tps(connection));
            } finally {
                countDownLatch.countDown();
            }
        });

        threadPoolTaskExecutor.submit(() -> {
            try {
                jdbcMonitorVO.setMemoryUsed(memoryUsed(connection));
            } finally {
                countDownLatch.countDown();
            }
        });

        threadPoolTaskExecutor.submit(() -> {
            try {
                jdbcMonitorVO.setTableSpaceUsed(tableSpaceUsed(connection));
            } finally {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("waiting for thread to be interrupted", e);
            throw new OpsException("monitor error");
        }
    }

    private String tableSpaceUsed(Connection connection) {
        String sql = "SELECT SUM( table_schema_size.table_schema_size ) AS 'tableSpaceUsed' FROM ( SELECT table_schema, SUM( data_length + index_length ) AS table_schema_size FROM information_schema.TABLES GROUP BY table_schema ) table_schema_size";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getString("tableSpaceUsed");
            }
        } catch (Exception e) {
            log.error("Failed to get tablespace", e);
        }

        throw new OpsException("Failed to get tablespace");
    }

    private String memoryUsed(Connection connection) {
        String sql = "SELECT (@@key_buffer_size + @@innodb_buffer_pool_size + @@innodb_log_buffer_size + @@max_connections * ( @@read_buffer_size + @@read_rnd_buffer_size + @@sort_buffer_size + @@join_buffer_size + @@binlog_cache_size + @@thread_stack + @@tmp_table_size )) AS 'memoryUsed'";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getString("memoryUsed");
            }
        } catch (Exception e) {
            log.error("Failed to get memoryUsed", e);
        }

        throw new OpsException("Failed to get memoryUsed");
    }

    /**
     * TPS = (Com_commit+Com_rollback)/Uptime
     *
     * @param connection
     * @return
     */
    private String tps(Connection connection) {
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
        } catch (Exception e) {
            log.error("Failed to get tps", e);
        }
        throw new OpsException("Failed to get tps");
    }

    /**
     * QPS = Questions/Uptime
     *
     * @param connection
     * @return
     */
    private String qps(Connection connection) {
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
        } catch (Exception e) {
            log.error("Failed to get qps", e);
        }
        throw new OpsException("Failed to get qps");
    }

    private String connNum(Connection connection) {
        String sql = "SHOW STATUS LIKE 'Threads_connected'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getString("Value");
            }
        } catch (Exception e) {
            log.error("Failed to get connection number", e);
        }

        throw new OpsException("Failed to get connection number");
    }

    private ClusterRoleEnum role(Connection connection) {
        String sql = "SHOW SLAVE STATUS";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return ClusterRoleEnum.SLAVE;
            } else {
                return ClusterRoleEnum.MASTER;
            }
        } catch (Exception e) {
            log.error("Failed to get role", e);
        }

        throw new OpsException("Failed to get role");
    }
}
