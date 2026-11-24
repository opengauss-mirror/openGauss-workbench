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

package com.nctigba.observability.instance.agent.service;

import com.nctigba.observability.instance.agent.config.model.TargetConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Service to manager target store in Yml file
 *
 * @since: 2023/12/1
 */
public interface TargetService {
    /**
     * Get all targets managed
     *
     * @return Target configs
     * @since 2023/12/1
     */
    List<TargetConfig> getTargetConfigs();

    /**
     * Clear all targets in manager
     *
     * @throws IOException Read yml file error
     * @since 2023/12/1
     */
    void clearTargets() throws IOException;

    /**
     * get or create Yml file
     *
     * @return File
     * @throws IOException Read yml file error
     * @since 2023/12/1
     */
    File getOrCreateYmlFile() throws IOException;

    /**
     * get Yml file
     *
     * @return File
     * @throws IOException Read yml file error
     * @since 2023/12/1
     */
    File getYmlFile() throws IOException;
}
