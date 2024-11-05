/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * system user
 *
 * @since 2024/10/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUser {
    private Integer userId;
    private String userName;
    private String nickName;
    private String phonenumber;
    private String password;

    /**
     * status(0:normal 1:disabled)
     */
    private String status;
    private String remark;
}
