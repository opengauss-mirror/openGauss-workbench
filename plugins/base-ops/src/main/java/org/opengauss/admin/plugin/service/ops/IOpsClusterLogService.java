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
 * IOpsClusterLogService.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/IOpsClusterLogService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterOperateLog;

/**
 * IOpsClusterLogService
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
public interface IOpsClusterLogService {

    /**
     * Save Operlog
     */
    void insertOperateLog(OpsClusterOperateLog operateLog);

    /**
     * Query the collection of system operation logs
     *
     * @param operateLog operateLog
     */
    IPage<OpsClusterOperateLog> selectOperateLogList(IPage<OpsClusterOperateLog> page, OpsClusterOperateLog operateLog);

    /**
     * Delete Cluster Operation Logs in Batches
     *
     * @param operateIds operateIds
     */
    int deleteOperateLogByIds(String[] operateIds);

    /**
     * Delete Cluster Operation Logs by ClusterId
     *
     * @param clusterId clusterId
     * @return delete count
     */
    int deleteOperateLogByClusterId(String clusterId);

    /**
     * Query operation log details
     *
     * @param operateId operateId
     */
    OpsClusterOperateLog selectOperateLogById(String operateId);

    /**
     * Query the operation log of the cluster
     *
     * @param clusterId clusterId
     * @return log
     */
    String queryClusterOperateLog(String clusterId);
}
