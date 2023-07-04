/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.entity.AlertConfig;

/**
 * @author wuyuebin
 * @date 2023/6/6 17:18
 * @description
 */
public interface AlertConfigService extends IService<AlertConfig> {
    void saveAlertConf(AlertConfig alertConfig);
}
