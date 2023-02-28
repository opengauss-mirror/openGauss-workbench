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

    static {
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.FULL_START.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.FULL_RUNNING.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.FULL_FINISH.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.FULL_CHECK_START.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.FULL_CHECKING.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.FULL_CHECK_FINISH.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.INCREMENTAL_START.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.INCREMENTAL_RUNNING.getCode(), TaskOperate.RUN.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.INCREMENTAL_STOP.getCode(), TaskOperate.STOP_INCREMENTAL.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.REVERSE_START.getCode(), TaskOperate.START_REVERSE.getCode());
        TASK_STATUS_OPERATE_MAPPING.put(TaskStatus.REVERSE_RUNNING.getCode(), TaskOperate.START_REVERSE.getCode());
    }
}
