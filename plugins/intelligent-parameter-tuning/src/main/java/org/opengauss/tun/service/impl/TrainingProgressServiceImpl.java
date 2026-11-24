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

package org.opengauss.tun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.opengauss.tun.domain.TrainingProgress;
import org.opengauss.tun.mapper.TrainingProgressMapper;
import org.opengauss.tun.service.TrainingProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TrainingProgressServiceImpl
 *
 * @author liu
 * @since 2023-12-20
 */
@Service
public class TrainingProgressServiceImpl implements TrainingProgressService {
    @Autowired
    private TrainingProgressMapper progressMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TrainingProgress> getTrainingProgressByTunId(String id) {
        return progressMapper.selectList(new QueryWrapper<TrainingProgress>().eq("training_id", id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainingProgress getMaxTpsByByTunId(String id) {
        return progressMapper.selectMaxTpsByTrainingId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainingProgress selectInitialTpsRecord(String id, String paramType) {
        return progressMapper.selectInitialTpsRecord(id, paramType);
    }
}
