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
    INSTALL_PORTAL_ERROR(501, "install_portal_error")
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
