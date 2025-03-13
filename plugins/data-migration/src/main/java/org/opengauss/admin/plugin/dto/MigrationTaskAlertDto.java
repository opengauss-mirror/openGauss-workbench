/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.dto;

import lombok.Data;

/**
 * migration task alert dto
 *
 * @since 2025/3/11
 */
@Data
public class MigrationTaskAlertDto {
    private Integer id;
    private int taskId;
    private int migrationPhase;
    private String causeCn;
    private String causeEn;
    private String logCode;
    private int logSource;

    private Integer minId;
    private Integer maxId;
    private Long groupNum;
    private String firstDateTime;
    private String latestDateTime;

    /**
     * format date time
     */
    public void formatDateTime() {
        String dataTimeModel = "yyyy-MM-dd HH:mm:ss";
        this.firstDateTime = this.firstDateTime.substring(0, dataTimeModel.length());
        this.latestDateTime = this.latestDateTime.substring(0, dataTimeModel.length());
    }
}
