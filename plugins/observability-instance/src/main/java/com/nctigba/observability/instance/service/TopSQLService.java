/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.constants.DatabaseType;
import com.nctigba.observability.instance.dto.topsql.TopSQLInfoReq;
import com.nctigba.observability.instance.dto.topsql.TopSQLListReq;
import com.nctigba.observability.instance.dto.topsql.TopSQLNowReq;
import com.nctigba.observability.instance.factory.TopSQLHandlerFactory;
import com.nctigba.observability.instance.handler.topsql.TopSQLHandler;
import com.nctigba.observability.instance.mapper.TopSqlMapper;
import com.nctigba.observability.instance.model.InstanceNodeInfo;
import com.nctigba.observability.instance.service.TopSQLService.waitEvent.event;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * TopSQL services
 * </p>
 *
 * @author zhanggr@vastdata.com.cn
 * @since 2022/9/15 15:46
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TopSQLService {
    private final TopSQLHandlerFactory topSQLHandlerFactory;
    private final ClusterManager clusterManager;
    private final TopSqlMapper topSqlMapper;

    public boolean testConnection(String nodeId) {
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(nodeId);
        try {
            return handler.getConnection(instanceNodeInfo) != null;
        } catch (Exception e) {
            log.error("test connection exception: {}", e.getMessage());
            return false;
        }
    }

    public List<JSONObject> getTopSQLList(TopSQLListReq topSQLListReq) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLListReq.getId());
        // query TopSQL list
        return handler.getTopSQLList(instanceNodeInfo, topSQLListReq);
    }

    public List<JSONObject> getTopSQLNow(TopSQLNowReq topSQLNowReq) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLNowReq.getId());
        // query TopSQL list
        return handler.getTopSQLNow(instanceNodeInfo, topSQLNowReq);
    }

    public JSONObject getStatisticalInfo(TopSQLInfoReq topSQLDetailReq) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLDetailReq.getId());
        // query TopSQL statistical information
        return handler.getStatisticalInfo(instanceNodeInfo, topSQLDetailReq.getSqlId());
    }

    public JSONObject getExecutionPlan(TopSQLInfoReq topSQLPlanReq, String type) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLPlanReq.getId());
        // query TopSQL statistical information
        return handler.getExecutionPlan(instanceNodeInfo, topSQLPlanReq.getSqlId(), type);
    }

    public List<JSONObject> getPartitionList(TopSQLInfoReq topSQLPartitionReq) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLPartitionReq.getId());
        // query TopSQL statistical information
        return handler.getPartitionList(instanceNodeInfo, topSQLPartitionReq.getSqlId());
    }

    public List<String> getIndexAdvice(TopSQLInfoReq topSQLIndexReq) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLIndexReq.getId());
        // query TopSQL statistical information
        return handler.getIndexAdvice(instanceNodeInfo, topSQLIndexReq.getSqlId());
    }

    public JSONObject getObjectInfo(TopSQLInfoReq topSQLObjectReq) {
        // get handler
        TopSQLHandler handler = topSQLHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType());
        // query node info
        InstanceNodeInfo instanceNodeInfo = queryNodeInfo(topSQLObjectReq.getId());
        // query TopSQL statistical information
        return handler.getObjectInfo(instanceNodeInfo, topSQLObjectReq.getSqlId());
    }

    /**
     * Query instance node information
     *
     * @param nodeId instance node id
     * @return Instance node information
     */
    public InstanceNodeInfo queryNodeInfo(String nodeId) {
        OpsClusterNodeVO opsClusterNode = clusterManager.getOpsNodeById(nodeId);
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

    public List<Map<String, Object>> waitEvent(String nodeId, String sqlId) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            clusterManager.setCurrentDatasource(nodeId, null);
            String table = topSqlMapper.waitEvent(sqlId);
            if (StrUtil.isBlank(table)) {
                return Collections.emptyList();
            }
            String[] lines = table.split(",");
            waitEvent pre = null;
            for (String string : lines) {
                var curr = new waitEvent(string);
                if (pre != null && pre.getE() == event.LOCK_START && curr.getE() == event.LOCK_END) {
                    list.add(Map.of("starttime", pre.getTimeStr(), "lockType", pre.getLockType(), "waittime",
                            (Duration.between(pre.getTime(), curr.getTime()).toNanos() / 1000)));
                    pre = null;
                    continue;
                }
                pre = curr;
            }
        } finally {
            clusterManager.pool();
        }
        return list;
    }

    @Data
    @Generated
    public static class waitEvent {
        private static final DateTimeFormatter[] FORMATTERS = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.Sx")
        };
        private String index;
        private event e;
        private LocalDateTime time;
        private String timeStr;
        private String id;
        private String lockType;

        public waitEvent(String string) {
            String[] str = string.split("'\\s+'");
            this.index = str[0].replaceAll("'", "");
            this.e = event.valueOf(str[1].replaceAll("'", ""));
            for (int i = 0; i < FORMATTERS.length; i++) {
                try {
                    this.time = LocalDateTime.parse(str[2].replaceAll("'", ""), FORMATTERS[i]);
                    break;
                } catch (DateTimeParseException e) {
                    continue;
                }
            }
            this.timeStr = str[2];
            if (str.length > 3)
                this.id = str[3].replaceAll("'", "");
            if (str.length > 4)
                this.lockType = str[4].replaceAll("'", "");
        }

        public enum event {
            LOCK_START,
            LOCK_END,
            LOCK_RELEASE
        }
    }
}