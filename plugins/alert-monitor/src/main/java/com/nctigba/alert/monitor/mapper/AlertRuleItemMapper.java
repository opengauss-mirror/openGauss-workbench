/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.nctigba.alert.monitor.entity.AlertRuleItem;

/**
 * @author wuyuebin
 * @date 2023/5/9 11:30
 * @description
 */
@Mapper
public interface AlertRuleItemMapper extends BaseMapper<AlertRuleItem> {
}
