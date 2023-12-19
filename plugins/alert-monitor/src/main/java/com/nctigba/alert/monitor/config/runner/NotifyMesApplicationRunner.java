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
 *  NotifyMesApplicationRunner.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/config/runner/NotifyMesApplicationRunner.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.config.runner;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.model.entity.NotifyMessageDO;
import com.nctigba.alert.monitor.service.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * NotifyMesApplicationRunner
 *
 * @since 2023/11/27 16:26
 */
@Component
@Slf4j
public class NotifyMesApplicationRunner implements ApplicationRunner {
    @Autowired
    private NotifyMessageMapper notifyMessageMapper;
    @Autowired
    private List<CommunicationService> communicationServices;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<NotifyMessageDO> notifyMessageDOS = notifyMessageMapper.selectList(
            Wrappers.<NotifyMessageDO>lambdaQuery().eq(NotifyMessageDO::getStatus, 0).eq(NotifyMessageDO::getIsDeleted,
                CommonConstants.IS_NOT_DELETE));
        if (CollectionUtil.isEmpty(notifyMessageDOS)) {
            return;
        }
        for (CommunicationService communicationService : communicationServices) {
            communicationService.send(notifyMessageDOS);
        }
    }
}