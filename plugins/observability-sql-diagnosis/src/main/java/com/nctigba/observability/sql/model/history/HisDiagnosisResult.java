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
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.util.LocaleString;
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

    /**
     * Construction method
     *
     * @param task Diagnosis task info
     * @param pointName Diagnosis point name
     * @param pointState Diagnosis point state
     * @param isHint Diagnosis point is or not hint
     */
    public HisDiagnosisResult(HisDiagnosisTask task, String pointName, PointState pointState, ResultState isHint) {
        this.clusterId = task.getClusterId();
        this.nodeId = task.getNodeId();
        this.taskId = task.getId();
        this.pointName = pointName;
        if (DiagnosisTypeCommon.HISTORY.equals(task.getDiagnosisType())) {
            this.pointTitle = LocaleString.format("history." + pointName + ".title");
            this.pointSuggestion = LocaleString.format("history." + pointName + ".suggest");
            this.pointDetail = LocaleString.format("history." + pointName + ".detail");
        } else {
            if ("BlockSession".equals(pointName) || "IndexAdvisor".equals(pointName)
                    || "SmpParallelQuery".equals(pointName)) {
                this.pointTitle = LocaleString.format("sql." + pointName + ".title");
                this.pointSuggestion = LocaleString.format("sql." + pointName + ".suggest");
                this.pointDetail = LocaleString.format("sql." + pointName + ".detail");
            } else {
                this.pointSuggestion = LocaleString.format(pointName + ".tip");
                this.pointTitle = LocaleString.format("ResultType." + pointName);
                this.pointDetail = LocaleString.format("ResultType." + pointName);
            }
        }
        this.isHint = isHint;
        this.pointState = pointState;
    }

    /**
     * Construction method
     *
     * @param task Diagnosis task info
     * @param analysisDTO Diagnosis data
     * @param pointName Diagnosis point name
     * @param pointState Diagnosis point state
     */
    public HisDiagnosisResult(HisDiagnosisTask task, AnalysisDTO analysisDTO, String pointName, PointState pointState) {
        this.clusterId = task.getClusterId();
        this.nodeId = task.getNodeId();
        this.taskId = task.getId();
        this.pointName = pointName;
        boolean isSqlDiagnosis = "BlockSession".equals(pointName) || "IndexAdvisor".equals(pointName)
                || "SmpParallelQuery".equals(pointName);
        if (analysisDTO.getIsHint() != null && analysisDTO.getIsHint().equals(
                ResultState.SUGGESTIONS)) {
            if (DiagnosisTypeCommon.HISTORY.equals(task.getDiagnosisType())) {
                this.pointSuggestion = LocaleString.format("history." + pointName + ".suggest.high");
                this.pointTitle = LocaleString.format("history." + pointName + ".title.high");
            } else {
                if (isSqlDiagnosis) {
                    this.pointTitle = LocaleString.format("sql." + pointName + ".title.high");
                    this.pointSuggestion = LocaleString.format("sql." + pointName + ".suggest.high");
                } else {
                    this.pointSuggestion = LocaleString.format(pointName + ".tip");
                    this.pointTitle = LocaleString.format("ResultType." + pointName);
                }
            }
        } else {
            if (DiagnosisTypeCommon.HISTORY.equals(task.getDiagnosisType())) {
                this.pointSuggestion = LocaleString.format("history." + pointName + ".suggest.normal");
                this.pointTitle = LocaleString.format("history." + pointName + ".title.normal");
            } else {
                if (isSqlDiagnosis) {
                    this.pointTitle = LocaleString.format("sql." + pointName + ".title.normal");
                    this.pointSuggestion = LocaleString.format("sql." + pointName + ".suggest.normal");
                } else {
                    this.pointSuggestion = LocaleString.format(pointName + ".tip");
                    this.pointTitle = LocaleString.format("ResultType." + pointName);
                }
            }
        }
        this.isHint = analysisDTO.getIsHint();
        this.pointType = analysisDTO.getPointType();
        if (DiagnosisTypeCommon.HISTORY.equals(task.getDiagnosisType())) {
            this.pointDetail = LocaleString.format("history." + pointName + ".detail");
        } else {
            this.pointDetail = LocaleString.format("sql." + pointName + ".detail");
        }
        this.pointState = pointState;
        Object jsonObject = JSONObject.toJSON(analysisDTO.getPointData());
        if (jsonObject instanceof JSON) {
            this.pointData = (JSON) jsonObject;
        }
    }

    /**
     * set method
     *
     * @param obj Diagnosis data
     * @return JSON formatted data
     */
    public HisDiagnosisResult setData(Object obj) {
        Object jsonObject = JSONObject.toJSON(obj);
        if (jsonObject instanceof JSON) {
            this.pointData = (JSON) jsonObject;
        }
        return this;
    }

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
}
