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
 *  DiagnosisResultDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/entity/DiagnosisResultDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nctigba.observability.sql.handler.JacksonJsonWithClassTypeHandler;
import com.nctigba.observability.sql.constant.DiagnosisTypeConstants;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * DiagnosisResultDO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@TableName(value = "his_diagnosis_result_info", autoResultMap = true)
@Accessors(chain = true)
@NoArgsConstructor
public class DiagnosisResultDO {
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
    public DiagnosisResultDO(DiagnosisTaskDO task, String pointName, PointState pointState, ResultState isHint) {
        this.clusterId = task.getClusterId();
        this.nodeId = task.getNodeId();
        this.taskId = task.getId();
        this.pointName = pointName;
        if (DiagnosisTypeConstants.HISTORY.equals(task.getDiagnosisType())) {
            this.pointTitle = LocaleStringUtils.format("history." + pointName + ".title");
            this.pointSuggestion = LocaleStringUtils.format("history." + pointName + ".suggest");
            this.pointDetail = LocaleStringUtils.format("history." + pointName + ".detail");
        } else {
            if ("BlockSession".equals(pointName) || "IndexAdvisor".equals(pointName)
                    || "SmpParallelQuery".equals(pointName)) {
                this.pointTitle = LocaleStringUtils.format("sql." + pointName + ".title");
                this.pointSuggestion = LocaleStringUtils.format("sql." + pointName + ".suggest");
                this.pointDetail = LocaleStringUtils.format("sql." + pointName + ".detail");
            } else {
                this.pointSuggestion = LocaleStringUtils.format(pointName + ".tip");
                this.pointTitle = LocaleStringUtils.format("ResultType." + pointName);
                this.pointDetail = LocaleStringUtils.format("ResultType." + pointName);
            }
        }
        this.isHint = isHint;
        this.pointState = pointState;
    }

    /**
     * Construction init method
     *
     * @param task Diagnosis task info
     * @param pointName Diagnosis point name
     * @param pointType Diagnosis point state
     * @param pointState Diagnosis point state
     * @param isHint Diagnosis point is or not hint
     */
    public DiagnosisResultDO(DiagnosisTaskDO task, String pointName, PointType pointType, PointState pointState,
            ResultState isHint) {
        this.clusterId = task.getClusterId();
        this.nodeId = task.getNodeId();
        this.taskId = task.getId();
        this.pointName = pointName;
        this.pointType = pointType;
        if (DiagnosisTypeConstants.HISTORY.equals(task.getDiagnosisType())) {
            this.pointTitle = LocaleStringUtils.format("history." + pointName + ".title");
            this.pointSuggestion = LocaleStringUtils.format("history." + pointName + ".suggest");
            this.pointDetail = LocaleStringUtils.format("history." + pointName + ".detail");
        } else {
            if ("BlockSession".equals(pointName) || "IndexAdvisor".equals(pointName)
                    || "SmpParallelQuery".equals(pointName)) {
                this.pointTitle = LocaleStringUtils.format("sql." + pointName + ".title");
                this.pointSuggestion = LocaleStringUtils.format("sql." + pointName + ".suggest");
                this.pointDetail = LocaleStringUtils.format("sql." + pointName + ".detail");
            } else {
                this.pointSuggestion = LocaleStringUtils.format(pointName + ".tip");
                this.pointTitle = LocaleStringUtils.format("ResultType." + pointName);
                this.pointDetail = LocaleStringUtils.format("ResultType." + pointName);
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
    public DiagnosisResultDO(DiagnosisTaskDO task, AnalysisDTO analysisDTO, String pointName, PointState pointState) {
        this.clusterId = task.getClusterId();
        this.nodeId = task.getNodeId();
        this.taskId = task.getId();
        this.pointName = pointName;
        boolean isSqlDiagnosis = "BlockSession".equals(pointName) || "IndexAdvisor".equals(pointName)
                || "SmpParallelQuery".equals(pointName);
        if (analysisDTO.getIsHint() != null && analysisDTO.getIsHint().equals(
                ResultState.SUGGESTIONS)) {
            if (DiagnosisTypeConstants.HISTORY.equals(task.getDiagnosisType())) {
                this.pointSuggestion = LocaleStringUtils.format("history." + pointName + ".suggest.high");
                this.pointTitle = LocaleStringUtils.format("history." + pointName + ".title.high");
            } else {
                if (isSqlDiagnosis) {
                    this.pointTitle = LocaleStringUtils.format("sql." + pointName + ".title.high");
                    this.pointSuggestion = LocaleStringUtils.format("sql." + pointName + ".suggest.high");
                } else {
                    this.pointSuggestion = LocaleStringUtils.format(pointName + ".tip");
                    this.pointTitle = LocaleStringUtils.format("ResultType." + pointName);
                }
            }
        } else {
            if (DiagnosisTypeConstants.HISTORY.equals(task.getDiagnosisType())) {
                this.pointSuggestion = LocaleStringUtils.format("history." + pointName + ".suggest.normal");
                this.pointTitle = LocaleStringUtils.format("history." + pointName + ".title.normal");
            } else {
                if (isSqlDiagnosis) {
                    this.pointTitle = LocaleStringUtils.format("sql." + pointName + ".title.normal");
                    this.pointSuggestion = LocaleStringUtils.format("sql." + pointName + ".suggest.normal");
                } else {
                    this.pointSuggestion = LocaleStringUtils.format(pointName + ".tip");
                    this.pointTitle = LocaleStringUtils.format("ResultType." + pointName);
                }
            }
        }
        this.isHint = analysisDTO.getIsHint();
        this.pointType = analysisDTO.getPointType();
        if (DiagnosisTypeConstants.HISTORY.equals(task.getDiagnosisType())) {
            this.pointDetail = LocaleStringUtils.format("history." + pointName + ".detail");
        } else {
            this.pointDetail = LocaleStringUtils.format("sql." + pointName + ".detail");
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
    public DiagnosisResultDO setData(Object obj) {
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
        INITIALIZE,
        NOT_MATCH_OPTION,
        COLLECT_EXCEPTION,
        ANALYSIS_EXCEPTION,
        SUCCEED,
        NOT_SATISFIED_DIAGNOSIS,
        NOT_HAVE_DATA
    }
}
