/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  AlertConfigServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/AlertConfigServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.model.entity.AlertConfigDO;
import com.nctigba.alert.monitor.mapper.AlertConfigMapper;
import com.nctigba.alert.monitor.service.AlertConfigService;
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
public class AlertConfigServiceImpl extends ServiceImpl<AlertConfigMapper, AlertConfigDO>
    implements AlertConfigService {
    @Autowired
    private PrometheusServiceImpl prometheusService;

    @Override
    @Transactional
    public void saveAlertConf(AlertConfigDO alertConfigDO) {
        List<AlertConfigDO> list = this.list();
        if (CollectionUtil.isNotEmpty(list)) {
            this.removeBatchByIds(list.stream().map(item -> item.getId()).collect(Collectors.toList()));
        }
        this.saveOrUpdate(alertConfigDO);
        prometheusService.updatePrometheusConfig(alertConfigDO);
    }
}
