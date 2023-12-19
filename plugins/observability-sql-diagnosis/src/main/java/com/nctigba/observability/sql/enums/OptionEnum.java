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
 *  OptionEnum.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/enums/OptionEnum.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.enums;

import com.nctigba.observability.sql.util.LocaleStringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OptionEnum
 *
 * @author luomeng
 * @since 2023/6/11
 */
@Getter
@NoArgsConstructor
public enum OptionEnum {
    IS_WDR("history", LocaleStringUtils.format("history.option.isWdr"), 5),
    IS_LOCK("history", LocaleStringUtils.format("history.option.isLock"), 6),
    IS_CPU("history", LocaleStringUtils.format("history.option.isCpu"), 1),
    IS_IO("history", LocaleStringUtils.format("history.option.isIo"), 2),
    IS_MEMORY("history", LocaleStringUtils.format("history.option.isMemory"), 3),
    IS_NETWORK("history", LocaleStringUtils.format("history.option.isNetwork"), 4),
    IS_EXPLAIN("sql", LocaleStringUtils.format("sql.option.explain"), 1),
    IS_OFF_CPU("sql", LocaleStringUtils.format("sql.option.isOffCpu"), 3),
    IS_ON_CPU("sql", LocaleStringUtils.format("sql.option.isOnCpu"), 2),
    IS_PARAM("sql", LocaleStringUtils.format("sql.option.isParam"), 4),
    IS_BCC("sql", LocaleStringUtils.format("sql.option.isBcc"), 5);
    private Integer sortNo;
    private String name;
    private String diagnosisType;

    OptionEnum(String diagnosisType, String name, Integer sortNo) {
        this.diagnosisType = diagnosisType;
        this.name = name;
        this.sortNo = sortNo;
    }
}
