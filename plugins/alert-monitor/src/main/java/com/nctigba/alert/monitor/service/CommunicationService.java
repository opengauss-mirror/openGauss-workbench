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

package com.nctigba.alert.monitor.service;

import com.nctigba.alert.monitor.model.entity.NotifyConfigDO;
import com.nctigba.alert.monitor.model.entity.NotifyMessageDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;

import java.util.List;

/**
 * CommunicationService
 *
 * @since 2023/11/27 01:19
 */
public interface CommunicationService {
    /**
     * send msg
     *
     * @param notifyMessageDOList List<NotifyMessage>
     */
    void send(List<NotifyMessageDO> notifyMessageDOList);

    /**
     *  test send msg
     *
     * @param notifyConfigDO NotifyConfig
     * @param notifyWayDO NotifyWay
     * @return boolean
     */
    default boolean sendTest(NotifyConfigDO notifyConfigDO, NotifyWayDO notifyWayDO) {
        return false;
    }

    /**
     * type
     *
     * @return string
     */
    String getType();
}
