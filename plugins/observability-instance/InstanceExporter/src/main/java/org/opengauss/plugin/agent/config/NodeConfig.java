/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.Data;

@Service
public class NodeConfig {
    private static final List<node> nodes = new ArrayList<>();

    public void loadNodes() {
        nodes.add(null);
    }

    @Data
    public static class node {
        private String hostId;
        private String host;
        private Integer sshPort;
        private List<dbInfo> db;
        private int scrape = 15;

        @Data
        public static class dbInfo {
            private String clusterId;
            private String nodeId;
            private Integer dbPort;
            private String dbUser;
            private String dbPassword;
        }
    }
}