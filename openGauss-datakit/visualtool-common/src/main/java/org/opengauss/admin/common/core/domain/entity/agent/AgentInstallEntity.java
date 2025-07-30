/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.common.core.domain.entity.agent;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.opengauss.admin.common.enums.agent.AgentStatus;

import java.time.Instant;

/**
 * AgentInstallEntity
 *
 * @author: wangchao
 * @Date: 2025/4/10 09:31
 * @Description: AgentInstallEntiry
 * @since 7.0.0-RC2
 **/
@Data
@TableName("agent_install")
@EqualsAndHashCode
public class AgentInstallEntity {
    @TableId(value = "agent_id", type = IdType.INPUT)
    private String agentId;
    private String agentIp;
    private String agentName;
    private Integer agentPort;
    private String installPath;
    private String installUser;
    private String installUserId;
    private AgentStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Instant createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Instant updateTime;
    @TableField(exist = false)
    private String publicKey;
}
