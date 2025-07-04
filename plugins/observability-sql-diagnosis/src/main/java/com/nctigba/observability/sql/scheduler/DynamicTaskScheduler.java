/*
 *  Copyright (c) Huawei Technologies Co. 2025-2025.
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
 *  DynamicTaskScheduler.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/scheduler/DynamicTaskScheduler.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.scheduler;

import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.impl.HisSlowsqlServiceImpl;
import com.nctigba.observability.sql.service.impl.TimeConfigServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * DynamicTaskScheduler
 *
 * @author jianghongbo
 * @since 2025-06-30
 */
@Component
@Slf4j
public class DynamicTaskScheduler {
    @Autowired
    private HisSlowsqlServiceImpl hisSlowsqlService;
    @Autowired
    private TimeConfigServiceImpl timeConfigService;
    @Autowired
    private ClusterManager clusterManager;
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledFuture;
    private ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(20, 20, 5,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private long frequency = 60L;

    /**
     * Constructor
     *
     * @param taskScheduler TaskScheduler the schema mapping map
     */
    public DynamicTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    /**
     * start scheduled task
     */
    @PostConstruct
    public void start() {
        this.frequency = timeConfigService.getFrequency();
        scheduleTask();
    }

    /**
     * set schedule task frequency
     *
     * @param frequency int frequency
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
        rescheduleTask();
    }

    private void scheduleTask() {
        PeriodicTrigger trigger = new PeriodicTrigger(frequency, TimeUnit.SECONDS);
        trigger.setFixedRate(false);
        scheduledFuture = taskScheduler.schedule(this::executeTask, trigger);
    }

    private void rescheduleTask() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        scheduleTask();
    }

    private void executeTask() {
        List<OpsClusterVO> clusters = clusterManager.getAllOpsCluster();
        List<OpsClusterNodeVO> allNodes = new ArrayList<>();
        for (OpsClusterVO cluster : clusters) {
            allNodes.addAll(cluster.getClusterNodes());
        }

        for (OpsClusterNodeVO node : allNodes) {
            poolExecutor.execute(() -> {
                hisSlowsqlService.cleanAndCollectSlowsqls(node);
            });
        }
    }
}
