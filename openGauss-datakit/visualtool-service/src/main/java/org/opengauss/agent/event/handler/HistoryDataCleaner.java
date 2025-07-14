/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.event.handler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.exception.ops.AgentTaskException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;

/**
 * HistoryDataCleaner
 *
 * @author: wangchao
 * @Date: 2025/6/10 09:05
 * @since 7.0.0-RC2
 **/
@Slf4j
public abstract class HistoryDataCleaner implements Runnable {
    private final CopyOnWriteArraySet<Long> taskIds = new CopyOnWriteArraySet<>();
    private final String keepPeriod;
    @Setter
    private ScheduledFuture<?> future;
    private Instant lastCleanTime = Instant.now();

    /**
     * PipOldHistoryDataCleaner
     *
     * @param keepPeriod keep period
     */
    public HistoryDataCleaner(String keepPeriod) {
        this.keepPeriod = keepPeriod;
    }

    @Override
    public void run() {
        try {
            if (!taskIds.isEmpty()) {
                Instant currentCleanTime = Instant.now();
                Instant cutoffTime = currentCleanTime.minus(Duration.parse(keepPeriod));
                int deleted = cleanOldHistoryData(getTaskIds(), cutoffTime);
                log.info("clean keepPeriod({}) tasks {} old history data [{}] clean period={}s, cost={}s ", keepPeriod,
                    taskIds, deleted, Duration.between(lastCleanTime, currentCleanTime).toSeconds(),
                    Duration.between(currentCleanTime, Instant.now()).toSeconds());
                lastCleanTime = currentCleanTime;
            }
        } catch (AgentTaskException ex) {
            log.error("History data cleanup failed for period {}", keepPeriod, ex);
        }
    }

    /**
     * clean old history data
     *
     * @param taskIds task ids
     * @param cutoffTime cutoff time
     * @return deleted count
     */
    public abstract int cleanOldHistoryData(List<Long> taskIds, Instant cutoffTime);

    public List<Long> getTaskIds() {
        return List.copyOf(taskIds);
    }

    /**
     * add task id
     *
     * @param taskId task id
     */
    public void addTask(Long taskId) {
        this.taskIds.add(taskId);
    }

    /**
     * remove task id
     *
     * @param taskId task id
     */
    public void removeTask(Long taskId) {
        taskIds.remove(taskId);
        if (taskIds.isEmpty() && future != null) {
            future.cancel(false);
        }
    }

    /**
     * shutdown now clear task ids and cancel future
     */
    public void shutdownNow() {
        if (future != null) {
            future.cancel(false);
        }
        taskIds.clear();
    }
}
