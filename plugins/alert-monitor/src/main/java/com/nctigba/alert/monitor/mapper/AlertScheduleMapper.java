/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.alert.monitor.entity.AlertSchedule;
import org.apache.ibatis.annotations.Mapper;

/**
 * AlertScheduleMapper
 *
 * @since 2023/8/1 10:45
 */
@Mapper
public interface AlertScheduleMapper extends BaseMapper<AlertSchedule> {
}
