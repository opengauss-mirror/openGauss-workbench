/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.admin.plugin.domain.MigrationTaskAlertDetail;

/**
 * migration task alert detail mapper
 *
 * @since 2024/12/17
 */
@Mapper
public interface MigrationTaskAlertDetailMapper extends BaseMapper<MigrationTaskAlertDetail> {
}
