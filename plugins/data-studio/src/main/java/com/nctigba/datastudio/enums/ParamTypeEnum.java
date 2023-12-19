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
 *  ParamTypeEnum.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/enums/ParamTypeEnum.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.enums;

/**
 * ParamTypeEnum
 *
 * @since 2023-6-26
 */
public enum ParamTypeEnum {
    /**
     * int8
     */
    INT8("bigint"),

    /**
     * int4
     */
    INT4("integer"),

    /**
     * int2
     */
    INT2("smallint"),

    /**
     * int1
     */
    INT1("tinyint");

    private String type;

    ParamTypeEnum(String type) {
        this.type = type;
    }

    /**
     * parse type
     *
     * @param name name
     * @return String
     */
    public static String parseType(String name) {
        for (ParamTypeEnum paramTypeEnum : ParamTypeEnum.values()) {
            if (paramTypeEnum.name().equalsIgnoreCase(name)) {
                return paramTypeEnum.getType();
            }
        }
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
