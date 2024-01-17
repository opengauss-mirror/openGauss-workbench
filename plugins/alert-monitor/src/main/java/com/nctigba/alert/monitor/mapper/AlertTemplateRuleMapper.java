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
 *  AlertTemplateRuleMapper.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/mapper/AlertTemplateRuleMapper.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.alert.monitor.model.dto.AlertTemplateRuleDTO;
import org.apache.ibatis.annotations.Mapper;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/4/28 02:43
 * @description
 */
@Mapper
public interface AlertTemplateRuleMapper extends BaseMapper<AlertTemplateRuleDO> {
    @Select("select t1.id,t1.template_id,t1.alert_desc,t1.notify_way_ids,t1.alert_notify,t1.silence_end_time,"
        + "t1.silence_start_time,t1.is_silence,t1.is_repeat,t1.notify_duration_unit,t1.notify_duration,t1.rule_content,"
        + "t1.rule_exp_comb,t1.rule_type,t1.level,t1.rule_name,t1.rule_id,t1.next_repeat,t1.next_repeat_unit,"
        + "t1.max_repeat_count,t1.check_frequency,t1.check_frequency_unit,t1.enable,t1.create_time,t1.update_time,"
        + "t1.is_deleted,t2.template_name from alert_template_rule t1, alert_template t2 where t1.is_deleted = 0 "
        + "and t2.is_deleted = 0 and t1.template_id = t2.id and t1.rule_id = #{ruleId}")
    List<AlertTemplateRuleDTO> getDtoListByRuleId(Long ruleId);
}
