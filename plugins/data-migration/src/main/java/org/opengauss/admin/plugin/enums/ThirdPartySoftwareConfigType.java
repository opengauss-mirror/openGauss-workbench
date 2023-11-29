/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ThirdPartySoftwareConfigType enum
 *
 * @author: www
 * @date: 2023/11/28 15:18
 * @description: msg
 * @since: 1.1
 * @version: 1.1
 */
@AllArgsConstructor
@Getter
public enum ThirdPartySoftwareConfigType {
    BIND(1),
    INSTALL(2);
    private Integer code;
}
