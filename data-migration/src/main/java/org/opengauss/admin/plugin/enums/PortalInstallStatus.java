package org.opengauss.admin.plugin.enums;

/**
 * portal install status
 *
 * @author xielibo
 */
public enum PortalInstallStatus {


    // 0 ：not install  1：installing；2：Installed；10：install error

    NOT_INSTALL(0, "not install"),
    INSTALLING(1, "installing"),
    INSTALLED(2, "installed"),
    INSTALL_ERROR(10, "install error")
    ;

    private final Integer code;
    private final String command;

    PortalInstallStatus(Integer code, String command) {
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
