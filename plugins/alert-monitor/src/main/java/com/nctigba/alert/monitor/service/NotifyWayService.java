
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
 *  NotifyWayService.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/NotifyWayService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.model.dto.NotifyWayDTO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/5/25 15:52
 * @description
 */
public interface NotifyWayService extends IService<NotifyWayDO> {
    List<NotifyWayDO> getList(String notifyType);

    Page<NotifyWayDTO> getListPage(String name, String notifyType, Page page);

    void saveNotifyWay(NotifyWayDO notifyWayDO);

    void delById(Long id);

    /**
     * test notify way
     *
     * @param notifyWayDO NotifyWay
     * @return boolean
     */
    boolean testNotifyWay(NotifyWayDO notifyWayDO);
}
