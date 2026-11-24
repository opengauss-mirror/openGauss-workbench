/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
