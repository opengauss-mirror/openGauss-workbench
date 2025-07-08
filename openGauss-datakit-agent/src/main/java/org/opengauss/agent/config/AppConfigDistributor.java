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

package org.opengauss.agent.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.agent.utils.OsCommandUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * AppConfigDistributor
 *
 * @author: wangchao
 * @Date: 2025/7/7 15:28
 * @since 7.0.0-RC2
 **/
@Slf4j
@Singleton
@Startup
public class AppConfigDistributor {
    private final AppConfig appConfig;

    /**
     * AppConfigDistributor
     *
     * @param appConfig appConfig
     */
    public AppConfigDistributor(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * onStart initialize AppConfigDistributor instance when application start
     *
     * @param event startup event
     */
    void onStart(@Observes @Initialized(ApplicationScoped.class) Object event) {
        initializeOsCommandWriteList();
    }

    private void initializeOsCommandWriteList() {
        String osCommandWriteList = appConfig.getOsCommandWriteList();
        String osCommandDefaultWriteList = appConfig.getOsCommandDefaultWriteList();
        List<String> writeList = new LinkedList<>();
        if (StrUtil.isNotEmpty(osCommandDefaultWriteList)) {
            writeList.addAll(Arrays.asList(osCommandDefaultWriteList.split(",")));
            log.info("initialized osCommand default write list : {}", osCommandDefaultWriteList);
        }
        if (StrUtil.isNotEmpty(osCommandWriteList)) {
            writeList.addAll(Arrays.asList(osCommandWriteList.split(",")));
            log.info("initialized osCommand custom write list : {}", osCommandDefaultWriteList);
        }
        if (CollUtil.isNotEmpty(writeList)) {
            OsCommandUtils.forceRefreshAllowedCommand(writeList);
            log.info("initialized osCommand write list: {}", OsCommandUtils.getAllowedCommands());
        } else {
            log.warn("initialized osCommand write list empty");
        }
    }
}
