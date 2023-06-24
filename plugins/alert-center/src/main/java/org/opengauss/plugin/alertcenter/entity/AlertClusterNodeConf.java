/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * @author wuyuebin
 * @date 2023/4/26 16:11
 * @description 实例告警配置类
 */
@Accessors(chain = true)
@Data
@TableName("alert_cluster_node_conf")
public class AlertClusterNodeConf {
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String clusterNodeId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;
    private Timestamp updateTime;
    private Timestamp createTime;
    private Integer isDeleted;
}
