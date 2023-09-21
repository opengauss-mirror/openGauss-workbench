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

import org.opengauss.plugin.agent.config.DbConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class Config {
    private final DbConfig dbConfig;

    @PostMapping("/set")
    public void set(@RequestBody DbConfig config) throws IOException {
        File application = new File(".", "application.yml");
        if (!application.exists())
            application.createNewFile();
        try (InputStream is = new FileInputStream(application);) {
            // refresh curr config
            BeanUtil.copyProperties(config, dbConfig);
            var map = new HashMap<String, Object>();
            map.put("conf",
                    Map.of("hostId", config.getHostId(), "user", config.getUser(), "pass", config.getPass(), "node",
                            Map.of("nodeId", config.getNodeId(), "dbport", config.getDbport(), "dbUsername",
                                    config.getDbUsername(), "dbPassword", config.getDbPassword())));
            Yaml yaml = new Yaml();
            try (var writer = new FileWriter(application)) {
                writer.write(yaml.dumpAsMap(map));
            }
            dbConfig.afterPropertiesSet();
        } catch (IOException e) {
            throw e;
        }
    }

    @GetMapping("list")
    public DbConfig list() {
        return dbConfig;
    }
}