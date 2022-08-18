package org.opengauss.admin.web.listener;

import com.gitee.starblues.integration.listener.PluginInitializerListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author xielibo
 * @version 1.0
 */
@Component
@Slf4j
public class MyPluginInitializerListener implements PluginInitializerListener {
    @Override
    public void before() {
        log.info("before init.");
    }

    @Override
    public void complete() {
        log.info("init complete.");
    }

    @Override
    public void failure(Throwable throwable) {
        log.error("init error:" + throwable.getMessage());
    }
}
