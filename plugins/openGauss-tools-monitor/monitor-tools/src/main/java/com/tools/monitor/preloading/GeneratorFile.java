/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.preloading;

import com.tools.monitor.util.FileUtil;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * GeneratorFile
 *
 * @author liu
 * @since 2022-10-01
 */
@Component
@Slf4j
public class GeneratorFile {
    @Value("${file.dataSourceConfig}")
    private String config;

    @Value("${file.taskConfig}")
    private String jobConfig;

    @Value("${file.relationConfig}")
    private String relationConfig;

    /**
     * init
     */
    @PostConstruct
    public void init() {
        File soucre = FileUtil.createFile(config);
        File job = FileUtil.createFile(jobConfig);
        File relation = FileUtil.createFile(relationConfig);
    }
}
