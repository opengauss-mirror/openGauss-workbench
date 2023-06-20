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
 * TaskStatus.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/enums/TaskStatus.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * task status
 *
 * @author xielibo
 */
public enum TaskStatus {

    NOT_RUN(0, "not run"),
    FULL_START(1, "full_start"),
    FULL_RUNNING(2, "full_running"),
    FULL_FINISH(3, "full_finish"),
    FULL_CHECK_START(4, "full_check_start"),
    FULL_CHECKING(5, "full_checking"),
    FULL_CHECK_FINISH(6, "full_check_finish"),
    INCREMENTAL_START(7, "incremental_start"),
    INCREMENTAL_RUNNING(8, "incremental_run"),
    // when click stop incremental
    INCREMENTAL_FINISHED(9, "incremental_finished"),
    // stop incremental complete
    INCREMENTAL_STOPPED(10, "incremental_stopped"),
    REVERSE_START(11, "reverse_start"),
    REVERSE_RUNNING(12, "reverse_run"),
    REVERSE_STOP(13, "reverse_stop"),
    MIGRATION_FINISH(100, "migration_finish"),
    MIGRATION_ERROR(500, "error"),
    WAIT_RESOURCE(1000, "wait_resource"),
    INSTALL_PORTAL(2000, "install portal")
    ;

    private final Integer code;
    private final String command;

    TaskStatus(Integer code, String command) {
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
        Optional<TaskStatus> first = Arrays.stream(TaskStatus.values())
                .filter(x -> x.getCode().equals(codeType)).findFirst();
        if (first != null) {
            return first.get().getCommand();
        }
        return "";
    }
}
