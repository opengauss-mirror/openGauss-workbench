/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
     * get name
     *
     * @param type type
     * @return String
     */
    public static String getName(String type) {
        for (ParamTypeEnum paramType : ParamTypeEnum.values()) {
            if (paramType.type.equalsIgnoreCase(type)) {
                return paramType.name();
            }
        }
        return type;
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
