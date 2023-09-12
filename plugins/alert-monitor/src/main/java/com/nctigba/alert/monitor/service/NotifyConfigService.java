/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.entity.NotifyConfig;
import com.nctigba.alert.monitor.model.NotifyConfigReq;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/6/1 01:46
 * @description
 */
public interface NotifyConfigService extends IService<NotifyConfig> {
    List<NotifyConfig> getAllList();

    void saveList(List<NotifyConfig> list);

    boolean testConfig(NotifyConfigReq notifyConfigReq);
}
