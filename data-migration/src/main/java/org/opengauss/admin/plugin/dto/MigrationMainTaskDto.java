package org.opengauss.admin.plugin.dto;

import lombok.Data;

/**
 * System task dto
 */
@Data
public class MigrationMainTaskDto {
    /**
     * task name
     */
    private String taskName;

    private String  createUser;
    /**
     * exec status
     */
    private Integer execStatus;

    private String execStartTime;

    private String execEndTime;

    private String finishStartTime;

    private String finishEndTime;
}
