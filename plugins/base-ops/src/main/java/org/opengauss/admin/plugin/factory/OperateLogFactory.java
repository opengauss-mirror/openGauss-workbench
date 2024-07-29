/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * OperateLogFactory.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/factory/OperateLogFactory.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengauss.admin.framework.manager.AsyncManager;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterOperateLog;
import org.opengauss.admin.plugin.enums.ops.ClusterOperateTypeEnum;
import org.opengauss.admin.plugin.service.ops.IOpsClusterLogService;
import org.opengauss.admin.plugin.utils.SpringUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.TimerTask;

/**
 * OperateLogFactory
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
public class OperateLogFactory {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * save cluster create operate log
     *
     * @param clusterId cluster id
     */
    public static void operateCreate(String clusterId) {
        saveLog(logOperate(clusterId, ClusterOperateTypeEnum.CREATE, "新建集群任务"));
    }

    /**
     * save cluster update operate log
     *
     * @param clusterId cluster id
     */
    public static void operateUpdate(String clusterId) {
        saveLog(logOperate(clusterId, ClusterOperateTypeEnum.UPDATE, "修改集群任务"));
    }

    /**
     * save cluster confirm operate log
     *
     * @param clusterId cluster id
     */
    public static void operateConfirm(String clusterId) {
        saveLog(logOperate(clusterId, ClusterOperateTypeEnum.CONFIRM, "确认集群任务"));
    }

    /**
     * save cluster copy operate log
     *
     * @param clusterId cluster id
     */
    public static void operateCopy(String clusterId) {
        saveLog(logOperate(clusterId, ClusterOperateTypeEnum.COPY, "复制集群任务"));
    }

    /**
     * save cluster install operate log
     *
     * @param clusterId clusterId
     * @param content   content
     */
    public static void operateInstall(String clusterId, String content) {
        saveLog(logOperate(clusterId, ClusterOperateTypeEnum.INSTALL, content));
    }

    private static OpsClusterOperateLog logOperate(String clusterId, ClusterOperateTypeEnum operateType, Object content) {
        OpsClusterOperateLog operateLog = new OpsClusterOperateLog();
        operateLog.setClusterId(clusterId);
        operateLog.setOperateType(operateType);
        operateLog.setOperateLog(toString(content));
        operateLog.setOperateTime(LocalDateTime.now());
        return operateLog;
    }

    /**
     * save cluster check environment operate log
     *
     * @param clusterId     cluster id
     * @param clusterNodeId cluster node id
     * @param content       content
     */
    public static void operateCheckEnvironment(String clusterId, String clusterNodeId, Object content) {
        OpsClusterOperateLog operateLog = logOperate(clusterId, ClusterOperateTypeEnum.CHECK_ENVIRONMENT, content);
        operateLog.setClusterNodeId(clusterNodeId);
        saveLog(operateLog);
    }

    private static void saveLog(OpsClusterOperateLog operateLog) {
        AsyncManager.me().execute(new TimerTask() {
            @Override
            public void run() {
                SpringUtils.getBean(IOpsClusterLogService.class).insertOperateLog(operateLog);
            }
        });
    }

    private static String toString(Object content) {
        try {
            return Objects.isNull(content) ? "" : objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            return "json exception: " + e.getMessage();
        }
    }
}
