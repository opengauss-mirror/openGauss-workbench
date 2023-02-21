package org.opengauss.admin.plugin.dto;

import lombok.Data;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskGlobalParam;

import java.util.List;

/**
 * @className: MigrationTaskDto
 * @author: xielibo
 * @date: 2023-01-14 20:22
 **/
@Data
public class MigrationTaskDto {

    private Integer taskId;

    private String taskName;

    private List<MigrationTask> tasks;

    private List<String> hostIds;

    private List<MigrationTaskGlobalParam> globalParams;


}
