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

import jakarta.inject.Singleton;
import lombok.Data;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * AppConfig
 *
 * @author: wangchao
 * @Date: 2025/4/26 15:46
 * @Description: AppConfig
 * @since 7.0.0-RC2
 **/
@Singleton
@Data
public class AppConfig {
    @ConfigProperty(name = "agent.id")
    Long agentId;
    @ConfigProperty(name = "agent.name")
    String appName;
    @ConfigProperty(name = "agent.version")
    String appVersion;
    @ConfigProperty(name = "agent.server")
    String appServerUrl;
    @ConfigProperty(name = "agent.heartbeat.interval")
    int heartbeatInterval;
    @ConfigProperty(name = "agent.heartbeat.break-wait-max-times")
    int heartbeatBreakWaitMaxTimes;
    @ConfigProperty(name = "agent.os.command-write-list", defaultValue = "")
    String osCommandWriteList;
    String osCommandDefaultWriteList
        = "date,dig,ping,ls,echo,dir,wc,head,java,git,cat,mkdir,tail,grep,touch,less,sort,id,javac";
}
