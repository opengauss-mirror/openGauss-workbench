/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.third.party.tools.enums;

import lombok.Getter;

import org.opengauss.third.party.tools.csv.CsvExportable;

/**
 * Third party tool enum class
 *
 * @since 2026/6/8
 */
@Getter
public enum ThirdPartyToolEnum implements CsvExportable {
    MIGRATION_PORTAL("migration-portal",
            "org.opengauss.third.party.tools.entity.PortalInstallInfo",
            "data/third-party-tools/migration-portal-install-info.csv"),
    PROMETHEUS("prometheus",
            "org.opengauss.third.party.tools.entity.PrometheusInstallInfo",
            "data/third-party-tools/prometheus-install-info.csv"),
    INSTANCE_EXPORTER("instance-exporter",
            "org.opengauss.third.party.tools.entity.InstanceExporterInstallInfo",
            "data/third-party-tools/instance-exporter-install-info.csv"),
    ;

    ThirdPartyToolEnum(String toolName, String installInfoClassName, String installInfoCsvFilePath) {
        this.toolName = toolName;
        this.installInfoClassName = installInfoClassName;
        this.installInfoCsvFilePath = installInfoCsvFilePath;
    }

    /**
     * all third-party tools csv file path
     */
    public static final String THIRD_PARTY_TOOLS_CSV_FILE_PATH = "data/third-party-tools/third-party-tools.csv";

    /**
     * tool's name
     */
    private final String toolName;

    /**
     * tool's install info class name
     */
    private final String installInfoClassName;

    /**
     * tool's install info csv file path
     * The csv file must contain the following columns: id, ip, port, username, password, install_dir
     */
    private final String installInfoCsvFilePath;

    @Override
    public String getCsvHeader() {
        return "tool_name,install_info_csv_file_path";
    }

    @Override
    public String getCsvData() {
        return String.format("%s,%s", toolName, installInfoCsvFilePath);
    }
}
