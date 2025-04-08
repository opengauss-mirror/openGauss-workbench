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
 *  DbDataLocationEnum.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/enums/DbDataLocationEnum.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.common.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * DbDataLocationEnum
 *
 * @since 2024/1/15
 */
@AllArgsConstructor
@Getter
public enum DbDataLocationEnum {
    INTARKDB("org.intarkdb.Driver", Arrays.asList("classpath:db/intarkDB-init.sql")),
    OPENGAUSS("org.opengauss.Driver", Arrays.asList("classpath:db/openGauss-visualtool.sql"));

    private String driverClass;
    private List<String> locations;

    /**
     * get DbDataLocationEnum by driverClass
     *
     * @param driverClass String
     * @return Optional
     */
    public static Optional<DbDataLocationEnum> of(String driverClass) {
        if (StrUtil.isEmpty(driverClass)) {
            return Optional.empty();
        }
        for (DbDataLocationEnum enumConstant : DbDataLocationEnum.class.getEnumConstants()) {
            if (enumConstant.getDriverClass().equalsIgnoreCase(driverClass)) {
                return Optional.of(enumConstant);
            }
        }
        return Optional.empty();
    }
}
