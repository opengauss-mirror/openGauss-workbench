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
 *  TaskResultDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/dto/TaskResultDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.nctigba.observability.sql.enums.FrameTypeEnum;
import com.nctigba.observability.sql.enums.ResultTypeEnum;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.FrameVO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TaskResultDTO
 *
 * @author luomeng
 * @since 2023/7/31
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TaskResultDTO {
    Integer taskId;
    ResultState state;
    @TableField("resultType")
    ResultTypeEnum resultType;
    FrameTypeEnum frameType;
    FrameVO.bearing bearing;
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
    public TaskResultDTO(DiagnosisTaskDO task, ResultState state, ResultTypeEnum resultType,
            FrameTypeEnum frameType,
            FrameVO.bearing bearing) {
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
     * @return TaskResultDTO
     */
    public TaskResultDTO setData(Object obj) {
        Object object = JSONObject.toJSON(obj);
        if (object instanceof JSON) {
            this.data = (JSON) object;
        }
        return this;
    }

    /**
     * Construction method
     *
     * @return FrameVO
     */
    public FrameVO toFrame() {
        return new FrameVO().setType(frameType).setData(data);
    }
}
