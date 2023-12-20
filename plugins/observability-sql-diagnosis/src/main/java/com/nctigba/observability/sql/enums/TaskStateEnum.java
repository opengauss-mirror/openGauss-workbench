/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  TaskStateEnum.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/enums/TaskStateEnum.java
 *
 *  -------------------------------------------------------------------------
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