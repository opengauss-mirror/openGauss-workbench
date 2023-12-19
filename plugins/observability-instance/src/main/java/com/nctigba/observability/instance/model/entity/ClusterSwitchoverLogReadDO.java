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
 *  ClusterSwitchoverLogReadDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/entity/ClusterSwitchoverLogReadDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * ClusterSwitchoverLogRead
 *
 * @author liupengfei
 * @since 2023/11/16
 */
@Data
@TableName(value = "cluster_switchover_log_read", autoResultMap = true)
@Accessors(chain = true)
@NoArgsConstructor
public class ClusterSwitchoverLogReadDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("cluster_id")
    String clusterId;
    @TableField("cluster_node_id")
    String clusterNodeId;
    @TableField("log_name")
    String logName;
    @TableField("log_last_read")
    Long logLastRead;
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date updateTime = new Date();

    /**
     * updateAfterReadLog
     *
     * @param logs logs
     */
    public void updateAfterReadLog(String[] logs) {
        String[] logCol = logs[logs.length - 1].split(">>>");
        String[] path = logCol[0].split("/");
        this.setLogName(path[path.length - 1]);
        this.setLogLastRead(Long.valueOf(logCol[1]));
        this.updateTime = new Date();
    }
}
