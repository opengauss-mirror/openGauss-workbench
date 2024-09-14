/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * -------------------------------------------------------------------------
 *
 * OpsClusterOperateLog.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/entity/ops/OpsClusterOperateLog.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.plugin.mapper.handler.LocalDateTimeTypeHandler;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.opengauss.admin.plugin.enums.ops.ClusterOperateTypeEnum;

import java.time.LocalDateTime;


/**
 * OpsClusterOperateLog
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Data
@TableName("ops_cluster_operate_log")
@EqualsAndHashCode(callSuper = true)
public class OpsClusterOperateLog extends BaseEntity {
    @TableId
    private String operateId;
    private String clusterId;
    private String clusterNodeId;
    /**
     * 创建，删除，修改，环境检查，确认，安装，
     * 初始化，启动，停止，重启，升级，回滚，卸载
     */
    private ClusterOperateTypeEnum operateType;
    /**
     * 操作命令，执行记录
     */
    private String operateLog;

    @TableField(typeHandler = LocalDateTimeTypeHandler.class)
    private LocalDateTime operateTime;

}
