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
 *  DiagnosisTaskDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/entity/DiagnosisTaskDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.entity;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nctigba.observability.sql.constant.CommonConstants;
import com.nctigba.observability.sql.model.dto.DiagnosisTaskDTO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.enums.TaskStateEnum;
import com.nctigba.observability.sql.enums.TaskTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

/**
 * DiagnosisTaskDO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@TableName(value = "his_diagnosis_task_info", autoResultMap = true)
@Accessors(chain = true)
@NoArgsConstructor
@Slf4j
public class DiagnosisTaskDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("cluster_id")
    String clusterId;
    @TableField("node_id")
    String nodeId;
    @TableField("db_name")
    String dbName;
    @TableField("schema_name")
    String schemaName;
    @TableField("task_name")
    String taskName;
    @TableField("topology_map")
    String topologyMap;
    @TableField("sql_id")
    String sqlId;
    String sql;
    Integer pid;
    @TableField("debug_query_id")
    Long debugQueryId;
    @TableField("session_id")
    Long sessionId;
    @TableField("his_data_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date hisDataStartTime;
    @TableField("his_data_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date hisDataEndTime;
    @TableField("task_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date taskStartTime;
    @TableField("task_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date taskEndTime;
    TaskStateEnum state;
    String span;
    String remarks = "";
    @TableField(value = "conf", typeHandler = TypeHander.class)
    List<OptionVO> configs;
    @TableField(value = "threshold", typeHandler = TypeHander.class)
    List<DiagnosisThresholdDO> thresholds;
    @TableField(value = "node_vo_sub", typeHandler = TypeHander.class)
    OpsClusterVO nodeVOSub;
    @TableField("task_type")
    TaskTypeEnum taskType;
    @TableField("diagnosis_type")
    String diagnosisType;
    @TableField("is_deleted")
    Integer isDeleted = 0;
    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date createTime = new Date();
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date updateTime = new Date();
    @TableField("collect_pid_status")
    Integer collectPidStatus;

    /**
     * Construction method
     *
     * @param taskDTO Diagnosis task info
     * @param optionDTOList Diagnosis option
     * @param thresholdList Diagnosis threshold
     */
    public DiagnosisTaskDO(DiagnosisTaskDTO taskDTO, List<OptionVO> optionDTOList,
            List<DiagnosisThresholdDO> thresholdList) {
        this.clusterId = taskDTO.getClusterId();
        this.nodeId = taskDTO.getNodeId();
        this.sqlId = taskDTO.getSqlId();
        this.dbName = taskDTO.getDbName();
        this.schemaName = taskDTO.getSchemaName();
        this.taskName = taskDTO.getTaskName();
        this.taskType = TaskTypeEnum.MANUAL;
        this.sql = taskDTO.getSql();
        this.hisDataStartTime = taskDTO.getHisDataStartTime();
        this.hisDataEndTime = taskDTO.getHisDataEndTime();
        this.configs = optionDTOList;
        this.thresholds = thresholdList;
        this.state = TaskStateEnum.CREATE;
        this.remarks = "***Ready to start diagnosis***";
        this.diagnosisType = taskDTO.getDiagnosisType();
        this.taskStartTime = new Date();
    }

    public static class TypeHander extends JacksonTypeHandler {
        public TypeHander(Class<?> type) {
            super(type);
        }

        @Override
        protected Object parse(String json) {
            return super.parse(json);
        }
    }

    public String getCost() {
        long costSec;
        if (taskEndTime != null) {
            costSec = taskEndTime.getTime() - taskStartTime.getTime();
        } else {
            costSec = 0L;
        }
        return this.span = DateUtil.secondToTime((int) costSec / 1000) + String.format(".%03d", costSec % 1000);
    }

    public synchronized DiagnosisTaskDO addRemarks(String remark) {
        this.remarks += CommonConstants.BR + LocalTime.now() + " " + remark;
        return this;
    }

    public DiagnosisTaskDO addRemarks(String taskState, Throwable throwable) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
        }
        String msg = sw.toString().replaceAll(System.lineSeparator(), CommonConstants.BR);
        return addRemarks(taskState + CommonConstants.BR + msg);
    }

    public DiagnosisTaskDO addRemarks(TaskStateEnum taskState) {
        setState(taskState);
        return addRemarks(taskState.getValue());
    }

    public DiagnosisTaskDO addRemarks(TaskStateEnum taskState, Object message) {
        setState(taskState);
        return addRemarks(taskState.getValue() + "[" + message + "]");
    }

    public DiagnosisTaskDO addRemarks(TaskStateEnum taskState, Throwable throwable) {
        setState(taskState);
        return addRemarks(taskState.getValue(), throwable);
    }
}
