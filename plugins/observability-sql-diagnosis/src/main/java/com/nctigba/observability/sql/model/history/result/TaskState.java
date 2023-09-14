/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.result;

import com.fasterxml.jackson.annotation.JsonValue;
import com.nctigba.observability.sql.util.LocaleString;

/**
 * TaskState
 *
 * @author luomeng
 * @since 2023/6/9
 */
public enum TaskState {
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

    @JsonValue
    public String getValue() {
        return LocaleString.format("TaskState." + this.name());
    }
}