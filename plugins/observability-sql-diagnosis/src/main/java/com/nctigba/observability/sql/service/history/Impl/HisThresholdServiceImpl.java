/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nctigba.observability.sql.mapper.history.HisThresholdMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.query.HisThresholdQuery;
import com.nctigba.observability.sql.service.history.HisThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * HisThresholdServiceImpl
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class HisThresholdServiceImpl implements HisThresholdService {
    @Autowired
    private HisThresholdMapper hisThresholdMapper;

    @Override
    public List<HisDiagnosisThreshold> select(String diagnosisType) {
        LambdaQueryWrapper<HisDiagnosisThreshold> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HisDiagnosisThreshold::getDiagnosisType, diagnosisType);
        queryWrapper.orderByDesc(HisDiagnosisThreshold::getThresholdType);
        return hisThresholdMapper.selectList(queryWrapper);
    }

    @Override
    public void insertOrUpdate(HisThresholdQuery hisThresholdQuery) {
        HisDiagnosisThreshold threshold = new HisDiagnosisThreshold();
        if (hisThresholdQuery.getId() == null) {
            threshold.setThresholdName(hisThresholdQuery.getThresholdName());
            threshold.setThresholdType(hisThresholdQuery.getThresholdType());
            threshold.setThresholdValue(hisThresholdQuery.getThresholdValue());
            hisThresholdMapper.insert(threshold);
        } else {
            threshold.setId(hisThresholdQuery.getId());
            threshold.setThresholdName(hisThresholdQuery.getThresholdName());
            threshold.setThresholdType(hisThresholdQuery.getThresholdType());
            threshold.setThresholdValue(hisThresholdQuery.getThresholdValue());
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
            LambdaQueryWrapper<HisDiagnosisThreshold> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(HisDiagnosisThreshold::getThresholdName, filter);
            HisDiagnosisThreshold diagnosisThreshold = hisThresholdMapper.selectOne(queryWrapper);
            if (diagnosisThreshold != null) {
                map.put(filter, diagnosisThreshold.getThresholdValue());
            }
        }
        return map;
    }
}
