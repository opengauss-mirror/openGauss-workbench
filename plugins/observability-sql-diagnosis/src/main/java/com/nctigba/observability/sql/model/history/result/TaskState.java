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
    CREATE, WAITING, FINISH, DATA;

    @JsonValue
    public String getValue() {
        return LocaleString.format("TaskState." + this.name());
    }
}