/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  TaskVO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/vo/point/TaskVO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.vo.point;

import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.enums.TaskStateEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.util.Date;
import java.util.List;

/**
 * TaskVO
 *
 * @author luomeng
 * @since 2023/7/26
 */
@Data
@Accessors(chain = true)
public class TaskVO {
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
    private TaskStateEnum state;
    private String span;
    private String remarks;
    private List<OptionVO> configs;
    private List<DiagnosisThresholdDO> thresholds;
    private OpsClusterVO nodeVOSub;
    private String taskType;
    private String diagnosisType;
    private Integer isDeleted;
    private Date createTime;
    private Date updateTime;
}
