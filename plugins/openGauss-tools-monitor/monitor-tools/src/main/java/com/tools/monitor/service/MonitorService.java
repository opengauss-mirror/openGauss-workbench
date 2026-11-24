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

package com.tools.monitor.service;

import java.util.List;
import com.tools.monitor.entity.ResponseVO;
import com.tools.monitor.entity.SysConfig;

/**
 * MonitorService
 *
 * @author liu
 * @since 2022-10-01
 */
public interface MonitorService {
    /**
     * setSourceConfig
     *
     * @param sysConfig sysConfig
     * @return ResponseVO
     */
    ResponseVO setSourceConfig(SysConfig sysConfig);

    /**
     * updateConfig
     *
     * @param sysConfig sysConfig
     * @return ResponseVO
     */
    ResponseVO updateConfig(SysConfig sysConfig);

    /**
     * getDataSourceList
     *
     * @param page page
     * @param size size
     * @return ResponseVO
     */
    ResponseVO getDataSourceList(Integer page, Integer size);

    /**
     * deleteConfigById
     *
     * @param ids ids
     * @return ResponseVO
     */
    ResponseVO deleteConfigById(List<Long> ids);

    /**
     * selectZabbixConfig
     *
     * @return ResponseVO
     */
    ResponseVO selectZabbixConfig();

    /**
     * selectNagiosConfig
     *
     * @return ResponseVO
     */
    ResponseVO selectNagiosConfig();

    /**
     * getDataSourceNameList
     *
     * @return ResponseVO
     */
    ResponseVO getDataSourceNameList();
}
