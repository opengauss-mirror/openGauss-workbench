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
 * TaskConstant.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/constants/TaskConstant.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.constants;

import org.opengauss.admin.plugin.enums.TaskOperate;
import org.opengauss.admin.plugin.enums.TaskStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @className: TaskContatns
 * @description: TODO Description
 * @author: xielibo
 * @date: 2023-02-24 00:04
 **/
public class TaskConstant {

    public static Map<Integer, Integer> TASK_STATUS_OPERATE_MAPPING = new HashMap<>();

    /**
     * default openGauss db version
     **/
    public static final String DEFAULT_OPENGAUSS_VERSION = "3.0.0";

    static {
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.FULL_START.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.FULL_RUNNING.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.FULL_FINISH.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.FULL_CHECK_START.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.FULL_CHECKING.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.FULL_CHECK_FINISH.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.INCREMENTAL_START.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.INCREMENTAL_RUNNING.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.INCREMENTAL_PAUSE.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.INCREMENTAL_FINISHED.getCode(),
            TaskOperate.STOP_INCREMENTAL.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.INCREMENTAL_STOPPED.getCode(),
            TaskOperate.STOP_INCREMENTAL.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.REVERSE_START.getCode(), TaskOperate.START_REVERSE.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.REVERSE_RUNNING.getCode(), TaskOperate.START_REVERSE.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.REVERSE_PAUSE.getCode(), TaskOperate.START_REVERSE.getCode());
    }
}
