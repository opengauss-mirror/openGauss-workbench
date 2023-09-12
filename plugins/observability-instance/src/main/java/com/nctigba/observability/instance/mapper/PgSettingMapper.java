/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.instance.entity.PgSettings;

/**
 * PgSettingMapper.java
 *
 * @since 2023-08-25
 */
@Mapper
public interface PgSettingMapper extends BaseMapper<PgSettings> {
}