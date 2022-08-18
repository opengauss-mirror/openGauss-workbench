package org.opengauss.admin.plugin.enums.ops;

import lombok.AllArgsConstructor;

/**
 * websocket connection type
 *
 * @author lhf
 * @date 2022/8/6 18:05
 **/
@AllArgsConstructor
public enum WsConnectType {
    /**
     * Downloading the Installation Package
     */
    DOWNLOAD_INSTALL_PACKAGE,
    /**
     * ssh Interaction
     */
    SSH,
    /**
     * Execute a command
     */
    COMMAND_EXEC;

    public static WsConnectType nameOf(String name) {
        WsConnectType[] enumConstants = WsConnectType.class.getEnumConstants();
        for (WsConnectType enumConstant : enumConstants) {
            if (enumConstant.name().equalsIgnoreCase(name)){
                return enumConstant;
            }
        }
        return null;
    }
}
