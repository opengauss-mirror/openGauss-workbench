/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.plugin.alertcenter.entity.NotifyConfig;
import org.opengauss.plugin.alertcenter.model.NotifyConfigReq;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/6/1 01:46
 * @description
 */
public interface NotifyConfigService extends IService<NotifyConfig> {
    List<NotifyConfig> getAllList();

    void saveList(List<NotifyConfig> list);

    void testConfig(NotifyConfigReq notifyConfigReq);
}
