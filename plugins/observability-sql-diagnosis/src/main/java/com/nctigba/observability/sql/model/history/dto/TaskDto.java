/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.dto;

import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.model.history.result.TaskState;
import lombok.Data;
import lombok.experimental.Accessors;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.util.Date;
import java.util.List;

/**
 * TaskDto
 *
 * @author luomeng
 * @since 2023/7/26
 */
@Data
@Accessors(chain = true)
public class TaskDto {
    private Integer id;
    private String clusterId;
    private String nodeId;
    private String dbName;
    private String taskName;
    private String sqlId;
    private String sql;
    private Date hisDataStartTime;
    private Date hisDataEndTime;
    private Date taskStartTime;
    private Date taskEndTime;
    private TaskState state;
    private String span;
    private String remarks;
    private List<OptionQuery> configs;
    private List<HisDiagnosisThreshold> thresholds;
    private OpsClusterVO nodeVOSub;
    private String taskType;
    private String diagnosisType;
    private Integer isDeleted;
    private Date createTime;
    private Date updateTime;
}
