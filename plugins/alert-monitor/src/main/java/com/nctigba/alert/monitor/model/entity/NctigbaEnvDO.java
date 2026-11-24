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

package com.nctigba.alert.monitor.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;

@Data
@Accessors(chain = true)
@TableName("nctigba_env")
public class NctigbaEnvDO {
    @TableId(type = IdType.ASSIGN_UUID)
    String id;
    String hostid;
    Type type;
    String username;
    String path;
    Integer port;
    String nodeid;
    @TableField(exist = false)
    OpsHostEntity host;

    public enum Type {
        PROMETHEUS, NODE_EXPORTER, OPENGAUSS_EXPORTER,
        PROMETHEUS_MAIN,
        ELASTICSEARCH, FILEBEAT,
        AGENT,
        PROMETHEUS_PKG, NODE_EXPORTER_PKG, OPENGAUSS_EXPORTER_PKG,
        ELASTICSEARCH_PKG, FILEBEAT_PKG,
        AGENT_PKG,
    }
}