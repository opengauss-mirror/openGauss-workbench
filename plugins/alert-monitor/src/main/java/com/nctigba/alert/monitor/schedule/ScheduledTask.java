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
 *  ScheduledTask.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/schedule/ScheduledTask.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.schedule;

import java.util.concurrent.ScheduledFuture;

/**
 * ScheduledTask
 *
 * @since 2023/8/1 11:48
 */
public final class ScheduledTask {
    volatile ScheduledFuture<?> future;

    /**
     * cancel the task
     */
    public void cancel() {
        ScheduledFuture<?> taskFuture = this.future;
        if (taskFuture != null) {
            taskFuture.cancel(true);
        }
    }
}
