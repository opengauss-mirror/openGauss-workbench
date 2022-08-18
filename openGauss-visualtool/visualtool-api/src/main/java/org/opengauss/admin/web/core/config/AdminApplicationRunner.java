package org.opengauss.admin.web.core.config;

import org.opengauss.admin.system.service.ISysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * @className: AdminApplicationRunner
 * @description: AdminApplicationRunner
 * @author: xielibo
 * @date: 2022-12-17 15:43
 **/
public class AdminApplicationRunner implements ApplicationRunner {

    @Autowired
    private ISysLogService sysLogService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        sysLogService.init();
    }
}
