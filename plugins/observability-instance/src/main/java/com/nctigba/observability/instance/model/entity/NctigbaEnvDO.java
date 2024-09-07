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
 *  NctigbaEnvDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/model/entity/NctigbaEnvDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("nctigba_env")
public class NctigbaEnvDO {
    @TableId(type = IdType.ASSIGN_UUID)
    String id;
    String hostid;
    String type;
    String username;
    String path;
    Integer port;
    String nodeid;
    @TableField(exist = false)
    OpsHostEntity host;
    String status;
    String param;
    String errStatusMsg;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Date updateTime;

    public envType type() {
        return envType.valueOf(type);
    }

    public NctigbaEnvDO setType(String type) {
        this.type = type;
        return this;
    }

    public NctigbaEnvDO setType(envType type) {
        this.type = type.name();
        return this;
    }

    public enum envType {
        PROMETHEUS,
        PROMETHEUS_MAIN,
        NODE_EXPORTER,
        OPENGAUSS_EXPORTER,
        EXPORTER,
        ELASTICSEARCH,
        FILEBEAT,
        AGENT,
        PROMETHEUS_PKG,
        NODE_EXPORTER_PKG,
        OPENGAUSS_EXPORTER_PKG,
        ELASTICSEARCH_PKG,
        FILEBEAT_PKG,
        AGENT_PKG,
    }
}