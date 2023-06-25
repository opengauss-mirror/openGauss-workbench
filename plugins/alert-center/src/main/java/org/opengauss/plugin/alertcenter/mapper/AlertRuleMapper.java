/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.plugin.alertcenter.entity.AlertRule;

/**
 * @author wuyuebin
 * @date 2023/5/9 10:07
 * @description
 */
@Mapper
public interface AlertRuleMapper extends BaseMapper<AlertRule> {
}
