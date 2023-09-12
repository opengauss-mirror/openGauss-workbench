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
    IS_WDR("history", LocaleString.format("history.option.isWdr"), 5),
    IS_LOCK("history", LocaleString.format("history.option.isLock"), 6),
    IS_CPU("history", LocaleString.format("history.option.isCpu"), 1),
    IS_IO("history", LocaleString.format("history.option.isIo"), 2),
    IS_MEMORY("history", LocaleString.format("history.option.isMemory"), 3),
    IS_NETWORK("history", LocaleString.format("history.option.isNetwork"), 4),
    IS_EXPLAIN("sql", LocaleString.format("sql.option.explain"), 1),
    IS_OFF_CPU("sql", LocaleString.format("sql.option.isOffCpu"), 3),
    IS_ON_CPU("sql", LocaleString.format("sql.option.isOnCpu"), 2),
    IS_PARAM("sql", LocaleString.format("sql.option.isParam"), 4),
    IS_BCC("sql", LocaleString.format("sql.option.isBcc"), 5);
    private Integer sortNo;
    private String name;
    private String diagnosisType;

    OptionCommon(String diagnosisType, String name, Integer sortNo) {
        this.diagnosisType = diagnosisType;
        this.name = name;
        this.sortNo = sortNo;
    }
}
