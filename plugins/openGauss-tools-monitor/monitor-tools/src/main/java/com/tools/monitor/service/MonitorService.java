package com.tools.monitor.service;

import com.tools.monitor.entity.ResponseVO;
import com.tools.monitor.entity.SysConfig;

import java.util.List;

/**
 * MonitorService
 *
 * @author liu
 * @since 2022-10-01
 */
public interface MonitorService {

	ResponseVO setSourceConfig(SysConfig sysConfig);

	ResponseVO updateConfig(SysConfig sysConfig);

	ResponseVO getDataSourceList(Integer page, Integer size);

	ResponseVO deleteConfigById(List<Long> ids);

    ResponseVO selectZabbixConfig();

	ResponseVO selectNagiosConfig();

	ResponseVO getDataSourceNameList();
}
