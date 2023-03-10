package com.nctigba.observability.sql.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.config.DataSourceIniter;
import com.nctigba.observability.sql.model.param.NctigbaParamInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS(DataSourceIniter.primary)
public interface NctigbaParamInfoMapper extends BaseMapper<NctigbaParamInfo> {
}
