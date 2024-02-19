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
 *  NctigbaEnv.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/model/entity/NctigbaEnv.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("nctigba_env")
public class NctigbaEnvDO {
    @TableId(type = IdType.ASSIGN_UUID)
    String id;
    String hostid;
    InstallType type;
    String username;
    String path;
    Integer port;
    String nodeid;
    @TableField(exist = false)
    OpsHostEntity host;
    String param;
    String status;
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date updateTime;

    /**
     * Set data
     *
     * @param status Enum value
     * @return NctigbaEnvDO
     */
    public NctigbaEnvDO setEnvStatus(String status) {
        this.status = status;
        this.updateTime = new Date();
        return this;
    }

    /**
     * Installation type
     *
     */
    public enum InstallType {
        PROMETHEUS, NODE_EXPORTER, OPENGAUSS_EXPORTER,
        ELASTICSEARCH, FILEBEAT,
        AGENT,
        PROMETHEUS_PKG, NODE_EXPORTER_PKG, OPENGAUSS_EXPORTER_PKG,
        ELASTICSEARCH_PKG, FILEBEAT_PKG,
        AGENT_PKG,
    }
}