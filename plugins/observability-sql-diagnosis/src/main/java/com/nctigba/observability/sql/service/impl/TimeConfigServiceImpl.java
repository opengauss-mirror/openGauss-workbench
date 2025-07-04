/*
 *  Copyright (c) Huawei Technologies Co. 2025-2025.
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
 *  TimeConfigServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/mapper/TimeConfigServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.observability.sql.mapper.TimeConfigMapper;
import com.nctigba.observability.sql.model.entity.TimeConfigDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * TimeConfigServiceImpl
 *
 * @author jianghongbo
 * @since 2025-06-30
 */
@Slf4j
@Service
public class TimeConfigServiceImpl extends ServiceImpl<TimeConfigMapper, TimeConfigDO> {
    @Autowired
    private TimeConfigMapper timeConfigMapper;

    /**
     * persist time config to database
     *
     * @param peroid Integer
     * @param frequency Integer
     */
    public void persistTimeConfig(Integer peroid, Integer frequency) {
        TimeConfigDO timeConfigDO = new TimeConfigDO(1, peroid, frequency);
        timeConfigMapper.updateById(timeConfigDO);
    }

    /**
     * get frequency from database
     *
     * @return int
     */
    public int getFrequency() {
        TimeConfigDO timeConfigDO = timeConfigMapper.selectById(1);
        return timeConfigDO.getFrequency();
    }

    /**
     * get peroid from database
     *
     * @return int
     */
    public int getPeroid() {
        TimeConfigDO timeConfigDO = timeConfigMapper.selectById(1);
        return timeConfigDO.getPeroid();
    }

    /**
     * get time config from database
     *
     * @return Map<String, Integer>
     */
    public Map<String, String> getTimeConfig() {
        TimeConfigDO timeConfigDO = timeConfigMapper.selectById(1);
        return new HashMap<>() {{
            put("peroid", formatPeroid(timeConfigDO.getPeroid()));
            put("frequency", formatFrequency(timeConfigDO.getFrequency()));
        }};
    }

    private String formatPeroid(Integer peroid) {
        Integer monthCount = peroid / 30;
        if (monthCount > 0) {
            return monthCount + "month";
        }
        return monthCount + "day";
    }

    private String formatFrequency(Integer frequency) {
        Integer hourCount = frequency / 3600;
        if (hourCount > 0) {
            return hourCount + "h";
        }
        Integer minuteCount = frequency / 60;
        if (minuteCount > 0) {
            return minuteCount + "m";
        }
        return frequency + "s";
    }
}
