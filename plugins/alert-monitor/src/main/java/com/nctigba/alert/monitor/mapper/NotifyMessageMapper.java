/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.nctigba.alert.monitor.entity.NotifyMessage;

/**
 * @author wuyuebin
 * @date 2023/5/6 16:08
 * @description
 */
@Mapper
public interface NotifyMessageMapper extends BaseMapper<NotifyMessage> {
}
