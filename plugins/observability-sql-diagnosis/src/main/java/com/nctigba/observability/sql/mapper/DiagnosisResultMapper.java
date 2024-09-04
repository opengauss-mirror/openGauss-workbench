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
 *  DiagnosisResultMapper.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/mapper/DiagnosisResultMapper.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * HisDiagnosisResultMapper
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Mapper
@DS("diagnosisSources")
public interface DiagnosisResultMapper extends BaseMapper<DiagnosisResultDO> {
    /**
     * Batch insert data
     *
     * @param diagnosisResultDO List of DiagnosisResultDO
     */
    @Insert("<script>"
            + "INSERT INTO his_diagnosis_result_info (cluster_id, node_id, task_id, point_name, point_type, "
            + "point_title, point_suggestion, point_data, is_hint, point_detail, point_state, is_deleted, create_time, "
            + "update_time) "
            + "VALUES "
            + "<foreach collection='list' item='item' separator=','>"
            + "(#{item.clusterId}, #{item.nodeId}, #{item.taskId}, #{item"
            + ".pointName}, #{item.pointType}, "
            + "#{item.pointTitle}, #{item.pointSuggestion}, "
            + "#{item.pointData, typeHandler = com.nctigba.observability.sql.handler.JacksonJsonWithClassTypeHandler}, "
            + "#{item.isHint}, #{item.pointDetail}, #{item.pointState}, #{item.isDeleted}, #{item.createTime}, "
            + "#{item.updateTime})"
            + "</foreach>"
            + "</script>")
    void batchInert(List<DiagnosisResultDO> diagnosisResultDO);
}
