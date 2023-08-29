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
 * TaskExecProgressDto.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/beans/TaskExecProgressDto.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.beans;

import lombok.Data;
import org.opengauss.admin.common.enums.SysTaskStatus;

/**
 * task execution progress
 */
@Data
public class TaskExecProgressDto {

    private Integer taskId;

    /**
     * Task status
     */
    private SysTaskStatus taskStatus;

    /**
     * Task Execution Progress Percentage
     */
    private Float execProgress;

}
