/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * HisDiagnosisThreshold
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "his_diagnosis_threshold_info", autoResultMap = true)
public class HisDiagnosisThreshold {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("cluster_id")
    String clusterId;
    @TableField("node_id")
    String nodeId;
    @TableField("threshold_type")
    String thresholdType;
    String threshold;
    @TableField("threshold_name")
    String thresholdName;
    @TableField("threshold_value")
    String thresholdValue;
    @TableField("threshold_unit")
    String thresholdUnit;
    @TableField("threshold_detail")
    String thresholdDetail;
    @TableField("sort_no")
    String sortNo;
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
}
