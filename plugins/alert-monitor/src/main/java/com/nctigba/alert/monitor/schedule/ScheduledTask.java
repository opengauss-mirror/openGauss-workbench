/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
