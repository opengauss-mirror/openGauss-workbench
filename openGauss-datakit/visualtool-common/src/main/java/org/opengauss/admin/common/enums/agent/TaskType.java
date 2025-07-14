/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.common.enums.agent;

import com.baomidou.mybatisplus.annotation.IEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TaskType
 *
 * @author: wangchao
 * @Date: 2025/4/16 09:34
 * @Description: TaskType
 * @since 7.0.0-RC2
 **/
@AllArgsConstructor
@Getter
public enum TaskType implements IEnum<String> {
    OSHI_FIXED_METRIC("OSHI_FIXED_METRIC"),
    OSHI_DYNAMIC_METRIC("OSHI_DYNAMIC_METRIC"),
    OS_METRIC("OS_METRIC"),
    DB_METRIC("DB_METRIC"),
    OS_PIPE("OS_PIPE"),
    DB_PIPE("DB_PIPE");
    private final String value;
}
