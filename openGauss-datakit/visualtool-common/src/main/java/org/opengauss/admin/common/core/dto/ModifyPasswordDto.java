/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2024. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 *           http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.common.core.dto;

import lombok.Data;

/**
 * ModifyPasswordDto
 *
 * @date: 2024/10/16 16:30
 * @description: ModifyPasswordDto
 * @version: 1.0
 * @since 2024-10-16
 */
@Data
public class ModifyPasswordDto {
    /**
     * oldPassword
     */
    private String oldPassword;

    /**
     * newPassword
     */
    private String newPassword;
}
