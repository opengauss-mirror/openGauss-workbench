/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.opengauss.tun.domain.TrainingConfig;
import org.opengauss.tun.mapper.TrainingConfigMpper;
import org.opengauss.tun.service.TrainingConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TrainingConfigImpl
 *
 * @author liu
 * @since 2023-12-20
 */
@Service
public class TrainingConfigImpl implements TrainingConfigService {
    @Autowired
    private TrainingConfigMpper configMpper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(String trainingId, String status, Integer process) {
        UpdateWrapper<TrainingConfig> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("training_id", trainingId);
        updateWrapper.set(StrUtil.isNotEmpty(status), "status", status);
        updateWrapper.set(process != null, "process", process);
        return configMpper.update(null, updateWrapper);
    }
}
