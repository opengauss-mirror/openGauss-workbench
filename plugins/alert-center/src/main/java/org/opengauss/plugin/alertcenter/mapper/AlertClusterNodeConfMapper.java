/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.opengauss.plugin.alertcenter.entity.AlertClusterNodeConf;

/**
 * @author wuyuebin
 * @date 2023/4/26 16:45
 * @description
 */
@Mapper
public interface AlertClusterNodeConfMapper extends BaseMapper<AlertClusterNodeConf> {
}
