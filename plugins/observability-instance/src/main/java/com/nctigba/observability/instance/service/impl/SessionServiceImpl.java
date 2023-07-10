/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.service.impl;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.common.web.exception.InstanceException;
import com.nctigba.observability.instance.constants.DatabaseType;
import com.nctigba.observability.instance.dto.session.DetailStatisticDto;
import com.nctigba.observability.instance.factory.SessionHandlerFactory;
import com.nctigba.observability.instance.handler.session.SessionHandler;
import com.nctigba.observability.instance.model.InstanceNodeInfo;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.SessionService;

import cn.hutool.core.thread.ThreadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final SessionHandlerFactory sessionHandlerFactory;
    private final ClusterManager opsFacade;

    @Override
    public JSONObject detailGeneral(String id, String sessionid) {
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(id);
        // get handler
        SessionHandler handler = sessionHandlerFactory.getInstance(instanceNodeInfo.getDbType());
        Connection connection = handler.getConnection(instanceNodeInfo);
        JSONObject general;
        try {
            // query TopSQL list
            general = handler.detailGeneral(connection, sessionid);
        } catch (Exception e) {
            throw new CustomException("", e);
        } finally {
            handler.close(connection);
        }
        return general;
    }

    @Override
    public List<DetailStatisticDto> detailStatistic(String id, String sessionid) {
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(id);
        // get handler
        SessionHandler handler = sessionHandlerFactory.getInstance(instanceNodeInfo.getDbType());
        Connection connection = handler.getConnection(instanceNodeInfo);
        List<DetailStatisticDto> statistic;
        try {
            // query TopSQL list
            statistic = handler.detailStatistic(connection, sessionid);
        } catch (Exception e) {
            throw new CustomException("", e);
        } finally {
            handler.close(connection);
        }
        return statistic;
    }

    @Override
    public List<JSONObject> detailWaiting(String id, String sessionid) {
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(id);
        // get handler
        SessionHandler handler = sessionHandlerFactory.getInstance(instanceNodeInfo.getDbType());
        Connection connection = handler.getConnection(instanceNodeInfo);
        List<JSONObject> detailWaiting;
        try {
            // query list
            detailWaiting = handler.detailWaiting(connection, sessionid);
        } catch (Exception e) {
            throw new CustomException("", e);
        } finally {
            handler.close(connection);
        }
        return detailWaiting;
    }

    @Override
    public List<JSONObject> detailBlockTree(String id, String sessionid) {
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(id);
        // get handler
        SessionHandler handler = sessionHandlerFactory.getInstance(instanceNodeInfo.getDbType());
        Connection connection = handler.getConnection(instanceNodeInfo);
        List<JSONObject> detailBlockTree;
        try {
            // query list
            detailBlockTree = handler.detailBlockTree(connection, sessionid);
        } catch (Exception e) {
            throw new CustomException("", e);
        } finally {
            handler.close(connection);
        }
        return detailBlockTree;
    }

    @Override
    public JSONObject simpleStatistic(String id) {
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(id);
        // get handler
        SessionHandler handler = sessionHandlerFactory.getInstance(instanceNodeInfo.getDbType());
        Connection connection = handler.getConnection(instanceNodeInfo);
        JSONObject simpleStatistic;
        try {
            // query list
            simpleStatistic = handler.simpleStatistic(connection);
        } catch (Exception e) {
            throw new CustomException("", e);
        } finally {
            handler.close(connection);
        }
        return simpleStatistic;
    }

    @Override
    public List<JSONObject> longTxc(String id) {
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(id);
        // get handler
        SessionHandler handler = sessionHandlerFactory.getInstance(instanceNodeInfo.getDbType());
        Connection connection = handler.getConnection(instanceNodeInfo);
        List<JSONObject> longTxc;
        try {
            // query list
            longTxc = handler.longTxc(connection);
        } catch (Exception e) {
            throw new CustomException("", e);
        } finally {
            handler.close(connection);
        }
        return longTxc;
    }

    @Override
    public HashMap<String, List<JSONObject>> blockAndLongTxc(String id) {
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(id);
        // get handler
        SessionHandler handler = sessionHandlerFactory.getInstance(instanceNodeInfo.getDbType());
        Connection connection = handler.getConnection(instanceNodeInfo);
        Future<List<JSONObject>> blockFuture = ThreadUtil.execAsync(() -> handler.detailBlockTree(connection, null));
        Future<List<JSONObject>> longTxcFuture = ThreadUtil.execAsync(() -> handler.longTxc(connection));
        List<JSONObject> blockTree;
        List<JSONObject> longTxc;
        try {
            blockTree = blockFuture.get();
            longTxc = longTxcFuture.get();
        } catch (Exception e) {
            log.error("", e);
            throw new CustomException("", e);
        } finally {
            handler.close(connection);
        }
        HashMap<String, List<JSONObject>> res = new HashMap<>();
        res.put("blockTree", blockTree);
        res.put("longTxc", longTxc);
        return res;
    }

    @SuppressWarnings({
            "rawtypes",
            "unchecked"
    })
    @Override
    public Map<String, Object> detail(String id, String sessionid) {
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(id);
        // get handler
        SessionHandler handler = sessionHandlerFactory.getInstance(instanceNodeInfo.getDbType());
        Connection connection = handler.getConnection(instanceNodeInfo);
        Map<String, Callable> callables = new HashMap<>();
        callables.put("general", () -> handler.detailGeneral(connection, sessionid));
        callables.put("statistic", () -> handler.detailStatistic(connection, sessionid));
        callables.put("blockTree", () -> handler.detailBlockTree(connection, sessionid));
        callables.put("waiting", () -> handler.detailWaiting(connection, sessionid));
        Map<String, Future> futures = new HashMap<>();
        for (Map.Entry<String, Callable> callable : callables.entrySet()) {
            futures.put(callable.getKey(), ThreadUtil.execAsync(callable.getValue()));
        }
        HashMap<String, Object> resMap = new HashMap<>();
        try {
            for (Map.Entry<String, Future> future : futures.entrySet()) {
                resMap.put(future.getKey(), future.getValue().get());
            }
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof InstanceException) {
                InstanceException instanceException = (InstanceException) e.getCause();
                log.error(instanceException.getMessage(), e);
                throw instanceException;
            }
            throw new InstanceException(e.getMessage(), e);
        } finally {
            handler.close(connection);
        }
        return resMap;
    }

    /**
     * Query instance node information
     *
     * @param nodeId instance node id
     * @return Instance node information
     */
    public InstanceNodeInfo queryNodeInfo(String nodeId) {
        OpsClusterNodeVO opsClusterNode = opsFacade.getOpsNodeById(nodeId);
        InstanceNodeInfo instanceNodeInfo = new InstanceNodeInfo();
        instanceNodeInfo.setId(opsClusterNode.getNodeId());
        instanceNodeInfo.setIp(opsClusterNode.getPublicIp());
        instanceNodeInfo.setPort(opsClusterNode.getDbPort());
        instanceNodeInfo.setDbName(opsClusterNode.getDbName());
        instanceNodeInfo.setDbUser(opsClusterNode.getDbUser());
        instanceNodeInfo.setDbUserPassword(opsClusterNode.getDbUserPassword());
        instanceNodeInfo.setDbType(DatabaseType.DEFAULT.getDbType());
        return instanceNodeInfo;
    }
}
