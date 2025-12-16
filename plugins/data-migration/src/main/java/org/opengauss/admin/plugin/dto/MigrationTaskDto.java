/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * MigrationTaskDto.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/dto/MigrationTaskDto.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.opengauss.admin.plugin.domain.MigrationTask;

import java.util.List;

/**
 * @className: MigrationTaskDto
 * @author: xielibo
 * @date: 2023-01-14 20:22
 **/
@Data
public class MigrationTaskDto {
    private Integer taskId;

    @NotBlank(message = "taskName cannot be empty")
    private String taskName;

    @NotEmpty(message = "tasks cannot be empty")
    private List<MigrationTask> tasks;

    @NotEmpty(message = "hostIds cannot be empty")
    private List<String> hostIds;
}
