/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  AlertClusterNodeConfMapper.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/mapper/AlertClusterNodeConfMapper.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
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
public interface AlertClusterNodeConfMapper extends BaseMapper<AlertClusterNodeConfDO> {
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
