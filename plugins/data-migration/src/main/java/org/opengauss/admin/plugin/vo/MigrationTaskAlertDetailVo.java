/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.admin.plugin.vo;

import lombok.Data;

/**
 * Migration task alert detail vo
 *
 * @since 2026/8/13
 */
@Data
public class MigrationTaskAlertDetailVo {
    private Long alertId;
    private String detail;
}
