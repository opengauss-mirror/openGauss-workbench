package com.nctigba.observability.instance.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.observability.instance.entity.OpsWdrEntity;

@Mapper
public interface OpsWdrMapper extends BaseMapper<OpsWdrEntity> {
}