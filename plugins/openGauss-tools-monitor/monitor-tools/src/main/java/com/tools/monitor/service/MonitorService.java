/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
