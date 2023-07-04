/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.entity.AlertConfig;
import com.nctigba.alert.monitor.mapper.AlertConfigMapper;
import com.nctigba.alert.monitor.service.AlertConfigService;
import com.nctigba.alert.monitor.service.PrometheusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/6/6 17:18
 * @description
 */
@Service
public class AlertConfigServiceImpl extends ServiceImpl<AlertConfigMapper, AlertConfig> implements AlertConfigService {
    @Autowired
    private PrometheusService prometheusService;

    @Override
    @Transactional
    public void saveAlertConf(AlertConfig alertConfig) {
        List<AlertConfig> list = this.list();
        if (CollectionUtil.isNotEmpty(list)) {
            this.removeBatchByIds(list.stream().map(item -> item.getId()).collect(Collectors.toList()));
        }
        this.saveOrUpdate(alertConfig);
        prometheusService.updateAlertConfig(alertConfig, list);
    }
}
