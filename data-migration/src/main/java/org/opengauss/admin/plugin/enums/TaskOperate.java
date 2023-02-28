package org.opengauss.admin.plugin.enums;

/**
 * main task status
 *
 * @author xielibo
 */
public enum TaskOperate {

    RUN(1, "run"),
    STOP_INCREMENTAL(2, "stop_incremental"),
    START_REVERSE(3, "start_reverse"),
    FINISH_MIGRATION(100, "finish_migration"),
    ;

    private final Integer code;
    private final String command;

    TaskOperate(Integer code, String command) {
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
