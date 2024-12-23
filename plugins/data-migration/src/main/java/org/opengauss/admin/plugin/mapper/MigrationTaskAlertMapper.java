/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.admin.plugin.domain.MigrationTaskAlert;

/**
 * migration task alert mapper
 *
 * @since 2024/12/16
 */
@Mapper
public interface MigrationTaskAlertMapper extends BaseMapper<MigrationTaskAlert> {
}
