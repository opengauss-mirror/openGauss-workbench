/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.mapper.history;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.config.history.HisDiagnosisInit;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
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
@DS(HisDiagnosisInit.HISTORY_DIAGNOSIS_RESULT)
public interface HisDiagnosisResultMapper extends BaseMapper<HisDiagnosisResult> {
    @Insert("<script>" +
            "INSERT INTO his_diagnosis_result_info (id, cluster_id, node_id, task_id, point_name, point_type, point_title, point_suggestion, point_data, is_hint, point_detail, point_state, is_deleted, create_time, update_time) "
            +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.id}, #{item.clusterId}, #{item.nodeId}, #{item.taskId}, #{item.pointName}, #{item.pointType}, #{item.pointTitle}, #{item.pointSuggestion}, #{item.pointData, typeHandler = com.nctigba.common.mybatis.JacksonJsonWithClassTypeHandler}, #{item.isHint}, #{item.pointDetail}, #{item.pointState}, #{item.isDeleted}, #{item.createTime}, #{item.updateTime})"
            +
            "</foreach>" +
            "</script>")
    void batchInert(List<HisDiagnosisResult> hisDiagnosisResult);
}
