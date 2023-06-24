/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nctigba.common.mybatis.JacksonJsonWithClassTypeHandler;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * HisDiagnosisResult
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@TableName(value = "his_diagnosis_result_info", autoResultMap = true)
@Accessors(chain = true)
@NoArgsConstructor
public class HisDiagnosisResult {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("cluster_id")
    String clusterId;
    @TableField("node_id")
    String nodeId;
    @TableField("task_id")
    Integer taskId;
    @TableField("point_name")
    String pointName;
    @TableField("point_type")
    PointType pointType;
    @TableField("point_title")
    String pointTitle;
    @TableField("point_suggestion")
    String pointSuggestion;
    @TableField(value = "point_data", typeHandler = JacksonJsonWithClassTypeHandler.class)
    JSON pointData;
    @TableField("is_hint")
    ResultState isHint;
    @TableField("point_detail")
    String pointDetail;
    @TableField("point_state")
    PointState pointState;
    @TableField("is_deleted")
    Integer isDeleted;
    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date createTime = new Date();
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date updateTime = new Date();

    public enum ResultState {
        NO_ADVICE,
        SUGGESTIONS
    }

    public enum PointType {
        ROOT,
        CENTER,
        DIAGNOSIS,
        DISPLAY
    }

    public enum PointState {
        NOT_ANALYZED,
        ABNORMAL,
        NORMAL
    }

    public HisDiagnosisResult setData(Object obj) {
        Object jsonObject = JSONObject.toJSON(obj);
        if (jsonObject instanceof JSON) {
            this.pointData = (JSON) jsonObject;
        }
        return this;
    }
}
