package com.tools.monitor.entity.zabbix;

import lombok.Data;

/**
 * FailMessage
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class FailMessage {
    private String sourceName;

    private String targetName;

    private String failResion;

    public FailMessage() {
    }

    public FailMessage(String sourceName, String targetName, String failResion) {
        this.sourceName = sourceName;
        this.targetName = targetName;
        this.failResion = failResion;
    }
}
