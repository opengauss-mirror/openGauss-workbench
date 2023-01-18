package com.nctigba.observability.sql.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.model.diagnosis.Resource;

@Mapper
@DS(Ds.diagnosis)
public interface DiagnosisResourceMapper extends BaseMapper<Resource>{
}
