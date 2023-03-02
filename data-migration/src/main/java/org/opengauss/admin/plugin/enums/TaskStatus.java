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
    INCREMENTAL_STOP(9, "incremental_stop"),
    REVERSE_START(10, "reverse_start"),
    REVERSE_RUNNING(11, "reverse_run"),
    REVERSE_STOP(12, "reverse_stop"),
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
