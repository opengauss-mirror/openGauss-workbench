/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;

@Data
@Generated
@Accessors(chain = true)
@TableName("nctigba_env")
public class NctigbaEnv {
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
        ELASTICSEARCH, FILEBEAT,
        AGENT,
        PROMETHEUS_PKG, NODE_EXPORTER_PKG, OPENGAUSS_EXPORTER_PKG,
        ELASTICSEARCH_PKG, FILEBEAT_PKG,
        AGENT_PKG,
    }
}