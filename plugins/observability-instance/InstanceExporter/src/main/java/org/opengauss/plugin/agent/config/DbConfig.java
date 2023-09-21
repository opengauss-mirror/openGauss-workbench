/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.agent.config;

import org.opengauss.plugin.agent.util.CmdUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.experimental.Accessors;

@Component
@Data
@Accessors(chain = true)
public class DbConfig implements InitializingBean {
    @Value("${exporter.port}")
    private Integer exporterPort;
    @Value("${conf.hostId:#{null}}")
    private String hostId;
    @Value("${conf.user:#{null}}")
    private String user;
    @Value("${conf.pass:#{null}}")
    private String pass;
    @Value("${conf.node.nodeId:#{null}}")
    private String nodeId;
    @Value("${conf.node.dbport:#{null}}")
    private Integer dbport;
    @Value("${conf.node.dbUsername:#{null}}")
    private String dbUsername;
    @Value("${conf.node.dbPassword:#{null}}")
    private String dbPassword;

    @Override
    public void afterPropertiesSet() {
        if (StrUtil.isBlank(user) || StrUtil.isBlank(pass)) {
            return;
        }
        CmdUtil.init(user, pass);
    }
}