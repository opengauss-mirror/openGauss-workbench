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
