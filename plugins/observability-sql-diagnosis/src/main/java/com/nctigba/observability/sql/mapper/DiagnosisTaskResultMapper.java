package com.nctigba.observability.sql.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.model.diagnosis.result.TaskResult;

@Mapper
@DS(Ds.diagnosis)
public interface DiagnosisTaskResultMapper extends BaseMapper<TaskResult> {
}