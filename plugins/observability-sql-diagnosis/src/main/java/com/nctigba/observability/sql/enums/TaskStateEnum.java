/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.sql.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.nctigba.observability.sql.util.LocaleStringUtils;

/**
 * TaskState
 *
 * @author luomeng
 * @since 2023/6/9
 */
public enum TaskStateEnum {
    CREATE,
    WAITING,
    SQL_RUNNING,
    RECEIVING,
    DATABASE_CONNECT_ERROR,
    SQL_PARSE_ERROR,
    SQL_ERROR,
    ERROR,
    TIMEOUT_ERROR,
    COLLECT_PID_ERROR,
    FINISH;

    /**
     * Get task state
     *
     * @return String
     */
    @JsonValue
    public String getValue() {
        return LocaleStringUtils.format("TaskState." + this.name());
    }
}