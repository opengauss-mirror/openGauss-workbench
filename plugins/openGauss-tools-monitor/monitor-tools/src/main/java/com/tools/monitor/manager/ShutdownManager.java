package com.tools.monitor.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * ShutdownManager
 *
 * @author liu
 * @since 2022-10-01
 */
@Component
public class ShutdownManager {
    private static final Logger logger = LoggerFactory.getLogger("sys-user");

    @PreDestroy
    public void destroy() {
        shutdownAsyncManager();
    }

    private void shutdownAsyncManager() {
        try {
            logger.info("====shutdown pool====");
            AsyncManager.me().shutdown();
        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
        }
    }
}
