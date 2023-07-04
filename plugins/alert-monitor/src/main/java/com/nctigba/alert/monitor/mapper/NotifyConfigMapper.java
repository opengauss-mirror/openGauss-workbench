/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.nctigba.alert.monitor.entity.NotifyConfig;

/**
 * @author wuyuebin
 * @date 2023/5/7 14:06
 * @description
 */
@Mapper
public interface NotifyConfigMapper extends BaseMapper<NotifyConfig> {
}
