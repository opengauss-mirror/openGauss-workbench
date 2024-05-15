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
 *  ClusterSwitchoverRecordDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/entity/ClusterSwitchoverRecordDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nctigba.observability.instance.exception.InstanceException;
import com.nctigba.observability.instance.util.MessageSourceUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClusterSwitchoverRecord
 *
 * @author liupengfei
 * @since 2023/11/15
 */
@Data
@TableName(value = "cluster_switchover_record", autoResultMap = true)
@Accessors(chain = true)
@NoArgsConstructor
public class ClusterSwitchoverRecordDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("cluster_id")
    String clusterId;
    @TableField("switchover_time")
    String switchoverTime;
    @TableField("primary_ip")
    String primaryIp;
    @TableField("switchover_reason")
    String switchoverReason;
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Date updateTime = new Date();
    @TableField("cluster_node_id")
    String clusterNodeId;
    /**
     * getLogTime
     *
     * @param log gs_ctl log
     * @return log time
     */
    public static String getLogTime(String log) {
        String regex = "\\[(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})";
        Matcher matcher = Pattern.compile(regex).matcher(log);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new InstanceException("gs_ctl log getLogTime err : " + log);
        }
    }

    /**
     * getSwitchoverReason
     *
     * @param log gs_ctl log
     * @return switchover reason
     */
    public static String getSwitchoverReason(String log) {
        if (log.contains("failover")) {
            return "cluster.node.log.switchover.reason.failover";
        } else if (log.contains("switchover")) {
            return "cluster.node.log.switchover.reason.switchover";
        } else {
            throw new InstanceException("gs_ctl log getSwitchoverReason err : " + log);
        }
    }

    /**
     * I18n
     *
     * @return String
     */
    public String getSwitchoverReasonI18n() {
        return MessageSourceUtils.getMsg(this.getSwitchoverReason());
    }
}
