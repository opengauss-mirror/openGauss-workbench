/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.nctigba.alert.monitor.entity.AlertTemplate;

/**
 * @author wuyuebin
 * @date 2023/4/28 02:42
 * @description
 */
@Mapper
public interface AlertTemplateMapper extends BaseMapper<AlertTemplate> {
}
