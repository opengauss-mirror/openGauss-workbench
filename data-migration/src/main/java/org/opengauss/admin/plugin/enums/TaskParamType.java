/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * TaskParamType.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/enums/TaskParamType.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.enums;

/**
 * TaskParamType Object
 *
 * @author xielibo
 */
public enum TaskParamType {

    TYPE_STRING(1, "string"),
    TYPE_NUMBER(2, "number"),
    TYPE_BOOLEAN(3, "boolean"),
    TYPE_OBJECT_ARR(9, "object_arr");
    private final Integer code;
    private final String command;

    TaskParamType(Integer code, String command) {
        this.code = code;
        this.command = command;
    }

    public Integer getCode() {
        return code;
    }

    public String getCommand() {
        return command;
    }
}
