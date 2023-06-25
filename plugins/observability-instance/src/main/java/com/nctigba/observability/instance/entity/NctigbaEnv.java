/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.entity;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("nctigba_env")
public class NctigbaEnv {
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

    public envType type() {
        return envType.valueOf(type);
    }

    public NctigbaEnv setType(String type) {
        this.type = type;
        return this;
    }

    public NctigbaEnv setType(envType type) {
        this.type = type.name();
        return this;
    }

    public enum envType {
        PROMETHEUS,
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