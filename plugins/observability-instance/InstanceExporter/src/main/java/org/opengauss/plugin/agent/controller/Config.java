/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.opengauss.plugin.agent.server.HostMetric;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class Config {
    private final HostMetric hostMetric;

    @GetMapping("/set")
    public void set(String hostId, String nodeId, Integer dbport, String username, String password) throws IOException {
        File application = new File(".", "application.yml");
        if (!application.exists())
            application.createNewFile();
        try (InputStream is = new FileInputStream(application);) {
            Yaml yaml = new Yaml();
            Map<String, Object> map = yaml.load(is);
            if (map == null)
                map = new HashMap<>();
            map.put("conf", Map.of("hostId", hostId, "node",
                    Map.of("nodeId", nodeId, "dbport", dbport, "username", username, "password", password)));
            // refresh curr config
            hostMetric.setHostId(hostId).setNodeId(nodeId).setDbport(dbport).setDbUsername(username)
                    .setDbPassword(password);
            try (var writer = new FileWriter(application)) {
                writer.write(yaml.dumpAsMap(map));
            }
        } catch (IOException e) {
            throw e;
        }
    }
}