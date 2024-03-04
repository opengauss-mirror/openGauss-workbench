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
 *  PromAgentRelationDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/entity/PromAgentRelationDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * PromAgentRelationDO
 *
 * @since 2024/2/6 17:11
 */
@Data
@Accessors(chain = true)
@TableName("prom_agent_relation")
public class PromAgentRelationDO {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String envPromId;
    private String envAgentId;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
}
