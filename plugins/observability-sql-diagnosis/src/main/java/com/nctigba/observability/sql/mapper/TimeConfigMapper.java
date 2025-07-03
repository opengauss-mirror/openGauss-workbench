package com.nctigba.observability.sql.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.sql.model.entity.TimeConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * TimeConfigMapper
 *
 * @author jianghongbo
 * @since 2025-06-30
 */
@Mapper
@DS("hisSlowSqlInfo")
public interface TimeConfigMapper extends BaseMapper<TimeConfigDO> {
}
