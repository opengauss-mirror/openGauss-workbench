/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.experimental.Accessors;

@Component
@Data
@Accessors(chain = true)
public class DbConfig {
    @Value("${exporter.port}")
    private Integer exporterPort;
    @Value("${conf.hostId:#{null}}")
    private String hostId;
    @Value("${conf.node.nodeId:#{null}}")
    private String nodeId;
    @Value("${conf.node.dbport:#{null}}")
    private Integer dbport;
    @Value("${conf.node.dbUsername:#{null}}")
    private String dbUsername;
    @Value("${conf.node.dbPassword:#{null}}")
    private String dbPassword;
}