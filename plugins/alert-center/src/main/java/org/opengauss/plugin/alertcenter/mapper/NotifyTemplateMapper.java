/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.plugin.alertcenter.entity.NotifyTemplate;

/**
 * @author wuyuebin
 * @date 2023/5/6 15:55
 * @description
 */
@Mapper
public interface NotifyTemplateMapper extends BaseMapper<NotifyTemplate> {
}
