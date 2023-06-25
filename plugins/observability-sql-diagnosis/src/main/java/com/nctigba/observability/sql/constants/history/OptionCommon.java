/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.constants.history;

import com.nctigba.observability.sql.util.LocaleString;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OptionCommon
 *
 * @author luomeng
 * @since 2023/6/11
 */
@Getter
@NoArgsConstructor
public enum OptionCommon {
    IS_WDR(LocaleString.format("history.option.isWdr"), 5),
    IS_LOCK(LocaleString.format("history.option.isLock"), 6),
    IS_CPU(LocaleString.format("history.option.isCpu"), 1),
    IS_IO(LocaleString.format("history.option.isIo"), 2),
    IS_MEMORY(LocaleString.format("history.option.isMemory"), 3),
    IS_NETWORK(LocaleString.format("history.option.isNetwork"), 4);
    private Integer sortNo;
    private String name;

    OptionCommon(String name, Integer sortNo) {
        this.name = name;
        this.sortNo = sortNo;
    }
}
