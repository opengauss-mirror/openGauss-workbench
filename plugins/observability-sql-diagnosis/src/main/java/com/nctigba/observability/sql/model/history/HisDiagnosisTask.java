/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.model.history.result.TaskState;
import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

/**
 * HisDiagnosisTask
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@TableName(value = "his_diagnosis_task_info", autoResultMap = true)
@Accessors(chain = true)
@Slf4j
@Generated
public class HisDiagnosisTask {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("cluster_id")
    String clusterId;
    @TableField("node_id")
    String nodeId;
    @TableField("db_name")
    String dbName;
    @TableField("task_name")
    String taskName;
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
    TaskState state;
    String span;
    String remarks = "";
    @TableField(value = "conf", typeHandler = TypeHander.class)
    List<OptionQuery> configs;
    @TableField(value = "threshold", typeHandler = TypeHander.class)
    List<HisDiagnosisThreshold> thresholds;
    @TableField(value = "node_vo_sub", typeHandler = TypeHander.class)
    OpsClusterVO nodeVOSub;
    @TableField("is_deleted")
    Integer isDeleted = 0;
    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date createTime = new Date();
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date updateTime = new Date();

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
        if (this.span != null) {
            return this.span;
        }
        long costSec;
        if (taskEndTime != null) {
            costSec = taskEndTime.getTime() - taskStartTime.getTime();
        } else {
            costSec = 0L;
        }
        return this.span = DateUtil.secondToTime((int) costSec / 1000) + String.format(".%03d", costSec % 1000);
    }

    public synchronized HisDiagnosisTask addRemarks(String remark) {
        this.remarks += CommonConstants.BR + LocalTime.now() + " " + remark;
        return this;
    }

    public HisDiagnosisTask addRemarks(String taskState, Throwable throwable) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
        }
        String msg = sw.toString().replaceAll(System.lineSeparator(), CommonConstants.BR);
        return addRemarks(taskState + CommonConstants.BR + msg);
    }

    public HisDiagnosisTask addRemarks(TaskState taskState) {
        setState(taskState);
        return addRemarks(taskState.getValue());
    }

    public HisDiagnosisTask addRemarks(TaskState taskState, Object message) {
        setState(taskState);
        return addRemarks(taskState.getValue() + "[" + message + "]");
    }

    public HisDiagnosisTask addRemarks(TaskState taskState, Throwable throwable) {
        setState(taskState);
        return addRemarks(taskState.getValue(), throwable);
    }
}
