/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.nctigba.alert.monitor.entity.AlertRecord;

/**
 * @author wuyuebin
 * @date 2023/5/2 10:23
 * @description
 */
@Mapper
public interface AlertRecordMapper extends BaseMapper<AlertRecord> {
}
