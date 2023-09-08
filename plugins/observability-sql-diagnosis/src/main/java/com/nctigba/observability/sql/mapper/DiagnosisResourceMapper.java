package com.nctigba.observability.sql.mapper;

import com.nctigba.observability.sql.model.history.result.Resource;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.config.DataSourceIniter;

@Mapper
@DS(DataSourceIniter.diagnosisSource)
public interface DiagnosisResourceMapper extends BaseMapper<Resource>{
}
