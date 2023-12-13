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
 *  AlertRecordDetailServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/AlertRecordDetailServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.mapper.AlertRecordDetailMapper;
import com.nctigba.alert.monitor.model.entity.AlertRecordDetailDO;
import com.nctigba.alert.monitor.service.AlertRecordDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AlertRecordDetailServiceImpl
 *
 * @since 2023/11/24 10:35
 */
@Service
public class AlertRecordDetailServiceImpl extends ServiceImpl<AlertRecordDetailMapper, AlertRecordDetailDO>
    implements AlertRecordDetailService {
    @Override
    public long unnotifyCount() {
        return this.count(Wrappers.<AlertRecordDetailDO>lambdaQuery().eq(AlertRecordDetailDO::getIsDeleted,
            CommonConstants.IS_NOT_DELETE).eq(AlertRecordDetailDO::getNotifyStatus, CommonConstants.UNSEND));
    }

    @Override
    public List<AlertRecordDetailDO> unnotifyList() {
        return this.list(Wrappers.<AlertRecordDetailDO>lambdaQuery().eq(AlertRecordDetailDO::getIsDeleted,
            CommonConstants.IS_NOT_DELETE).eq(AlertRecordDetailDO::getNotifyStatus, CommonConstants.UNSEND)
            .orderByDesc(AlertRecordDetailDO::getId));
    }
}
