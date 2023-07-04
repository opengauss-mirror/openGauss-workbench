/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;

/**
 * @author wuyuebin
 * @date 2023/4/28 02:43
 * @description
 */
@Mapper
public interface AlertTemplateRuleMapper extends BaseMapper<AlertTemplateRule> {
}