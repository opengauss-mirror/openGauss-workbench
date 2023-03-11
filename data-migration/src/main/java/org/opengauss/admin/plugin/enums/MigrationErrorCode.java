package org.opengauss.admin.plugin.enums;

/**
 * migration error code
 *
 * @author xielibo
 */
public enum MigrationErrorCode {

    MAIN_TASK_NOT_EXISTS_ERROR(50101, "main task not exists error"),
    MAIN_TASK_IS_RUNNING_ERROR(50102, "main task is running"),


    SUB_TASK_NOT_EXISTS_ERROR(50151, "task not exists error"),
    SUB_TASK_NOT_IN_INCREMENTAL_ERROR(50152, "task status is not incremental"),
    SUB_TASK_NOT_IN_INCREMENTAL_STOP_ERROR(50153, "task status is not incremental stop"),

    PORTAL_INSTALL_ERROR(50120, "portal install error"),
    ;

    private final Integer code;
    private final String msg;

    MigrationErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
