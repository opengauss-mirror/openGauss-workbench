/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 *  TimePointDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/scheduler/TimePointDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * TimePointDO
 *
 * @author jianghongbo
 * @since 2025-06-30
 */
@Data
@TableName("tb_time_point")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TimePointDO {
    @TableField("node_tablename")
    private String nodeTablename;
    @TableField("start_time_point")
    private Date startTimePoint;
    @TableField("finish_time_point")
    private Date finishTimePoint;
}
