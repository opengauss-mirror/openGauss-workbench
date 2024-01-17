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
 *  NotifyWayMapper.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/mapper/NotifyWayMapper.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.nctigba.alert.monitor.model.dto.NotifyWayDTO;

/**
 * @author wuyuebin
 * @date 2023/5/9 11:35
 * @description
 */
@Mapper
public interface NotifyWayMapper extends BaseMapper<NotifyWayDO> {
    @Select("select t1.id,t1.name,t1.notify_type,t1.send_way,t1.webhook,t1.sign,t1.phone,t1.email,t1.person_id,"
        + "t1.dept_id,t1.header,t1.params,t1.body,t1.result_code,t1.snmp_ip,t1.snmp_port,t1.snmp_community,"
        + "t1.snmp_oid,t1.snmp_version,t1.snmp_username,t1.snmp_auth_passwd,t1.snmp_priv_passwd,t1.notify_template_id,"
        + "t1.create_time,t1.update_time,t1.is_deleted,t2.notify_template_name "
        + "from notify_way t1 left join notify_template t2 "
        + "on t1.notify_template_id = t2.id  ${ew.customSqlSegment}")
    Page<NotifyWayDTO> selectDtoPage(Page page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);
}
