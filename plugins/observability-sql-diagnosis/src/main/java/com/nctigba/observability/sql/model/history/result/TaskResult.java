/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.result;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.nctigba.observability.sql.model.diagnosis.result.Frame;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TaskResult
 *
 * @author luomeng
 * @since 2023/7/31
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TaskResult {
    Integer taskId;
    ResultState state;
    @TableField("resultType")
    ResultType resultType;
    FrameType frameType;
    Frame.bearing bearing;
    JSON data;

    /**
     * Construction method
     *
     * @param task Diagnosis task info
     * @param state Diagnosis task state
     * @param resultType Diagnosis result type
     * @param frameType Diagnosis result frame type
     * @param bearing Diagnosis result bearing
     */
    public TaskResult(HisDiagnosisTask task, TaskResult.ResultState state, ResultType resultType, FrameType frameType,
            Frame.bearing bearing) {
        super();
        this.state = state;
        this.taskId = task.getId();
        this.resultType = resultType;
        this.frameType = frameType;
        this.bearing = bearing;
    }

    /**
     * Enum result state
     *
     */
    public enum ResultState {
        NO_ADVICE,
        SUGGESTION,
    }

    /**
     * Set data method
     *
     * @param obj data info
     * @return TaskResult
     */
    public TaskResult setData(Object obj) {
        Object object = JSONObject.toJSON(obj);
        if (object instanceof JSON) {
            this.data = (JSON) object;
        }
        return this;
    }

    /**
     * Construction method
     *
     * @return Frame
     */
    public Frame toFrame() {
        return new Frame().setType(frameType).setData(data);
    }
}
