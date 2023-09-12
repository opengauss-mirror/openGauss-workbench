/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.alert.monitor.entity.AlertClusterNodeConf;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/26 16:45
 * @description
 */
@Mapper
public interface AlertClusterNodeConfMapper extends BaseMapper<AlertClusterNodeConf> {
    /**
     * get ruleId by exclude clusterNodeIds
     *
     * @param wrapper QueryWrapper
     * @return List<Long>
     */
    @Select("select distinct t2.rule_id from alert_cluster_node_conf t1 inner join alert_template_rule t2 "
        + "on t1.template_id = t2.template_id and t1.is_deleted = 0 and t2.is_deleted = 0 "
        + "${ew.customSqlSegment}")
    List<Long> getRuleIdExcludeNoIds(@Param("ew") QueryWrapper wrapper);
}
