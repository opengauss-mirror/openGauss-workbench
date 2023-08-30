/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * OpsBackupEntity.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/entity/ops/OpsBackupEntity.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lhf
 * @date 2022/8/6 16:08
 **/
@Data
@TableName("ops_backup")
@EqualsAndHashCode(callSuper = true)
public class OpsBackupEntity extends BaseEntity {
    @TableId
    private String backupId;
    private String clusterId;
    private String hostId;
    private String clusterNodeId;
    private String backupPath;
}
