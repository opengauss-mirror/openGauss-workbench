/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.plugin.domain.MigrationTaskAlertDetail;
import org.opengauss.admin.plugin.mapper.MigrationTaskAlertDetailMapper;
import org.opengauss.admin.plugin.service.MigrationTaskAlertDetailService;
import org.springframework.stereotype.Service;

/**
 * tb migration alert detail service impl
 *
 * @since 2024/12/17
 */
@Service
public class MigrationTaskAlertDetailServiceImpl
        extends ServiceImpl<MigrationTaskAlertDetailMapper, MigrationTaskAlertDetail>
        implements MigrationTaskAlertDetailService {
}
