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
 * MainTaskStatus.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/enums/MainTaskStatus.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * main task status
 *
 * @author xielibo
 */
public enum MainTaskStatus {
    NOT_RUN(0, "not run"),
    RUNNING(1, "migrating"),
    FINISH(2, "finish"),
    SUCCESS(3, "success"),
    FAIL(4, "fail"),
    INSTALL_PORTAL_ERROR(501, "install_portal_error"),
    CHECK_MIGRATION(3000, "check migration")
    ;

    private final Integer code;
    private final String command;

    MainTaskStatus(Integer code, String command) {
        this.code = code;
        this.command = command;
    }

    public Integer getCode() {
        return code;
    }

    public String getCommand() {
        return command;
    }

    public String getCommandByCode(Integer codeType) {
        Optional<MainTaskStatus> first = Arrays.stream(MainTaskStatus.values())
                .filter(x -> x.getCode().equals(codeType)).findFirst();
        if (first != null) {
            return first.get().getCommand();
        }
        return "";
    }
}
