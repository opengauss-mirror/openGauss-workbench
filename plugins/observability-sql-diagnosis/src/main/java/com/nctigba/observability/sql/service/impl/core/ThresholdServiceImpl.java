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
 *  ThresholdServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/core/ThresholdServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nctigba.observability.sql.mapper.HisThresholdMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.query.ThresholdQuery;
import com.nctigba.observability.sql.service.ThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * ThresholdServiceImpl
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class ThresholdServiceImpl implements ThresholdService {
    @Autowired
    private HisThresholdMapper hisThresholdMapper;

    @Override
    public List<DiagnosisThresholdDO> select(String diagnosisType) {
        LambdaQueryWrapper<DiagnosisThresholdDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DiagnosisThresholdDO::getDiagnosisType, diagnosisType);
        queryWrapper.orderByDesc(DiagnosisThresholdDO::getThresholdType);
        return hisThresholdMapper.selectList(queryWrapper);
    }

    @Override
    public void insertOrUpdate(ThresholdQuery thresholdQuery) {
        DiagnosisThresholdDO threshold = new DiagnosisThresholdDO();
        if (thresholdQuery.getId() == null) {
            threshold.setThresholdName(thresholdQuery.getThresholdName());
            threshold.setThresholdType(thresholdQuery.getThresholdType());
            threshold.setThresholdValue(thresholdQuery.getThresholdValue());
            hisThresholdMapper.insert(threshold);
        } else {
            threshold.setId(thresholdQuery.getId());
            threshold.setThresholdName(thresholdQuery.getThresholdName());
            threshold.setThresholdType(thresholdQuery.getThresholdType());
            threshold.setThresholdValue(thresholdQuery.getThresholdValue());
            hisThresholdMapper.updateById(threshold);
        }
    }

    @Override
    public void delete(int id) {
        hisThresholdMapper.deleteById(id);
    }

    @Override
    public HashMap<String, String> getThresholdValue(List<String> list) {
        HashMap<String, String> map = new HashMap<>();
        for (String filter : list) {
            LambdaQueryWrapper<DiagnosisThresholdDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DiagnosisThresholdDO::getThresholdName, filter);
            DiagnosisThresholdDO diagnosisThreshold = hisThresholdMapper.selectOne(queryWrapper);
            if (diagnosisThreshold != null) {
                map.put(filter, diagnosisThreshold.getThresholdValue());
            }
        }
        return map;
    }
}
