/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.plugin.alertcenter.entity.AlertConfig;

/**
 * @author wuyuebin
 * @date 2023/6/6 16:14
 * @description
 */
@Mapper
public interface AlertConfigMapper extends BaseMapper<AlertConfig> {
}
