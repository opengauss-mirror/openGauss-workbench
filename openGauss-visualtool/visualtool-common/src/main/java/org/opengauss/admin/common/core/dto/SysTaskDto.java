package org.opengauss.admin.common.core.dto;

import lombok.Data;

/**
 * System task dto
 */
@Data
public class SysTaskDto {
    /**
     * task name
     */
    private String taskName;

    /**
     * task type
     */
    private Integer taskType;

    /**
     * exec status
     */
    private Integer execStatus;

    private String execStartTime;

    private String execEndTime;

    private String finishStartTime;

    private String finishEndTime;
}
