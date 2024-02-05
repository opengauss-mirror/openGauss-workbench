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
 *  ResourceDO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/entity/ResourceDO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nctigba.observability.sql.enums.GrabTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ResourceDO
 *
 * @author luomeng
 * @since 2023/7/26
 */
@Data
@TableName(value = "diagnosis_resource", autoResultMap = true)
@Accessors(chain = true)
@NoArgsConstructor
public class ResourceDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer taskid;
    @TableField("grabType")
    GrabTypeEnum grabType;
    String f;

    /**
     * Construction method
     *
     * @param task     Diagnosis task info
     * @param grabType Bcc type
     */
    public ResourceDO(DiagnosisTaskDO task, GrabTypeEnum grabType) {
        super();
        this.taskid = task.getId();
        this.grabType = grabType;
    }
}