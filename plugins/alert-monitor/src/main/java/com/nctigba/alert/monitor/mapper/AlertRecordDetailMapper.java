/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.alert.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nctigba.alert.monitor.model.entity.AlertRecordDetailDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AlertRecordDetailMapper
 *
 * @since 2023/11/24 10:31
 */
@Mapper
public interface AlertRecordDetailMapper extends BaseMapper<AlertRecordDetailDO> {
}
