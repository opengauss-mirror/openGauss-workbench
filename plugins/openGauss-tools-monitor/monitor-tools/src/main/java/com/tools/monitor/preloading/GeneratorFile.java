/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.tools.monitor.preloading;

import com.tools.monitor.util.FileUtil;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

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
