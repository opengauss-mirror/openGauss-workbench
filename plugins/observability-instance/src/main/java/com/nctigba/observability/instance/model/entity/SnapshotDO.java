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
 *  SnapshotDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/model/entity/SnapshotDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * Snapshot.java
 *
 * @since 2023-08-25
 */
@Data
@TableName("snapshot.snapshot")
public class SnapshotDO {
    private Long snapshotId;
    private Date startTs;
    private Date endTs;
}