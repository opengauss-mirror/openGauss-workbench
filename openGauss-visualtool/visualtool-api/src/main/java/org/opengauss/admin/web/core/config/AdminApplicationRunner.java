package org.opengauss.admin.web.core.config;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.enums.SysTaskStatus;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.system.service.ISysLogService;
import org.opengauss.admin.system.service.ISysTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @className: AdminApplicationRunner
 * @description: AdminApplicationRunner
 * @author: xielibo
 * @date: 2022-12-17 15:43
 **/
@Slf4j
@Component
public class AdminApplicationRunner implements ApplicationRunner {

    @Autowired
    private ISysLogService sysLogService;
    @Autowired
    private ISysTaskService sysTaskService;


    ExecutorService singlePool = new ThreadPoolExecutor(3, 4, 1000L, TimeUnit.MICROSECONDS,
            new LinkedBlockingQueue<>());

    public void doRefreshTaskStatus(){
        while(true) {
            sysTaskService.refreshTaskStatus();
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        sysLogService.init();
        singlePool.submit(() -> {
            try {
                doRefreshTaskStatus();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("RefreshMainTask Async Thread error", e);
            }
        });
    }
}
