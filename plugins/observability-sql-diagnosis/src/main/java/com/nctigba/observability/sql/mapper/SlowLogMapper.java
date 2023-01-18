package com.nctigba.observability.sql.mapper;


import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.model.StatementHistory;

@Mapper
public interface SlowLogMapper extends BaseMapper<StatementHistory> {
}