/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.enums;

import lombok.Getter;
import org.opengauss.admin.plugin.constants.TaskAlertConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * alert source enum
 *
 * @since 2024/12/16
 */
@Getter
public enum AlertSourceEnum {
    PORTAL(0, "portal log file portal_id.log"),
    FULL_MIGRATION(10, "full migration log file full_migration.log"),
    CHECK_CHECK(20, "data check log file check.log"),
    CHECK_SOURCE(21, "data check log file source.log"),
    CHECK_SINK(22, "data check log file sink.log"),
    CONNECT_SOURCE(31, "incremental migration log file connect_source.log"),
    CONNECT_SINK(32, "incremental migration log file connect_sink.log"),
    REVERSE_CONNECT_SOURCE(41, "reverse migration log file reverse_connect_source.log"),
    REVERSE_CONNECT_SINK(42, "reverse migration log file reverse_connect_sink.log");

    private static final Map<Integer, String> SOURCE_TOOLS_MAP = new HashMap<>();

    static {
        SOURCE_TOOLS_MAP.put(PORTAL.sourceId, TaskAlertConstants.MigrationTools.PORTAL);
        SOURCE_TOOLS_MAP.put(FULL_MIGRATION.sourceId, TaskAlertConstants.MigrationTools.CHAMELEON);
        SOURCE_TOOLS_MAP.put(CHECK_CHECK.sourceId, TaskAlertConstants.MigrationTools.DATA_CHECKER);
        SOURCE_TOOLS_MAP.put(CHECK_SOURCE.sourceId, TaskAlertConstants.MigrationTools.DATA_CHECKER);
        SOURCE_TOOLS_MAP.put(CHECK_SINK.sourceId, TaskAlertConstants.MigrationTools.DATA_CHECKER);
        SOURCE_TOOLS_MAP.put(CONNECT_SOURCE.sourceId, TaskAlertConstants.MigrationTools.DEBEZIUM);
        SOURCE_TOOLS_MAP.put(CONNECT_SINK.sourceId, TaskAlertConstants.MigrationTools.DEBEZIUM);
        SOURCE_TOOLS_MAP.put(REVERSE_CONNECT_SOURCE.sourceId, TaskAlertConstants.MigrationTools.DEBEZIUM);
        SOURCE_TOOLS_MAP.put(REVERSE_CONNECT_SINK.sourceId, TaskAlertConstants.MigrationTools.DEBEZIUM);
    }

    private int sourceId;
    private String description;

    AlertSourceEnum(int sourceId, String description) {
        this.sourceId = sourceId;
        this.description = description;
    }

    /**
     * get migration tool name by source id
     *
     * @param sourceId source id
     * @return migration tool name
     */
    public static String getToolBySourceId(Integer sourceId) {
        return SOURCE_TOOLS_MAP.get(sourceId);
    }
}
