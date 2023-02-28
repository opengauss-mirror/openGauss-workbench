package com.nctigba.observability.sql.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.config.DataSourceIniter;
import com.nctigba.observability.sql.model.DictionaryConfig;

@Mapper
@DS(DataSourceIniter.diagnosis)
public interface DictionaryConfigMapper extends BaseMapper<DictionaryConfig>{
}