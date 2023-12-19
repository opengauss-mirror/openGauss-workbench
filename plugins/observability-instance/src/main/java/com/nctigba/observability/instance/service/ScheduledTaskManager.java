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
 *  ScheduledTaskManager.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/ScheduledTaskManager.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * ScheduledTaskManager
 *
 * @author liupengfei
 * @since 2023/11/20
 */
@Component
@Slf4j
public class ScheduledTaskManager {
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private ClusterOpsService clusterOpsService;

    /**
     * Scheduled switchRecord
     */
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void switchRecord() {
        List<OpsClusterVO> allOpsCluster = clusterManager.getAllOpsCluster().stream()
                .filter(cluster -> DeployTypeEnum.CLUSTER.equals(cluster.getDeployType())).collect(Collectors.toList());
        List<? extends Future<?>> futures = allOpsCluster.stream().map(opsClusterVO -> ThreadUtil.execAsync(
                () -> clusterOpsService.switchRecordUpdate(opsClusterVO.getClusterId()))).collect(Collectors.toList());
        for (Future<?> future : futures) {
            try {
                future.get(3000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                future.cancel(true);
                log.error(e.getMessage(), e);
            }
        }
        log.info("switchRecord scheduled Task run success");
    }
}
