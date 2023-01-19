package com.tools.monitor.manager;

import com.tools.monitor.quartz.util.spring.SpringUtils;
import com.tools.monitor.util.Threads;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AsyncManager
 *
 * @author liu
 * @since 2022-10-01
 */
public class AsyncManager {
    private final int OPERATE_DELAY_TIME = 10;

    private ScheduledExecutorService executor = SpringUtils.getBean("scheduledExecutorService");

    private AsyncManager() {
    }

    private static AsyncManager me = new AsyncManager();

    public static AsyncManager me() {
        return me;
    }

    /**
     * execute
     *
     * @param task task
     */
    public void execute(TimerTask task) {
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * shutdown
     *
     */
    public void shutdown() {
        Threads.shutdownAndAwaitTermination(executor);
    }
}
