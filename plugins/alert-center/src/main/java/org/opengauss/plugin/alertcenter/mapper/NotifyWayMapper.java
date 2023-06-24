/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.opengauss.plugin.alertcenter.dto.NotifyWayDto;
import org.opengauss.plugin.alertcenter.entity.NotifyWay;

/**
 * @author wuyuebin
 * @date 2023/5/9 11:35
 * @description
 */
@Mapper
public interface NotifyWayMapper extends BaseMapper<NotifyWay> {
    @Select("select t1.*,t2.notify_template_name from notify_way t1 left join notify_template t2 "
            + "on t1.notify_template_id = t2.id  ${ew.customSqlSegment}")
    Page<NotifyWayDto> selectDtoPage(Page page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);
}
