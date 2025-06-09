/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2025.
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
 *  DbTypeEnum.java
 *
 *  IDENTIFICATION
 *  plugins/data-migration/src/main/java/org/opengauss/admin/plugin/enums/DbTypeEnum.java
 *
 *  -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.enums;

import lombok.Getter;

/**
 * DbTypeEnum
 *
 * @since 2025/6/20
 * @author ybx
 */
@Getter
public enum DbTypeEnum {
    MYSQL("MYSQL"),
    OPENGAUSS("OPENGAUSS"),
    POSTGRESQL("POSTGRESQL");
    private final String dbType;

    DbTypeEnum(String dbType) {
        this.dbType = dbType;
    }

    /**
     * Convert the String type to an enum type
     *
     * @param value StringValue
     * @return DbTypeEnum
     */
    public static DbTypeEnum fromString(String value) {
        for (DbTypeEnum type : DbTypeEnum.values()) {
            if (type.dbType.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported database type: " + value);
    }
}
